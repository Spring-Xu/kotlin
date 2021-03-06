package test.collections

import kotlin.test.*
import org.junit.Test

class ListSpecificTest {
    val data = listOf("foo", "bar")
    val empty = listOf<String>()

    @Test
    fun _toString() {
        assertEquals("[foo, bar]", data.toString())
    }

    @Test
    fun tail() {
        val data = listOf("foo", "bar", "whatnot")
        val actual = data.drop(1)
        assertEquals(listOf("bar", "whatnot"), actual)
    }

    @Test
    fun slice() {
        val list = listOf('A', 'B', 'C', 'D')
        // ABCD
        // 0123
        assertEquals(listOf('B', 'C', 'D'), list.slice(1..3))
        assertEquals(listOf('D', 'C', 'B'), list.slice(3 downTo 1))

        val iter = listOf(2, 0, 3)
        assertEquals(listOf('C', 'A', 'D'), list.slice(iter))
    }

    @Test
    fun getOr() {
        expect("foo") { data.get(0) }
        expect("bar") { data.get(1) }
        fails { data.get(2) }
        fails { data.get(-1) }
        fails { empty.get(0) }

        expect("foo") { data.getOrElse(0, {""} )}
        expect("zoo") { data.getOrElse(-1, { "zoo" })}
        expect("zoo") { data.getOrElse(2, { "zoo" })}
        expect("zoo") { empty.getOrElse(0) { "zoo" }}

        expect(null) { empty.getOrNull(0) }

    }

    @Test
    fun lastIndex() {
        assertEquals(-1, empty.lastIndex)
        assertEquals(1, data.lastIndex)
    }

    @Test
    fun mutableList() {
        val items = listOf("beverage", "location", "name")

        var list = listOf<String>()
        for (item in items) {
            list += item
        }

        assertEquals(3, list.size())
        assertEquals("beverage,location,name", list.join(","))
    }

    @Test
    fun testNullToString() {
        assertEquals("[null]", listOf<String?>(null).toString())
    }
}
