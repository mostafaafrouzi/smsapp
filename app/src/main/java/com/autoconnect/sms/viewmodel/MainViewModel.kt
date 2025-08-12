package com.autoconnect.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.model.MessageStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModel() {

    // Expose settings as StateFlow directly
    val settings = preferencesManager.settings

    val messagesThisWeek = MutableStateFlow(0)
    val lastSuccessfulMessage = MutableStateFlow<String?>(null)
    val lastFailedMessage = MutableStateFlow<String?>(null)

    init {
        loadStatistics()
    }

    fun updateAppEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateAppEnabled(enabled)
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Calculate date range for this week
                val calendar = Calendar.getInstance()
                val endDate = calendar.time

                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val startDate = calendar.time

                // Get message count for this week
                val count = database.callLogDao().getMessageCountInDateRange(startDate, endDate)
                messagesThisWeek.value = count

                // Collect last successful and failed messages in separate coroutines
                launch {
                    database.callLogDao().getLastMessageByStatus(MessageStatus.SUCCESS)
                        .collect { message ->
                            lastSuccessfulMessage.value = message?.let {
                                "${it.phoneNumber} - ${it.timestamp}"
                            }
                        }
                }
                launch {
                    database.callLogDao().getLastMessageByStatus(MessageStatus.FAILED)
                        .collect { message ->
                            lastFailedMessage.value = message?.let {
                                "${it.phoneNumber} - ${it.timestamp}"
                            }
                        }
                }
            } catch (e: Exception) {
                // swallow
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
}
