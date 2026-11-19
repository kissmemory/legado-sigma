package io.legado.app.api.controller

import androidx.annotation.Keep
import io.legado.app.BuildConfig
import io.legado.app.api.ReturnData
import io.legado.app.constant.BookType
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.help.book.DiscoveryBookIdentity
import io.legado.app.utils.GSON
import io.legado.app.utils.getPrefString
import io.legado.app.utils.putPrefString
import splitties.init.appCtx
import java.util.UUID
import java.io.File
import java.util.Properties
import io.legado.app.model.webBook.WebBook

/** Atomic new-book insertion only. Never invokes WebDAV or alters existing progress. */
object DiscoveryBridgeController {
    @Keep data class GroupProof(val groupId: String, val groupName: String)
    @Keep data class CreateRequest(val deviceId: String, val identity: String, val operationId: String, val groups: List<GroupProof>, val book: Book)

    @Keep data class ResolveRequest(val source: String, val identity: String, val title: String)
    private val credentialFile get() = File(appCtx.noBackupFilesDir, "reading-discovery-bridge.properties")

    @Synchronized private fun credentials(create: Boolean): Properties {
        val file = credentialFile
        val values = Properties()
        if (file.exists()) file.inputStream().use(values::load)
        if (create && (values.getProperty("deviceId") == null || values.getProperty("pairing") == null)) {
            values.setProperty("deviceId", UUID.randomUUID().toString())
            values.setProperty("pairing", (UUID.randomUUID().toString()+UUID.randomUUID().toString()).replace("-", ""))
            file.parentFile?.mkdirs()
            val temp = File(file.parentFile, file.name + ".tmp")
            temp.outputStream().use { values.store(it, "Local instance; excluded from backups") }
            check(temp.renameTo(file)) { "Unable to store pairing" }
        }
        return values
    }
    private fun deviceId(): String = credentials(true).getProperty("deviceId")
    fun pairingToken(): String = credentials(true).getProperty("pairing")
    fun authorized(value: String?): Boolean {
        val saved = credentials(false).getProperty("pairing") ?: return false
        return value != null && java.security.MessageDigest.isEqual(saved.toByteArray(), value.toByteArray())
    }

    suspend fun resolve(postData: String?): ReturnData {
        return try {
            require(postData != null && postData.length <= 8192)
            val request = GSON.fromJson(postData, ResolveRequest::class.java)
            require(request.title.isNotBlank() && request.title.length <= 500)
            val source = appDb.bookSourceDao.getBookSource(request.source) ?: error("source_missing")
            val matches = WebBook.searchBookAwait(source, request.title, 1)
                .filter { DiscoveryBookIdentity.key(it.bookUrl) == request.identity }
                .distinctBy { it.bookUrl }
            require(matches.size == 1) { "native_book_not_unique" }
            ReturnData().setData(matches.single().toBook())
        } catch (_: Exception) {
            ReturnData().setErrorMsg("所选书源没有返回唯一固定作品ID，请检查该书源搜索与作品是否正常。")
        }
    }

    fun info(): ReturnData = ReturnData().setData(mapOf(
        "protocol" to 1, "atomicCreateOnly" to true, "noWebDav" to true,
        "deviceId" to deviceId(), "packageName" to appCtx.packageName,
        "appName" to appCtx.applicationInfo.loadLabel(appCtx.packageManager).toString(),
        "versionName" to BuildConfig.VERSION_NAME,
        "groups" to appDb.bookGroupDao.all.filter { it.groupId > 0 || it.groupId == Long.MIN_VALUE }.map {
            mapOf("groupId" to it.groupId.toString(), "groupName" to it.groupName)
        }
    ))

    fun create(postData: String?): ReturnData {
        return try {
            require(postData != null && postData.length <= 65536) { "invalid_request" }
            val request = GSON.fromJson(postData, CreateRequest::class.java)
            require(request.operationId.matches(Regex("[a-f0-9]{32}"))) { "invalid_operation" }
            require(request.deviceId == deviceId()) { "device_changed" }
            val book = request.book
            require(book.name.isNotBlank() && book.name.length <= 500) { "invalid_book" }
            require(DiscoveryBookIdentity.key(book.bookUrl) == request.identity) { "identity_mismatch" }
            require(request.identity.matches(Regex("(?:jj|cp|ch):[1-9][0-9]*"))) { "invalid_identity" }
            require(book.durChapterIndex == 0 && book.durChapterPos == 0 && book.durChapterTime == 0L) { "new_book_only" }
            require(request.groups.isNotEmpty() && request.groups.map { it.groupId }.distinct().size == request.groups.size) { "invalid_groups" }
            appDb.runInTransaction {
                val actual = appDb.bookGroupDao.all.associateBy { it.groupId }
                var mask = 0L
                request.groups.forEach {
                    val id = it.groupId.toLong()
                    require((id > 0 && (id and (id - 1)) == 0L) || id == Long.MIN_VALUE) { "invalid_group_id" }
                    require(actual[id]?.groupName == it.groupName) { "groups_changed" }
                    mask = mask or id
                }
                require(mask == book.group) { "group_mask_mismatch" }
                require(appDb.bookSourceDao.getBookSource(book.origin) != null) { "source_missing" }
                require(appDb.bookDao.all.none { it.bookUrl == book.bookUrl || DiscoveryBookIdentity.key(it.bookUrl) == request.identity }) { "already_exists" }
                book.putVariable("__reading_discovery_operation_v1", request.operationId)
                book.type = BookType.text
                book.totalChapterNum = 0
                book.lastCheckCount = 0
                require(appDb.bookDao.insertNewOnly(book) != -1L) { "already_exists" }
            }
            ReturnData().setData(mapOf("created" to true, "identity" to request.identity, "operationId" to request.operationId))
        } catch (_: Exception) {
            ReturnData().setErrorMsg("新增被拒绝：作品已存在，或设备/书源/实时分组发生变化。未覆盖已有书籍。")
        }
    }
}
