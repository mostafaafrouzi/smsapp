package com.autoconnect.sms.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.autoconnect.sms.data.model.AppSettings
import com.autoconnect.sms.data.model.AutoCleanupType
import com.autoconnect.sms.data.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    companion object {
        private const val PREFS_NAME = "autoconnect_sms_prefs"
        
        // Keys
        private const val KEY_APP_ENABLED = "app_enabled"
        private const val KEY_SMS_ENABLED = "sms_enabled"
        private const val KEY_WHATSAPP_ENABLED = "whatsapp_enabled"
        private const val KEY_TELEGRAM_ENABLED = "telegram_enabled"
        private const val KEY_DEDUP_HOURS = "dedup_hours"
        private const val KEY_AUTO_CLEANUP = "auto_cleanup"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_MESSAGE_INCOMING = "message_incoming"
        private const val KEY_MESSAGE_OUTGOING = "message_outgoing"
        private const val KEY_MESSAGE_MISSED = "message_missed"
        private const val KEY_WHATSAPP_API_KEY = "whatsapp_api_key"
        private const val KEY_TELEGRAM_BOT_TOKEN = "telegram_bot_token"
        private const val KEY_TELEGRAM_CHAT_ID = "telegram_chat_id"
    }
    
    private fun loadSettings(): AppSettings {
        return AppSettings(
            isAppEnabled = prefs.getBoolean(KEY_APP_ENABLED, false),
            isSmsEnabled = prefs.getBoolean(KEY_SMS_ENABLED, true),
            isWhatsAppEnabled = prefs.getBoolean(KEY_WHATSAPP_ENABLED, false),
            isTelegramEnabled = prefs.getBoolean(KEY_TELEGRAM_ENABLED, false),
            dedupHours = prefs.getInt(KEY_DEDUP_HOURS, 24),
            autoCleanup = AutoCleanupType.valueOf(
                prefs.getString(KEY_AUTO_CLEANUP, AutoCleanupType.DAILY.name) ?: AutoCleanupType.DAILY.name
            ),
            language = Language.valueOf(
                prefs.getString(KEY_LANGUAGE, Language.ENGLISH.name) ?: Language.ENGLISH.name
            ),
            messageIncoming = prefs.getString(KEY_MESSAGE_INCOMING, "") ?: "",
            messageOutgoing = prefs.getString(KEY_MESSAGE_OUTGOING, "") ?: "",
            messageMissed = prefs.getString(KEY_MESSAGE_MISSED, "") ?: "",
            whatsappApiKey = prefs.getString(KEY_WHATSAPP_API_KEY, "") ?: "",
            telegramBotToken = prefs.getString(KEY_TELEGRAM_BOT_TOKEN, "") ?: "",
            telegramChatId = prefs.getString(KEY_TELEGRAM_CHAT_ID, "") ?: ""
        )
    }
    
    suspend fun updateSettings(settings: AppSettings) {
        prefs.edit {
            putBoolean(KEY_APP_ENABLED, settings.isAppEnabled)
            putBoolean(KEY_SMS_ENABLED, settings.isSmsEnabled)
            putBoolean(KEY_WHATSAPP_ENABLED, settings.isWhatsAppEnabled)
            putBoolean(KEY_TELEGRAM_ENABLED, settings.isTelegramEnabled)
            putInt(KEY_DEDUP_HOURS, settings.dedupHours)
            putString(KEY_AUTO_CLEANUP, settings.autoCleanup.name)
            putString(KEY_LANGUAGE, settings.language.name)
            putString(KEY_MESSAGE_INCOMING, settings.messageIncoming)
            putString(KEY_MESSAGE_OUTGOING, settings.messageOutgoing)
            putString(KEY_MESSAGE_MISSED, settings.messageMissed)
            putString(KEY_WHATSAPP_API_KEY, settings.whatsappApiKey)
            putString(KEY_TELEGRAM_BOT_TOKEN, settings.telegramBotToken)
            putString(KEY_TELEGRAM_CHAT_ID, settings.telegramChatId)
        }
        _settings.value = settings
    }
    
    suspend fun updateAppEnabled(enabled: Boolean) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(isAppEnabled = enabled))
    }
    
    suspend fun updateSmsEnabled(enabled: Boolean) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(isSmsEnabled = enabled))
    }
    
    suspend fun updateWhatsAppEnabled(enabled: Boolean) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(isWhatsAppEnabled = enabled))
    }
    
    suspend fun updateTelegramEnabled(enabled: Boolean) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(isTelegramEnabled = enabled))
    }
    
    suspend fun updateDedupHours(hours: Int) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(dedupHours = hours))
    }
    
    suspend fun updateAutoCleanup(type: AutoCleanupType) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(autoCleanup = type))
    }
    
    suspend fun updateLanguage(language: Language) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(language = language))
    }
    
    suspend fun updateMessageIncoming(message: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(messageIncoming = message))
    }
    
    suspend fun updateMessageOutgoing(message: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(messageOutgoing = message))
    }
    
    suspend fun updateMessageMissed(message: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(messageMissed = message))
    }
    
    suspend fun updateWhatsAppApiKey(apiKey: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(whatsappApiKey = apiKey))
    }
    
    suspend fun updateTelegramBotToken(token: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(telegramBotToken = token))
    }
    
    suspend fun updateTelegramChatId(chatId: String) {
        val currentSettings = _settings.value
        updateSettings(currentSettings.copy(telegramChatId = chatId))
    }
}
