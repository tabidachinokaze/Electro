package cn.tabidachi.electro.ext

import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

fun InputStream.md5WithCopy(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
    val digest = MessageDigest.getInstance("MD5")
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        digest.update(buffer, 0, bytes)
        bytes = read(buffer)
    }
    return digest.digest()
}

fun InputStream.md5(bufferSize: Int = DEFAULT_BUFFER_SIZE): ByteArray {
    val digest = MessageDigest.getInstance("MD5")
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        digest.update(buffer, 0, bytes)
        bytes = read(buffer)
    }
    return digest.digest()
}