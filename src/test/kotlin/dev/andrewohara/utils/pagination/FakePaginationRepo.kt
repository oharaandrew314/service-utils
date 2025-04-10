package dev.andrewohara.utils.pagination

data class FakeMessage(
    val id: Int,
    val topic: String,
    val message: String
)

class FakePaginationRepo(private val pageSize: Int) {

    private val messages = mutableListOf<FakeMessage>()

    operator fun plusAssign(message: FakeMessage) = messages.plusAssign(message)

    fun list(topic: String) = Paginator<FakeMessage, Int> { cursor ->
        val results = messages
            .sortedBy { it.id }
            .filter { it.topic == topic }
            .dropWhile { cursor != null && it.id < cursor }

        Page(
            items = results.take(pageSize),
            next = results.getOrNull(pageSize)?.id
        )
    }
}