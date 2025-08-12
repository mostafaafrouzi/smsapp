package com.autoconnect.sms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModel() {
    
    val settings = preferencesManager.settings
    
    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()
    
    fun updateSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updateSmsEnabled(enabled)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateWhatsAppEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updateWhatsAppEnabled(enabled)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateTelegramEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updateTelegramEnabled(enabled)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateDedupHours(hours: Int) {
        viewModelScope.launch {
            try {
                preferencesManager.updateDedupHours(hours)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateMessageIncoming(message: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateMessageIncoming(message)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateMessageOutgoing(message: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateMessageOutgoing(message)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateMessageMissed(message: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateMessageMissed(message)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateWhatsAppApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateWhatsAppApiKey(apiKey)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateTelegramBotToken(token: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateTelegramBotToken(token)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun updateTelegramChatId(chatId: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateTelegramChatId(chatId)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
    
    fun clearSaveResult() {
        _saveResult.value = null
    }
}
