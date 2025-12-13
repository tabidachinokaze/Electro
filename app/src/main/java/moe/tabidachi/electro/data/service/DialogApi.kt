package moe.tabidachi.electro.data.service

import io.ktor.client.HttpClient
import moe.tabidachi.electro.model.response.DialogResponse
import moe.tabidachi.electro.model.response.Response
import moe.tabidachi.electro.data.provider.BaseUrlProvider

interface DialogApi {
    suspend fun dialogs(): Response<List<DialogResponse>>
    suspend fun dialog(sid: Long): Response<DialogResponse>
}

class DialogApiImpl(
    client: HttpClient,
    baseUrlProvider: BaseUrlProvider
) : DialogApi, BaseKtor(client, baseUrlProvider) {
    override suspend fun dialogs(): Response<List<DialogResponse>> {
        return get("dialogs")
    }

    override suspend fun dialog(sid: Long): Response<DialogResponse> {
        return get("dialogs", "$sid")
    }
}