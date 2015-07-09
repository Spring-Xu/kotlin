// NO_KOTLIN_REFLECT

import kotlin.test.assertEquals
import kotlin.reflect.jvm.lite.*

class Klass

inline fun <reified T> simpleName(): String =
        T::class.java.getSimpleName()

inline fun <reified T> simpleName2(): String {
    val kClass = T::class // Intrinsic for T::class.java is not used
    return kClass.java.getSimpleName()
}


fun box(): String {
    assertEquals("Integer", simpleName<Int>())
    assertEquals("Integer", simpleName2<Int>())
    assertEquals("Klass", simpleName<Klass>())

    return "OK"
}