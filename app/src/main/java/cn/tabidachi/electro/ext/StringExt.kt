package cn.tabidachi.electro.ext

fun String.isEmail() = matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$".toRegex())

fun String.isValidPassword() = matches("[a-zA-Z0-9-*/+.~!@#$%^&()]{8,64}$".toRegex())

fun String.regex(): Regex {
    return this.toCharArray().joinToString("", "^", ".+") {
        "(?=.*$it)"
    }.toRegex()
}

fun String.extension(): String {
    return this.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
}