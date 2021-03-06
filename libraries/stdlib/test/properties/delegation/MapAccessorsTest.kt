package test.properties.delegation.map

import java.util.*
import kotlin.properties.*
import kotlin.test.*
import org.junit.Test as test

class ValByMapExtensionsTest {
    val map: Map<String, String> = hashMapOf("a" to "all", "b" to "bar", "c" to "code")
    val genericMap = mapOf<String, Any?>("i" to 1, "x" to 1.0)

    val a: String by map
    val b: String by map
    val c: Any by map
    val d: String? by map
    val e: String? by map.withDefault { "default" }
    val i: Int by genericMap
    val x: Double by genericMap


    @test fun doTest() {
        assertEquals("all", a)
        assertEquals("bar", b)
        assertEquals("code", c)
        assertEquals("default", e)
        assertEquals(1, i)
        assertEquals(1.0, x)
        assertTrue(fails { d } is NoSuchElementException)
    }
}



class VarByMapExtensionsTest {
    val map = hashMapOf<String, Any?>("a" to "all", "b" to null, "c" to 1, "xProperty" to 1.0)
    val map2: MutableMap<String, CharSequence> = hashMapOf("a2" to "all")

    var a: String by map
    var b: Any? by map
    var c: Int by map
    var d: String? by map
    var a2: String by map2.withDefault { "empty" }
    //var x: Int by map2  // prohibited by type system

    @test fun doTest() {
        assertEquals("all", a)
        assertEquals(null, b)
        assertEquals(1, c)
        c = 2
        assertEquals(2, c)
        assertEquals(2, map["c"])

        assertEquals("all", a2)
        map2.remove("a2")
        assertEquals("empty", a2)

        map["c"] = "string"
        // fails { c }  // does not fail in JS due to KT-8135

        map["a"] = null
        a // fails { a } // does not fail due to KT-8135

        assertTrue(fails { d } is NoSuchElementException)
        map["d"] = null
        assertEquals(null, d)
    }
}
