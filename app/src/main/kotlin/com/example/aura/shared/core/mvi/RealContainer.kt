package com.example.aura.shared.core.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    val settings: ContainerSettings
) : Container<STATE, SIDE_EFFECT> {

    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<STATE> = _stateFlow.asStateFlow()

    private val _sideEffectChannel = Channel<SIDE_EFFECT>(Channel.BUFFERED)
    override val sideEffectFlow: Flow<SIDE_EFFECT> = _sideEffectChannel.receiveAsFlow()

    private val mutex = Mutex()

    suspend fun reduce(reducer: STATE.() -> STATE) {
        mutex.withLock {
            _stateFlow.value = _stateFlow.value.reducer()
        }
    }

    suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        _sideEffectChannel.send(sideEffect)
    }

    val state: STATE
        get() = _stateFlow.value
}

data class ContainerSettings(
    val exceptionHandler: ((Throwable) -> Unit)? = null
)
