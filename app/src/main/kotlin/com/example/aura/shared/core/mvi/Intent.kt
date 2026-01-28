package com.example.aura.shared.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ContainerContext<STATE : Any, SIDE_EFFECT : Any>  internal constructor(
    private val container: RealContainer<STATE, SIDE_EFFECT>
) {
    val state: STATE
        get() = container.state

    suspend fun reduce(reducer: STATE.() -> STATE) {
        container.reduce(reducer)
    }

    suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        container.postSideEffect(sideEffect)
    }
}

fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.intent(
    transformer: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
) {
    val container = container as RealContainer
    val scope = if (this is ViewModel) viewModelScope else error("Scope required")

    scope.launch {
        try {
            ContainerContext(container).transformer()
        } catch (e: Exception) {
            container.settings.exceptionHandler?.invoke(e) ?: throw e
        }
    }
}
