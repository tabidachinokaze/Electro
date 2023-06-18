package cn.tabidachi.electro.ui.imagepreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ImagePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<ImagePreviewState> =
        MutableStateFlow(ImagePreviewState())

    val stateFlow: StateFlow<ImagePreviewState> = _stateFlow.asStateFlow()
    init {

    }

    fun update(block: ImagePreviewState.() -> ImagePreviewState) {
        _stateFlow.value = _stateFlow.value.block()
    }

    fun init() {
        viewModelScope.launch {
            update {
                copy()
            }
            update {
                copy()
            }
        }
    }
}