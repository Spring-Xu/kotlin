@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(vararg val p: Int)

@Ann() class MyClass1
@Ann(1) class MyClass2
@Ann(1, 2) class MyClass3

@Ann(*intArrayOf()) class MyClass4
@Ann(*intArrayOf(1)) class MyClass5
@Ann(*intArrayOf(1, 2)) class MyClass6

@Ann(p = 1) class MyClass7

@Ann(p = *intArrayOf()) class MyClass8
@Ann(p = *intArrayOf(1)) class MyClass9
@Ann(p = *intArrayOf(1, 2)) class MyClass10

fun box(): String {
    test(javaClass<MyClass1>(), "")
    test(javaClass<MyClass2>(), "1")
    test(javaClass<MyClass3>(), "12")

    test(javaClass<MyClass4>(), "")
    test(javaClass<MyClass5>(), "1")
    test(javaClass<MyClass6>(), "12")

    test(javaClass<MyClass7>(), "1")

    test(javaClass<MyClass8>(), "")
    test(javaClass<MyClass9>(), "1")
    test(javaClass<MyClass10>(), "12")

    return "OK"
}

fun test(klass: Class<*>, expected: String) {
    val ann = klass.getAnnotation(javaClass<Ann>())
    if (ann == null) throw AssertionError("fail: cannot find Ann on ${klass}")

    var result = ""
    for (i in ann.p) {
        result += i
    }

    if (result != expected) {
        throw AssertionError("fail: expected = ${expected}, actual = ${result}")
    }
}