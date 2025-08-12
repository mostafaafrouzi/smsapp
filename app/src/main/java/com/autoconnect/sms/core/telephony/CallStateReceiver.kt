package com.autoconnect.sms.core.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CallStateReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CallStateReceiver"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var lastPhoneNumber: String? = null
        private var callStartTime: Long = 0
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                
                Log.d(TAG, "Phone state changed: $state, Number: $phoneNumber")
                
                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // Incoming call ringing
                        lastState = TelephonyManager.CALL_STATE_RINGING
                        lastPhoneNumber = phoneNumber
                        callStartTime = System.currentTimeMillis()
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        // Call answered or outgoing call started
                        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            // Incoming call was answered
                            Log.d(TAG, "Incoming call answered: $lastPhoneNumber")
                        } else {
                            // Outgoing call
                            lastPhoneNumber = phoneNumber
                            callStartTime = System.currentTimeMillis()
                            Log.d(TAG, "Outgoing call started: $lastPhoneNumber")
                        }
                        lastState = TelephonyManager.CALL_STATE_OFFHOOK
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // Call ended
                        if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                            // Call was active and now ended
                            val callDuration = System.currentTimeMillis() - callStartTime
                            val callType = determineCallType(context, lastPhoneNumber, callDuration)
                            
                            Log.d(TAG, "Call ended: $lastPhoneNumber, Type: $callType, Duration: ${callDuration}ms")
                            
                            // Trigger automatic messaging
                            lastPhoneNumber?.let { number ->
                                triggerAutomaticMessaging(context, number, callType)
                            }
                        } else if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                            // Missed call
                            Log.d(TAG, "Missed call: $lastPhoneNumber")
                            lastPhoneNumber?.let { number ->
                                triggerAutomaticMessaging(context, number, CallType.MISSED)
                            }
                        }
                        
                        lastState = TelephonyManager.CALL_STATE_IDLE
                        lastPhoneNumber = null
                        callStartTime = 0
                    }
                }
            }
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                Log.d(TAG, "New outgoing call: $phoneNumber")
                lastPhoneNumber = phoneNumber
                callStartTime = System.currentTimeMillis()
                lastState = TelephonyManager.CALL_STATE_OFFHOOK
            }
        }
    }
    
    private fun determineCallType(context: Context, phoneNumber: String?, callDuration: Long): CallType {
        if (phoneNumber.isNullOrBlank()) return CallType.UNKNOWN
        
        // Simple logic: if call duration is very short, it might be a missed call
        // This is a basic implementation and could be enhanced with call log analysis
        return when {
            callDuration < 5000 -> CallType.MISSED // Less than 5 seconds
            lastState == TelephonyManager.CALL_STATE_RINGING -> CallType.INCOMING
            else -> CallType.OUTGOING
        }
    }
    
    private fun triggerAutomaticMessaging(context: Context, phoneNumber: String, callType: CallType) {
        val preferencesManager = PreferencesManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = preferencesManager.settings.value
                
                // Check if app is enabled
                if (!settings.isAppEnabled) {
                    Log.d(TAG, "App is disabled, skipping automatic messaging")
                    return@launch
                }
                
                // Check dedup logic
                if (shouldSkipDueToDedup(context, phoneNumber, settings.dedupHours)) {
                    Log.d(TAG, "Skipping message due to dedup logic for $phoneNumber")
                    return@launch
                }
                
                // Get message template
                val message = getMessageTemplate(settings, callType)
                
                // Send messages through enabled channels
                sendMessagesThroughChannels(context, phoneNumber, message, callType, settings)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in automatic messaging", e)
            }
        }
    }
    
    private suspend fun shouldSkipDueToDedup(context: Context, phoneNumber: String, dedupHours: Int): Boolean {
        // This is a simplified dedup check
        // In a real implementation, you would query the database for recent messages
        return false // For now, always allow messaging
    }
    
    private fun getMessageTemplate(settings: com.autoconnect.sms.data.model.AppSettings, callType: CallType): String {
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
        context: Context,
        phoneNumber: String,
        message: String,
        callType: CallType,
        settings: com.autoconnect.sms.data.model.AppSettings
    ) {
        val callDetectionManager = CallDetectionManager(context)
        callDetectionManager.handleCallEnded(phoneNumber, callType)
    }
}
