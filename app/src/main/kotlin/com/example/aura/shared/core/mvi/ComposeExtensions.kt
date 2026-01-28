package com.example.aura.shared.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectAsState(): State<STATE> {
    return container.stateFlow.collectAsState()
}

@Composable
fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.CollectSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    sideEffect: suspend (SIDE_EFFECT) -> Unit
) {
    val sideEffectFlow = container.sideEffectFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentSideEffect by rememberUpdatedState(sideEffect)

    LaunchedEffect(sideEffectFlow, lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sideEffectFlow.collect { effect ->
                withContext(Dispatchers.Main.immediate) {
                    currentSideEffect(effect)
                }
            }
        }
    }
}
