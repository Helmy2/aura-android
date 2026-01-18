package com.example.aura.feature.favorites

sealed class FavoritesSideEffect {
    data class ShowSnackbar(val message: String) : FavoritesSideEffect()
}
