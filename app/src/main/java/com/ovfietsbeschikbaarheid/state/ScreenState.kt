package com.ovfietsbeschikbaarheid.state

import kotlinx.coroutines.flow.MutableStateFlow

sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()

    data class Loaded<T>(
        val data: T,
        val isRefreshing: Boolean = false
    ) : ScreenState<T>()

    data object FullPageError : ScreenState<Nothing>()
}

fun <T> MutableStateFlow<ScreenState<T>>.setRefreshing() {
    val loadedState = value as ScreenState.Loaded
    value = loadedState.copy(isRefreshing = true)
}