package com.autoconnect.sms.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "call_logs")
data class CallLogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val callType: CallType,
    val timestamp: Date,
    val message: String,
    val channel: MessageChannel,
    val status: MessageStatus,
    val errorMessage: String? = null
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    UNKNOWN
}

enum class MessageChannel {
    SMS,
    WHATSAPP,
    TELEGRAM
}

enum class MessageStatus {
    SUCCESS,
    FAILED,
    PENDING
}
