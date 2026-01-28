package com.example.aura.shared.core.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for MVI-Lite Architecture.
 *
 * Usage:
 * 1. Define State as a Data Class (Immutable).
 * 2. Expose Public Functions as "Intents".
 * 3. Use [updateState] to safely modify state.
 */
abstract class StateViewModel<S>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * Atomically updates the state.
     * This is safe to call from any thread/coroutine.
     */
    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    /**
     * Optional: Helper to get current state value without .value access
     */
    protected val currentState: S
        get() = _state.value
}