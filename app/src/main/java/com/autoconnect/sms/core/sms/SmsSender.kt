package com.autoconnect.sms.core.sms

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.autoconnect.sms.data.model.CallLogItem
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.MessageChannel
import com.autoconnect.sms.data.model.MessageStatus
import java.util.Date

class SmsSender(private val context: Context) {
    
    companion object {
        private const val TAG = "SmsSender"
    }
    
    suspend fun sendSms(
        phoneNumber: String,
        message: String,
        callType: CallType
    ): CallLogItem {
        return try {
            Log.d(TAG, "Sending SMS to $phoneNumber: $message")
            
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            
            if (parts.size > 1) {
                // Send multipart SMS
                val sentIntents = ArrayList<android.content.Intent>()
                val deliveredIntents = ArrayList<android.content.Intent>()
                
                for (i in parts.indices) {
                    val sentIntent = android.content.Intent("SMS_SENT")
                    val deliveredIntent = android.content.Intent("SMS_DELIVERED")
                    
                    sentIntents.add(sentIntent)
                    deliveredIntents.add(deliveredIntent)
                }
                
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    sentIntents,
                    deliveredIntents
                )
            } else {
                // Send single SMS
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }
            
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            
            CallLogItem(
                phoneNumber = phoneNumber,
                callType = callType,
                timestamp = Date(),
                message = message,
                channel = MessageChannel.SMS,
                status = MessageStatus.SUCCESS
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            
            CallLogItem(
                phoneNumber = phoneNumber,
                callType = callType,
                timestamp = Date(),
                message = message,
                channel = MessageChannel.SMS,
                status = MessageStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
    
    fun isSmsSupported(): Boolean {
        return try {
            SmsManager.getDefault() != null
        } catch (e: Exception) {
            Log.e(TAG, "SMS not supported", e)
            false
        }
    }
}
