package moe.tabidachi.electro.data.network

import io.minio.BucketExistsArgs
import io.minio.ComposeObjectArgs
import io.minio.ComposeSource
import io.minio.GetObjectArgs
import io.minio.GetObjectResponse
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import io.minio.StatObjectArgs
import io.minio.StatObjectResponse
import io.minio.http.Method
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import moe.tabidachi.electro.data.repository.SharedRepository
import java.io.InputStream

class MinIO(
    val sharedRepository: SharedRepository,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private var client: MinioClient? = null

    init {
        sharedRepository.state.map {
            Triple(it.minioUrl, it.accessKey, it.secretKey)
        }.filter { it.first.isNotBlank() && it.second.isNotBlank() && it.third.isNotBlank() }
            .distinctUntilChanged()
            .onEach { (minioUrl, accessKey, secretKey) ->
                client?.close()
                client = MinioClient.builder().endpoint(minioUrl).credentials(accessKey, secretKey)
                    .build()
            }.launchIn(scope)
    }

    fun checkOrCreateBucket(name: String): Boolean {
        val client = client ?: return false
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

    fun getPresignedObjectUrl(
        method: Method,
        bucket: String,
        `object`: String
    ): String? {
        return client?.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(method)
                .bucket(bucket)
                .`object`(`object`)
                .build()
        )
    }

    fun statObject(
        bucket: String,
        `object`: String
    ): StatObjectResponse? {
        return client?.statObject(
            StatObjectArgs.builder()
                .bucket(bucket)
                .`object`(`object`)
                .build()
        )
    }

    fun getObject(
        bucket: String,
        `object`: String,
        offset: Long
    ): GetObjectResponse? {
        return client?.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(`object`)
                .offset(offset)
                .build()
        )
    }

    fun composeObject(
        bucket: String,
        sources: List<ComposeSource>,
        `object`: String
    ): ObjectWriteResponse? {
        return client?.composeObject(
            ComposeObjectArgs.builder()
                .bucket(bucket)
                .sources(sources)
                .`object`(`object`)
                .build()
        )
    }

    fun upload(inputStream: InputStream, filename: String): String? {
        if (!checkOrCreateBucket("electro")) {
            throw Exception("未创建bucket")
        }
        return client?.putObject(
            PutObjectArgs.builder()
                .bucket("electro")
                .stream(inputStream, -1, 1_073_741_824)
                .`object`(filename)
                .build()
        )?.`object`()
    }

    fun download(filename: String): GetObjectResponse? {
        return client?.getObject(
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