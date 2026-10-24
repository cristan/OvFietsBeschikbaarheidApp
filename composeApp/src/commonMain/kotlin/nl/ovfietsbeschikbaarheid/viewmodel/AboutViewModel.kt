package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.repository.UsageStatisticsSettingRepository

class AboutViewModel(
    private val usageStatisticsSettingRepository: UsageStatisticsSettingRepository
) : ViewModel() {

    val isUsageStatisticsEnabled: Flow<Boolean> = usageStatisticsSettingRepository.isEnabled

    fun onUsageStatisticsSwitched(isEnabled: Boolean) {
        viewModelScope.launch {
            usageStatisticsSettingRepository.setEnabled(isEnabled)
        }
    }
}
