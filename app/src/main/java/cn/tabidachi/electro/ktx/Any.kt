package cn.tabidachi.electro.ktx

val Any.TAG: String get() = this::class.java.simpleName
