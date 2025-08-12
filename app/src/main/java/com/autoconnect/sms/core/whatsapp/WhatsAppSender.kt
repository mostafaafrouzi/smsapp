package com.autoconnect.sms.core.whatsapp

import android.content.Context
import android.util.Log
import com.autoconnect.sms.data.model.CallLogItem
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.MessageChannel
import com.autoconnect.sms.data.model.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Date
import java.util.concurrent.TimeUnit

class WhatsAppSender(private val context: Context) {
    
    companion object {
        private const val TAG = "WhatsAppSender"
        private const val BASE_URL = "https://api.360messenger.com/v1"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    suspend fun sendWhatsAppMessage(
        phoneNumber: String,
        message: String,
        callType: CallType,
        apiKey: String
    ): CallLogItem = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending WhatsApp message to $phoneNumber: $message")
            
            if (apiKey.isBlank()) {
                throw IllegalArgumentException("WhatsApp API key is required")
            }
            
            val jsonBody = JSONObject().apply {
                put("to", phoneNumber)
                put("type", "text")
                put("text", JSONObject().put("body", message))
            }.toString()
            
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$BASE_URL/messages")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "WhatsApp message sent successfully to $phoneNumber. Response: $responseBody")
                
                CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.WHATSAPP,
                    status = MessageStatus.SUCCESS
                )
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "WhatsApp API error: ${response.code} - $errorBody")
                
                CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.WHATSAPP,
                    status = MessageStatus.FAILED,
                    errorMessage = "API Error: ${response.code} - $errorBody"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send WhatsApp message to $phoneNumber", e)
            
            CallLogItem(
                phoneNumber = phoneNumber,
                callType = callType,
                timestamp = Date(),
                message = message,
                channel = MessageChannel.WHATSAPP,
                status = MessageStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
    
    fun isWhatsAppSupported(): Boolean {
        return try {
            // Check if we have internet permission and can make network calls
            context.packageManager.checkPermission(
                android.Manifest.permission.INTERNET,
                context.packageName
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp not supported", e)
            false
        }
    }
}
