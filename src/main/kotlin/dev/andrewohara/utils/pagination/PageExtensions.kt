package dev.andrewohara.utils.pagination

fun <In: Any, Out: Any, Cursor: Any> Page<In, Cursor>.map(fn: (In) -> Out) = Page(
    items = items.map(fn),
    next = next
)

fun <Item: Any, Cursor: Any> Page<Item, Cursor>.filter(fn: (Item) -> Boolean) = Page(
    items = items.filter(fn),
    next = next
)

fun <Item: Any, Cursor: Any> stream(paginator: Paginator<Item, Cursor>) = sequence {
    var cursor: Cursor? = null
    do {
        val page = paginator(cursor)
        yieldAll(page.items)
        cursor = page.next
    } while (cursor != null)
}

fun <Item: Any, Cursor: Any> Paginator<Item, Cursor>.asSequence() = stream(this)