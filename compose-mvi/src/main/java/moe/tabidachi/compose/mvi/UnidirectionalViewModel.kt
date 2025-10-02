package moe.tabidachi.compose.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

public interface UnidirectionalViewModel<STATE, EVENT, EFFECT> {
    public val state: StateFlow<STATE>
    public val effect: SharedFlow<EFFECT>
    public fun event(event: EVENT)

    @Composable
    public operator fun component1(): State<STATE> = state.collectAsStateWithLifecycle()
    public operator fun component2(): (EVENT) -> Unit = ::event
}

@Composable
public inline fun <reified STATE, EVENT, EFFECT> UnidirectionalViewModel<STATE, EVENT, EFFECT>.observe(
    crossinline handleEffect: (EFFECT) -> Unit
): UnidirectionalViewModel<STATE, EVENT, EFFECT> = apply {
    LaunchedEffect(key1 = effect) {
        effect.collect {
            handleEffect(it)
        }
    }
}
