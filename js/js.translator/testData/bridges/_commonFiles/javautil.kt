package java.util

public object Arrays {
    public fun asList<T>(vararg ts : T) : List<T> {
        val result = ArrayList<T>()
        for (t in ts) {
            result.add(t)
        }
        return result
    }
}

