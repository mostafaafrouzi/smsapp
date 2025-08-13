package com.autoconnect.sms.core.telegram

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

class TelegramSender(private val context: Context) {
    
    companion object {
        private const val TAG = "TelegramSender"
        private const val BASE_URL = "https://api.telegram.org/bot"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    suspend fun sendTelegramMessage(
        phoneNumber: String,
        message: String,
        callType: CallType,
        botToken: String,
        chatId: String
    ): CallLogItem = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending Telegram message to chat $chatId: $message")
            
            if (botToken.isBlank()) {
                throw IllegalArgumentException("Telegram bot token is required")
            }
            
            if (chatId.isBlank()) {
                throw IllegalArgumentException("Telegram chat ID is required")
            }
            
            val jsonBody = JSONObject().apply {
                put("chat_id", chatId)
                put("text", "📞 Call from $phoneNumber ($callType):\n$message")
                put("parse_mode", "HTML")
            }.toString()
            
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$BASE_URL$botToken/sendMessage")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Telegram message sent successfully to chat $chatId. Response: $responseBody")
                
                CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.TELEGRAM,
                    status = MessageStatus.SUCCESS
                )
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Telegram API error: ${response.code} - $errorBody")
                
                CallLogItem(
                    phoneNumber = phoneNumber,
                    callType = callType,
                    timestamp = Date(),
                    message = message,
                    channel = MessageChannel.TELEGRAM,
                    status = MessageStatus.FAILED,
                    errorMessage = "API Error: ${response.code} - $errorBody"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send Telegram message to chat $chatId", e)
            
            CallLogItem(
                phoneNumber = phoneNumber,
                callType = callType,
                timestamp = Date(),
                message = message,
                channel = MessageChannel.TELEGRAM,
                status = MessageStatus.FAILED,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
    
    fun isTelegramSupported(): Boolean {
        return try {
            // Check if we have internet permission and can make network calls
            context.packageManager.checkPermission(
                android.Manifest.permission.INTERNET,
                context.packageName
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Telegram not supported", e)
            false
        }
    }
}
