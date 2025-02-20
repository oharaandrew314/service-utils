package dev.andrewohara.utils.pagination

data class Page<Item: Any, Cursor: Any>(
    val items: List<Item>,
    val next: Cursor?
)



