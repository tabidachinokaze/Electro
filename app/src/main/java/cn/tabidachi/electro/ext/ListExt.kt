package cn.tabidachi.electro.ext

fun <T> MutableList<T>.addIfAbsent(element: T): Boolean {
    return if (contains(element)) {
        false
    } else {
        add(element)
        true
    }
}