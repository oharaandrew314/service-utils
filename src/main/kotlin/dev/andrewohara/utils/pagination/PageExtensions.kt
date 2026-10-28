package dev.andrewohara.utils.pagination

fun <In: Any, Out: Any, Cursor: Any> Page<In, Cursor>.map(fn: (In) -> Out) = Page(
    items = items.map(fn),
    next = next
)

fun <Item: Any, Cursor: Any> Page<Item, Cursor>.filter(fn: (Item) -> Boolean) = Page(
    items = items.filter(fn),
    next = next
)

fun <Item: Any, In: Any, Out: Any> Page<Item, In>.mapCursor(fn: (In) -> Out) = Page(
    items = items,
    next = next?.let(fn)
)

fun <Item: Any, Id: Any> Iterable<Item>.toPage(limit: Int, idFn: (Item) -> Id) =
    take(limit + 1).toPage(limit, idFn)

fun <Item: Any, Id: Any> Collection<Item>.toPage(limit: Int, idFn: (Item) -> Id) = Page(
    items = take(limit),
    next = drop(limit).firstOrNull()?.let(idFn)
)