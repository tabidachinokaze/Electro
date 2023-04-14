package cn.tabidachi.electro.model.response

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val status: Int,
    val message: String,
    val data: T?
)