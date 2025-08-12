package com.autoconnect.sms.data.model

data class AppSettings(
    val isAppEnabled: Boolean = false,
    val isSmsEnabled: Boolean = true,
    val isWhatsAppEnabled: Boolean = false,
    val isTelegramEnabled: Boolean = false,
    val dedupHours: Int = 24,
    val autoCleanup: AutoCleanupType = AutoCleanupType.DAILY,
    val language: Language = Language.ENGLISH,
    val messageIncoming: String = "",
    val messageOutgoing: String = "",
    val messageMissed: String = "",
    val whatsappApiKey: String = "",
    val telegramBotToken: String = "",
    val telegramChatId: String = ""
)

enum class AutoCleanupType {
    DAILY,
    WEEKLY,
    OFF
}

enum class Language {
    ENGLISH,
    PERSIAN
}
