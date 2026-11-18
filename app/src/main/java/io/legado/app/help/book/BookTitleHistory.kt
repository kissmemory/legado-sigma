package io.legado.app.help.book

import androidx.annotation.Keep
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonObject

/** Stored in the existing book variables so normal backup/restore includes it. */
object BookTitleHistory {
    private const val VARIABLE = "__sigma_jj_title_history_v1"

    @Keep
    data class History(val identity: String, val titles: List<String>, val current: String)

    private fun read(book: Book): History? {
        val identity = JinjiangShelfIdentity.key(book.bookUrl) ?: return null
        val history = GSON.fromJsonObject<History>(book.getVariable(VARIABLE)).getOrNull() ?: return null
        return history.takeIf { it.identity == identity && !it.current.isNullOrBlank() && !it.titles.isNullOrEmpty() }
    }

    fun observe(book: Book, previousName: String, observedName: String) {
        val identity = JinjiangShelfIdentity.key(book.bookUrl) ?: return
        if (observedName.isBlank()) return
        // A detail opened from discovery/search may have a fresh Book instance. Merge
        // saved history by stable ID before that fresh instance can replace its variables.
        val shelf = appDb.bookDao.all.filter {
            !it.isNotShelf && JinjiangShelfIdentity.key(it.bookUrl) == identity
        }
        val seen = linkedSetOf<String>()
        read(book)?.titles?.let(seen::addAll)
        shelf.forEach { saved -> read(saved)?.titles?.let(seen::addAll) }
        val seedName = shelf.firstOrNull { it.bookUrl == book.bookUrl }?.name
            ?: shelf.firstOrNull()?.name ?: previousName
        val titles = JinjiangTitleHistory.observe(seen.toList(), seedName, observedName)
        book.putVariable(VARIABLE, GSON.toJson(History(identity, titles, observedName.trim())))
    }

    fun displayIntro(book: Book, intro: String?): String? {
        val history = read(book) ?: return intro
        return JinjiangTitleHistory.decorate(history.titles, history.current, intro)
    }

    fun copyFrom(source: Book, destination: Book) {
        if (read(source) != null && JinjiangShelfIdentity.key(source.bookUrl) == JinjiangShelfIdentity.key(destination.bookUrl)) {
            destination.putVariable(VARIABLE, source.getVariable(VARIABLE))
        }
    }
}
