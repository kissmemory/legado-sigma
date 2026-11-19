package io.legado.app.api.controller

import androidx.annotation.Keep
import io.legado.app.api.ReturnData
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.help.book.DiscoveryBookIdentity
import io.legado.app.help.book.TaskGroupSyncRules
import io.legado.app.help.book.restoreProgressTitle
import io.legado.app.model.ReadBook
import io.legado.app.utils.GSON
import java.security.MessageDigest
import java.util.UUID

/** Paired, previewed field deltas. Never refreshes a TOC, inserts books or invokes WebDAV. */
object DiscoverySyncController {
    @Keep data class Change(val identity: String, val groupIds: List<String>, val chapterRef: String?, val expectedFingerprint: String)
    @Keep data class PreviewRequest(val deviceId: String, val groups: List<DiscoveryBridgeController.GroupProof>, val changes: List<Change>)
    @Keep data class ApplyRequest(val plan: String)
    private data class Pending(val bookUrl: String, val identity: String, val before: String, val mask: Long, val chapterIndex: Int?, val chapterRef: String?, val tocHash: String?)
    private data class Plan(val groupsHash: String, val device: String, val items: List<Pending>, val expires: Long)
    private val plans = mutableMapOf<String, Plan>()
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private fun fingerprint(book: Book) = hash(GSON.toJson(book))
    private fun otherFingerprint(book: Book): String {
        val value = GSON.toJsonTree(book).asJsonObject
        listOf("group", "durChapterIndex", "durChapterPos", "durChapterTitle", "durChapterTime").forEach { value.remove(it) }
        return hash(value.toString())
    }
    private fun groupsHash() = hash(GSON.toJson(appDb.bookGroupDao.all.sortedBy { it.groupId }.map { listOf(it.groupId.toString(), it.groupName) }))
    private fun tocHash(url: String) = hash(GSON.toJson(appDb.bookChapterDao.getChapterList(url).map { listOf(it.url, it.index, it.title, it.isVolume) }))
    private fun reading(url: String) = ReadBook.callBack != null && ReadBook.book?.bookUrl == url

    fun state(): ReturnData {
        val records = appDb.bookDao.all.mapNotNull { book ->
            val identity = DiscoveryBookIdentity.key(book.bookUrl) ?: return@mapNotNull null
            val chapter = appDb.bookChapterDao.getChapter(book.bookUrl, book.durChapterIndex)
            mapOf("identity" to identity, "remote_ref" to "sha256:${hash(book.bookUrl)}", "fingerprint" to fingerprint(book), "other_fingerprint" to otherFingerprint(book),
                "group_mask" to java.lang.Long.toUnsignedString(book.group), "chapter_ref" to chapter?.takeUnless { it.isVolume }?.let { TaskGroupSyncRules.chapterRef(identity, it.url) },
                "chapter_title" to book.durChapterTitle, "chapter_index" to book.durChapterIndex, "chapter_time" to book.durChapterTime,
                "first_volume" to (chapter?.isVolume == true && chapter.index == 0))
        }
        return ReturnData().setData(mapOf("deviceId" to DiscoveryBridgeController.instanceId(), "groupsHash" to groupsHash(), "books" to records))
    }

