package com.autoconnect.sms.core.telephony

import android.content.Context
import android.util.Log
import com.autoconnect.sms.core.sms.SmsSender
import com.autoconnect.sms.core.whatsapp.WhatsAppSender
import com.autoconnect.sms.core.telegram.TelegramSender
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.model.*
import com.autoconnect.sms.data.prefs.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CallDetectionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CallDetectionManager"
    }
    
    private val preferencesManager = PreferencesManager(context)
    private val database = AppDatabase.getDatabase(context)
    private val smsSender = SmsSender(context)
    private val whatsAppSender = WhatsAppSender(context)
    private val telegramSender = TelegramSender(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    suspend fun handleCallEnded(phoneNumber: String, callType: CallType) {
        try {
            Log.d(TAG, "Handling call ended: $phoneNumber, type: $callType")
            
            val settings = preferencesManager.settings.value
            
            // Check if app is enabled
            if (!settings.isAppEnabled) {
                Log.d(TAG, "App is disabled, skipping message")
                return
            }
            
            // Check dedup logic
            if (shouldSkipDueToDedup(phoneNumber, settings.dedupHours)) {
                Log.d(TAG, "Skipping message due to dedup logic for $phoneNumber")
                return
            }
            
            // Get message template
            val message = getMessageTemplate(settings, callType)
            
            // Send messages through enabled channels
            sendMessagesThroughChannels(phoneNumber, message, callType, settings)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling call ended", e)
        }
    }
    
    private suspend fun shouldSkipDueToDedup(phoneNumber: String, dedupHours: Int): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR, -dedupHours)
            val since = calendar.time

            val recentMessages = database.callLogDao().getRecentMessagesForNumber(phoneNumber, since)
            var hasRecent = false
            recentMessages.collect { messages ->
                hasRecent = messages.isNotEmpty()
            }
            hasRecent
        } catch (e: Exception) {
            Log.e(TAG, "Error checking dedup", e)
            false
        }
    }
    
    private fun getMessageTemplate(settings: AppSettings, callType: CallType): String {
        return when (callType) {
            CallType.INCOMING -> {
                if (settings.messageIncoming.isNotBlank()) settings.messageIncoming
                else getDefaultMessage(settings.language, callType)
            }
            CallType.OUTGOING -> {
                if (settings.messageOutgoing.isNotBlank()) settings.messageOutgoing
                else getDefaultMessage(settings.language, callType)
            }
            CallType.MISSED -> {
                if (settings.messageMissed.isNotBlank()) settings.messageMissed
                else getDefaultMessage(settings.language, callType)
            }
            CallType.UNKNOWN -> getDefaultMessage(settings.language, CallType.INCOMING)
        }
    }
    
    private fun getDefaultMessage(language: Language, callType: CallType): String {
        return when (language) {
            Language.ENGLISH -> when (callType) {
                CallType.INCOMING -> "Thanks for calling!"
                CallType.OUTGOING -> "I called you earlier."
                CallType.MISSED -> "I missed your call, will call back soon."
                CallType.UNKNOWN -> "Thanks for calling!"
            }
            Language.PERSIAN -> when (callType) {
                CallType.INCOMING -> "ممنون از تماس شما!"
                CallType.OUTGOING -> "قبلاً با شما تماس گرفتم."
                CallType.MISSED -> "تماس شما را از دست دادم، به زودی تماس می‌گیرم."
                CallType.UNKNOWN -> "ممنون از تماس شما!"
            }
        }
    }
    
    private suspend fun sendMessagesThroughChannels(
        phoneNumber: String,
        message: String,
        callType: CallType,
        settings: AppSettings
    ) {
        val results = mutableListOf<CallLogItem>()
        
        // Try WhatsApp first if enabled
        if (settings.isWhatsAppEnabled && settings.whatsappApiKey.isNotBlank()) {
            try {
                val result = whatsAppSender.sendWhatsAppMessage(
                    phoneNumber, message, callType, settings.whatsappApiKey
                )
                results.add(result)
                
                if (result.status == MessageStatus.SUCCESS) {
                    Log.d(TAG, "WhatsApp message sent successfully")
                } else {
                    Log.w(TAG, "WhatsApp message failed: ${result.errorMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending WhatsApp message", e)
                results.add(CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.WHATSAPP,
                    status = MessageStatus.FAILED,
                    errorMessage = e.message ?: "Unknown error"
                ))
            }
        }
        
        // Send SMS if enabled (or as fallback)
        if (settings.isSmsEnabled) {
            try {
                val result = smsSender.sendSms(phoneNumber, message, callType)
                results.add(result)
                
                if (result.status == MessageStatus.SUCCESS) {
                    Log.d(TAG, "SMS sent successfully")
                } else {
                    Log.w(TAG, "SMS failed: ${result.errorMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending SMS", e)
                results.add(CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.SMS,
                    status = MessageStatus.FAILED,
                    errorMessage = e.message ?: "Unknown error"
                ))
            }
        }
        
        // Try Telegram if enabled
        if (settings.isTelegramEnabled && settings.telegramBotToken.isNotBlank() && settings.telegramChatId.isNotBlank()) {
            try {
                val result = telegramSender.sendTelegramMessage(
                    phoneNumber, message, callType, settings.telegramBotToken, settings.telegramChatId
                )
                results.add(result)
                
                if (result.status == MessageStatus.SUCCESS) {
                    Log.d(TAG, "Telegram message sent successfully")
                } else {
                    Log.w(TAG, "Telegram message failed: ${result.errorMessage}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending Telegram message", e)
                results.add(CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.TELEGRAM,
                    status = MessageStatus.FAILED,
                    errorMessage = e.message ?: "Unknown error"
                ))
            }
        }
        
        // Save all results to database
        scope.launch {
            try {
                results.forEach { result ->
                    database.callLogDao().insertCallLog(result)
                }
                Log.d(TAG, "Saved ${results.size} log entries to database")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving log entries", e)
            }
        }
    }
}
