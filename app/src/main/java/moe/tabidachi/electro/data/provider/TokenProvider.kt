package moe.tabidachi.electro.data.provider

fun interface TokenProvider {
    fun getToken(): String?
}