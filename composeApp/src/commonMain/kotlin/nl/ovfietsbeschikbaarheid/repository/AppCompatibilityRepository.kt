package nl.ovfietsbeschikbaarheid.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val APP_COMPATIBILITY = 2

class AppCompatibilityRepository {
    private val updateRequired = MutableStateFlow(false)
    val isUpdateRequired: StateFlow<Boolean> = updateRequired.asStateFlow()

    fun markUpdateRequired() {
        updateRequired.value = true
    }
}
