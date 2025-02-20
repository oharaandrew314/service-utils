package dev.andrewohara.utils.pagination

import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PageTest {

    private val repo = FakePaginationRepo(pageSize = 2)
    private val stuff1 = FakeMessage(1, topic = "stuff", message = "stuff1").also(repo::plusAssign)
    private val thing1 = FakeMessage(2, topic = "things", message = "thing1").also(repo::plusAssign)
    private val stuff2 =  FakeMessage(3, topic = "stuff", message = "stuff2").also(repo::plusAssign)
    private val stuff3 = FakeMessage(4, topic = "stuff", message = "stuff3").also(repo::plusAssign)

    @Test
    fun `repo sanity`() {
        repo.list("things", null) shouldBe Page(
            items = listOf(thing1),
            next = null
        )
        repo.list("stuff", null) shouldBe Page(
            items = listOf(stuff1, stuff2),
            next = stuff3.id
        )
        repo.list("stuff", stuff3.id) shouldBe Page(
            items = listOf(stuff3),
            next = null
        )
    }

    @Test
    fun `paginator as sequence`() {
        val paginator = Paginator { cursor: Int? -> repo.list("stuff", cursor) }
        paginator.asSequence().shouldContainExactly(stuff1, stuff2, stuff3)
    }

    @Test
    fun `filter page`() {
        Page(
            items = listOf(stuff1, thing1, stuff2, stuff3),
            next = null
        )
            .filter { it.topic == "stuff" } shouldBe Page(
                items = listOf(stuff1, stuff2, stuff3),
                next = null
            )
        }

    @Test
    fun `map page`() {
        Page(
            items = listOf(stuff1, thing1, stuff2, stuff3),
            next = null
        )
            .map { it.copy(topic = "all") } shouldBe Page(
            items = listOf(
                stuff1.copy(topic = "all"),
                thing1.copy(topic = "all"),
                stuff2.copy(topic = "all"),
                stuff3.copy(topic = "all")
            ),
            next = null
        )
    }
}