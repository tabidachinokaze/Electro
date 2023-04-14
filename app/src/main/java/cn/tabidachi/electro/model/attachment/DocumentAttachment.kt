package cn.tabidachi.electro.model.attachment

sealed interface DocumentAttachment : Attachment {
    // mime类型
    val contentType: String
    val uri: String?

    // 本地路径
    val path: String?

    // 文件名
    val filename: String

    // 文件大小，bytes
    val size: Long

    // 文件在服务器上的地址
    val url: String?

//    val md5: String
    val bucket: String?
    val `object`: String?
    // 显示名称
    val displayName: String?
}