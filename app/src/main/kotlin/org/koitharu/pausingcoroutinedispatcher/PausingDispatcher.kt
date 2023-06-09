package org.koitharu.pausingcoroutinedispatcher

import kotlinx.coroutines.CoroutineDispatcher

abstract class PausingDispatcher internal constructor() : CoroutineDispatcher() {

    override fun toString(): String {
        return "PausingDispatcher"
    }
}