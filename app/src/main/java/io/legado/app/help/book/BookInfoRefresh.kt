package io.legado.app.help.book

import io.legado.app.data.entities.Book

/** Explicit detail refresh must not reuse a discovery/search response. */
object BookInfoRefresh {
    fun prepare(book: Book, forceFresh: Boolean) {
        if (forceFresh) {
            book.infoHtml = null
            book.tocHtml = null
        }
    }
}
