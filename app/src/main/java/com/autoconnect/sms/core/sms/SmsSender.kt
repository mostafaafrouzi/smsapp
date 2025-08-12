package com.autoconnect.sms.core.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
                val sentPendingIntents = ArrayList<PendingIntent>()
                val deliveredPendingIntents = ArrayList<PendingIntent>()

                val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                for (i in parts.indices) {
                    val sentIntent = Intent("SMS_SENT")
                    val deliveredIntent = Intent("SMS_DELIVERED")

                    val sentPI = PendingIntent.getBroadcast(
                        context,
                        1000 + i,
                        sentIntent,
                        pendingIntentFlags
                    )
                    val deliveredPI = PendingIntent.getBroadcast(
                        context,
                        2000 + i,
                        deliveredIntent,
                        pendingIntentFlags
                    )

                    sentPendingIntents.add(sentPI)
                    deliveredPendingIntents.add(deliveredPI)
                }

                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    sentPendingIntents,
                    deliveredPendingIntents
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