    @Synchronized fun preview(raw: String?): ReturnData {
        return try {
            require(raw != null && raw.length <= 2 * 1024 * 1024)
            val request = GSON.fromJson(raw, PreviewRequest::class.java)
            require(request.deviceId == DiscoveryBridgeController.instanceId())
            require(request.changes.isNotEmpty() && request.changes.size <= 5000 && request.changes.map { it.identity }.distinct().size == request.changes.size)
            val startingGroupsHash = groupsHash()
            val actualGroups = appDb.bookGroupDao.all.filter { TaskGroupSyncRules.isTaskGroup(it.groupName) }
            require(request.groups.size == actualGroups.size && request.groups.map { it.groupId }.distinct().size == request.groups.size)
            var allTasks = 0L
            request.groups.forEach { proof ->
                val group = actualGroups.single { it.groupId.toString() == proof.groupId && it.groupName == proof.groupName }
                require(group.groupId > 0 || group.groupId == Long.MIN_VALUE)
                allTasks = allTasks or group.groupId
            }
            val known = appDb.bookDao.all.groupBy { DiscoveryBookIdentity.key(it.bookUrl) }
            val pending = mutableListOf<Pending>()
            val ready = mutableListOf<Map<String, Any?>>()
            val blocked = mutableListOf<Map<String, String>>()
            for (change in request.changes) {
                try {
                    val book = known[change.identity]?.singleOrNull() ?: error("手机没有唯一对应作品，请先单独新增或核对重书")
                    require(!reading(book.bookUrl)) { "请先退出这本书的阅读页面" }
                    require(fingerprint(book) == change.expectedFingerprint) { "手机书目已变化，请重新读取并预览" }
                    require(change.groupIds.distinct().size == change.groupIds.size)
                    var selected = 0L
                    change.groupIds.forEach { gid -> require(request.groups.any { it.groupId == gid }); selected = selected or gid.toLong() }
                    val mask = TaskGroupSyncRules.workMask(book.group, allTasks, selected)
                    val chapter = change.chapterRef?.let { ref ->
                        require(ref.length < 160)
                        appDb.bookChapterDao.getChapterList(book.bookUrl).filter { !it.isVolume && TaskGroupSyncRules.chapterRef(change.identity, it.url) == ref }.singleOrNull()
                            ?: error("手机缓存目录没有唯一目标章，请在手机更新目录后重试；本次不自动刷新")
                    }
                    pending.add(Pending(book.bookUrl, change.identity, fingerprint(book), mask, chapter?.index, change.chapterRef, chapter?.let { tocHash(book.bookUrl) }))
                    ready.add(mapOf("identity" to change.identity, "title" to book.name, "before_mask" to java.lang.Long.toUnsignedString(book.group), "after_mask" to java.lang.Long.toUnsignedString(mask), "target_ref" to change.chapterRef, "target_index" to chapter?.index))
                } catch (e: Exception) {
                    blocked.add(mapOf("identity" to change.identity, "reason" to (e.message?.takeIf { it.any { c -> c.code > 127 } } ?: "书目、分组或章节对应校验失败，未加入推送")))
                }
            }
            plans.entries.removeAll { it.value.expires < System.currentTimeMillis() }
            require(plans.size < 8) { "待确认预览过多，请稍后重试" }
            val token = UUID.randomUUID().toString()
            require(startingGroupsHash == groupsHash()) { "groups_changed" }
            plans[token] = Plan(startingGroupsHash, request.deviceId, pending, System.currentTimeMillis() + 600000)
            ReturnData().setData(mapOf("plan" to token, "ready" to ready, "blocked" to blocked, "writes" to 0))
        } catch (_: Exception) { ReturnData().setErrorMsg("推送预览被拒绝：设备、实时工作组或请求发生变化，请重新同步。手机未修改。") }
    }

    @Synchronized fun apply(raw: String?): ReturnData {
        return try {
            require(raw != null && raw.length < 1024)
            val request = GSON.fromJson(raw, ApplyRequest::class.java)
            val plan = plans.remove(request.plan) ?: error("expired")
            require(plan.expires >= System.currentTimeMillis() && plan.device == DiscoveryBridgeController.instanceId() && plan.items.isNotEmpty())
            appDb.runInTransaction {
                require(plan.groupsHash == groupsHash()) { "groups_changed" }
                for (item in plan.items) {
                    val book = appDb.bookDao.getBook(item.bookUrl) ?: error("book_missing")
                    require(!reading(item.bookUrl) && fingerprint(book) == item.before && DiscoveryBookIdentity.key(book.bookUrl) == item.identity) { "phone_changed" }
                    if (item.chapterIndex != null) {
                        require(item.tocHash == tocHash(item.bookUrl)) { "catalog_changed" }
                        val chapter = appDb.bookChapterDao.getChapter(item.bookUrl, item.chapterIndex) ?: error("chapter_missing")
                        require(!chapter.isVolume && TaskGroupSyncRules.chapterRef(item.identity, chapter.url) == item.chapterRef)
                        book.durChapterIndex = item.chapterIndex
                        book.restoreProgressTitle(chapter.title)
                        appDb.bookDao.updateDiscoverySyncFields(item.bookUrl, item.mask, item.chapterIndex, book.durChapterTitle, System.currentTimeMillis())
                    } else appDb.bookDao.updateDiscoveryWorkGroups(item.bookUrl, item.mask)
                }
            }
            ReturnData().setData(mapOf("applied" to plan.items.map { it.identity }, "plan" to request.plan, "noWebDav" to true))
        } catch (_: Exception) { ReturnData().setErrorMsg("推送未完成：预览过期或手机数据已改变；事务已回滚，请先只读核验，不自动重试。") }
    }
}
