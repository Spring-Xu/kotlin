class MyClass

fun MyClass?.inc(): MyClass? = null

public fun box() : String {
    var i : MyClass? 
    i = MyClass()
    val j = i++

    return if (j is MyClass && null == i) "OK" else "fail i = $i j = $j"
}