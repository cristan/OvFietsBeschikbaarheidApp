package nl.ovfietsbeschikbaarheid.state

import androidx.compose.runtime.MutableState

sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()

    data class Loaded<T>(
        val data: T,
        val isRefreshing: Boolean = false
    ) : ScreenState<T>()

    data object FullPageError : ScreenState<Nothing>()
}

fun <T> MutableState<ScreenState<T>>.setRefreshing() {
    val loadedState = value as ScreenState.Loaded
    value = loadedState.copy(isRefreshing = true)
}