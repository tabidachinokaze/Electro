package cn.tabidachi.electro.ext

import io.ktor.http.URLProtocol

val URLProtocol.Companion.ELECTRO: URLProtocol
    get() = URLProtocol("electro", 23333)

val URLProtocol.Companion.MINIO: URLProtocol
    get() = URLProtocol("minio", 9000)