package cn.tabidachi.electro.ext

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

val Long.size get() = this.toDouble()
val Long.sizeInKb get() = size / 1024
val Long.sizeInMb get() = sizeInKb / 1024
val Long.sizeInGb get() = sizeInMb / 1024
val Long.sizeInTb get() = sizeInGb / 1024

fun Long.sizeStr(): String = size.toString()
fun Long.sizeStrInKb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInKb)
fun Long.sizeStrInMb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInMb)
fun Long.sizeStrInGb(decimals: Int = 0): String = "%.${decimals}f".format(sizeInGb)

fun Long.sizeStrWithBytes(): String = sizeStr() + " B"
fun Long.sizeStrWithKb(decimals: Int = 0): String = sizeStrInKb(decimals) + " kB"
fun Long.sizeStrWithMb(decimals: Int = 0): String = sizeStrInMb(decimals) + " MB"
fun Long.sizeStrWithGb(decimals: Int = 0): String = sizeStrInGb(decimals) + " GB"

fun Long.sizeStrWithAuto(decimals: Int = 0): String {
    return when {
        this < 1024 -> sizeStrWithBytes()
        this < 1024 * 1024 -> sizeStrWithKb(decimals)
        this < 1024 * 1024 * 1024 -> sizeStrWithMb(decimals)
        else -> sizeStrWithGb(decimals)
    }
}

fun Long.timeFormat(): String {
    return if (Date().day == Date(this).day) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(this))
    } else {
//        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(this))
        SimpleDateFormat("MMMdd").format(Date(this))
    }
}

fun Long.longTimeFormat(): String {
    val second = this / 1000 % 60
    val minutes = this / 1000 / 60
    return "${if (minutes < 10) "0" else ""}$minutes:${if (second < 10) "0" else ""}$second"
}