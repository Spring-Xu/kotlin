package foo

// CHECK_CONTAINS_NO_CALLS: test

internal var counter = 0

internal fun test(a: Int) {
    a.times {
        counter += 1
    }
}

fun box(): String {
    assertEquals(0, counter)
    test(5)
    assertEquals(5, counter)

    return "OK"
}