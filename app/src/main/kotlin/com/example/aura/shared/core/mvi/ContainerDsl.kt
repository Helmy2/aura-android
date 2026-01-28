package com.example.aura.shared.core.mvi

import androidx.lifecycle.ViewModel

fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    settings: ContainerSettings = ContainerSettings()
): Container<STATE, SIDE_EFFECT> {
    return RealContainer(initialState, settings)
}

