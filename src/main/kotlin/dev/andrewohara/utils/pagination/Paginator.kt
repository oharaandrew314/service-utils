package dev.andrewohara.utils.pagination

fun interface Paginator<Item: Any, Cursor: Any>: Iterable<Item> {
    operator fun get(cursor: Cursor?): Page<Item, Cursor>

    override fun iterator() = iterator<Item> {
        var cursor: Cursor? = null
        do {
            val page = get(cursor)
            yieldAll(page.items)
            cursor = page.next
        } while (cursor != null)
    }
}