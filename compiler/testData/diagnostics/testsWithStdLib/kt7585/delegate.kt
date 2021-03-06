// FILE: Base.java

interface Base {}

// FILE: Other.java

interface Other {}

// FILE: Derived.java

final class Derived<T> implements Base, Other {}

// FILE: Exotic.java

final class Exotic implements Base, Other {

    int x;

    Exotic(int x) {
        this.x = x;
    }
}

// FILE: Properties.java

import kotlin.jvm.functions.Function0;

class Val<T> {

    Function0<T> initializer;

    Val(Function0<T> initializer) {
        this.initializer = initializer;
    }

    T get(Object instance, Object metadata) {
        return initializer.invoke();
    }
}

class Properties {
    static <T> Val<T> calcVal(Function0<T> initializer) {
        return new Val<T>(initializer);
    }
}

// FILE: My.kt

open class Wrapper<out T: Base>(val v: T)

class DerivedWrapper(v: Derived<*>): Wrapper<Derived<*>>(v)

class ExoticWrapper(v: Exotic): Wrapper<Exotic>(v)

object MyBase {

    fun derived() = Derived<String>()
    fun exotic(x: Int) = Exotic(x)

    fun derivedWrapper() = DerivedWrapper(derived())
    fun exoticWrapper(x: Int) = ExoticWrapper(exotic(x))
}

class My(val x: Int) {
    val wrapper/*: Wrapper<*>*/ by Properties.calcVal {
        val y = x + 1
        when {
            y > 0 -> MyBase.derivedWrapper()
            x < 0 -> MyBase.exoticWrapper(x)
            else  -> throw java.lang.NullPointerException("")
        }
    }
}
