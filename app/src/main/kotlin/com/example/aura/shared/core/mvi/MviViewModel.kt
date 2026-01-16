package com.example.aura.shared.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Robust Base Class for MVI ViewModels.
 *
 * Rules:
 * 1. State (S) must be an Immutable Data Class.
 * 2. reduce() must be pure, fast, and synchronous (no IO/Database calls).
 * 3. Side effects (API calls) should be launched in sendIntent() or init block,
 *    then dispatch results via new Intents.
 */
abstract class MviViewModel<S, I, E>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected val currentState: S
        get() = _state.value

    private val _intents = Channel<I>(
        capacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        viewModelScope.launch {
            _intents.receiveAsFlow().collect { intent ->
                try {
                    val currentState = _state.value
                    val (newState, newEffect) = reduce(currentState, intent)

                    if (newState != currentState) {
                        _state.value = newState
                    }

                    if (newEffect != null) {
                        _effect.trySend(newEffect)
                            .onFailure {
                                onEffectDropped(newEffect, it)
                            }
                    }
                } catch (e: Throwable) {
                    onReducerError(e)
                }
            }
        }
    }

    /**
     * Core logic. Must be Pure and Fast.
     * Do NOT launch coroutines or perform IO here.
     */
    protected abstract fun reduce(currentState: S, intent: I): Pair<S, E?>

    /**
     * Optional: Override to handle reducer crashes (e.g. Log to Crashlytics)
     */
    protected open fun onReducerError(throwable: Throwable?) {
        throwable?.printStackTrace()
    }

    protected open fun onEffectDropped(effect: E, cause: Throwable?) {
        cause?.printStackTrace()
    }

    fun sendIntent(intent: I) {
        _intents.trySend(intent)
            .onFailure {
                onReducerError(it)
            }
    }

    protected fun S.only(): Pair<S, E?> = this to null
    protected fun S.with(effect: E): Pair<S, E?> = this to effect
}