package com.autoconnect.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.CallLogItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LogsViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModel() {
    
    private val _logs = MutableStateFlow<List<CallLogItem>>(emptyList())
    val logs: StateFlow<List<CallLogItem>> = _logs.asStateFlow()

    private val _exportResult = MutableStateFlow<Boolean?>(null)
    val exportResult: StateFlow<Boolean?> = _exportResult.asStateFlow()

    private val _clearResult = MutableStateFlow<Boolean?>(null)
    val clearResult: StateFlow<Boolean?> = _clearResult.asStateFlow()
    
    private var currentFilter: CallType? = null
    
    init {
        loadLogs()
    }
    
    fun setFilter(callType: CallType?) {
        currentFilter = callType
        loadLogs()
    }
    
    private fun loadLogs() {
        viewModelScope.launch {
            try {
                val flow = when (currentFilter) {
                    null -> database.callLogDao().getAllCallLogs()
                    else -> database.callLogDao().getCallLogsByType(currentFilter!!)
                }
                flow.collect { list -> _logs.value = list }
            } catch (e: Exception) {
                _logs.value = emptyList()
            }
        }
    }
    
    fun exportCsv() {
        viewModelScope.launch {
            try {
                // TODO: Implement CSV export functionality
                _exportResult.value = true
            } catch (e: Exception) {
                _exportResult.value = false
            }
        }
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            try {
                database.callLogDao().clearAllLogs()
                loadLogs()
                _clearResult.value = true
            } catch (e: Exception) {
                _clearResult.value = false
            }
        }
    }
}
