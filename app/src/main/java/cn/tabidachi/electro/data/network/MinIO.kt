package cn.tabidachi.electro.data.network

import cn.tabidachi.electro.BuildConfig
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.GetObjectResponse
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import java.io.InputStream

class MinIO(
    url: String = BuildConfig.MINIO_URL,
    accessKey: String = BuildConfig.MINIO_ACCESS_KEY,
    secretKey: String = BuildConfig.MINIO_SECRET_KEY
) {
    val client = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build()
    fun checkOrCreateBucket(name: String): Boolean {
        return when (client.bucketExists(BucketExistsArgs.builder().bucket(name).build())) {
            true -> true
            false -> {
                return kotlin.runCatching {
                    client.makeBucket(
                        MakeBucketArgs.builder()
                            .bucket(name)
                            .build()
                    )
                    true
                }.getOrElse {
                    false
                }
            }
        }
    }

    fun upload(inputStream: InputStream, filename: String): String {
        if (!checkOrCreateBucket("electro")) {
            throw Exception("未创建bucket")
        }
        return client.putObject(
            PutObjectArgs.builder()
                .bucket("electro")
                .stream(inputStream, -1, 1_073_741_824)
                .`object`(filename)
                .build()
        ).`object`()
    }

    fun download(filename: String): GetObjectResponse? {
        return client.getObject(
            GetObjectArgs.builder()
                .bucket("electro")
                .`object`(filename)
                .build()
        )
    }

    companion object {
        const val ELECTRO = "electro"
        const val UPLOAD = "upload"
        const val AVATAR = "avatar"
    }
}