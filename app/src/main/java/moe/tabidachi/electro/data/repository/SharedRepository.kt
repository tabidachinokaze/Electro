package moe.tabidachi.electro.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SharedRepository {
    val state: StateFlow<SharedState>
    fun updateState(block: (SharedState) -> SharedState)
}

data class SharedState(
    val baseUrl: String = "http://localhost:23333",
    val tokens: Map<Long, String> = emptyMap(),
    val currentUserId: Long = 0,
    val webSocketPort: Int = 23333,
    val minioUrl: String = "http://localhost:9000",
    val accessKey: String = "",
    val secretKey: String = ""
)

class SharedRepositoryImpl() : SharedRepository {
    private val _state = MutableStateFlow(SharedState())
    override val state: StateFlow<SharedState> = _state.asStateFlow()

    override fun updateState(block: (SharedState) -> SharedState) {
        _state.update(block)
    }
}
