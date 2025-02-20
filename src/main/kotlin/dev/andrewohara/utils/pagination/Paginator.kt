package dev.andrewohara.utils.pagination

fun interface Paginator<Item: Any, Cursor: Any> {
    operator fun invoke(cursor: Cursor?): Page<Item, Cursor>
}