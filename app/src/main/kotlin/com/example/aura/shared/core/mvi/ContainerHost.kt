package com.example.aura.shared.core.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ContainerHost<STATE : Any, SIDE_EFFECT : Any> {
    val container: Container<STATE, SIDE_EFFECT>
}

interface Container<STATE : Any, SIDE_EFFECT : Any> {
    val stateFlow: StateFlow<STATE>
    val sideEffectFlow: Flow<SIDE_EFFECT>
}