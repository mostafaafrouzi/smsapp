package com.autoconnect.sms.data.db

import androidx.room.TypeConverter
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.MessageChannel
import com.autoconnect.sms.data.model.MessageStatus
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromCallType(value: CallType): String {
        return value.name
    }
    
    @TypeConverter
    fun toCallType(value: String): CallType {
        return try {
            CallType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CallType.UNKNOWN
        }
    }
    
    @TypeConverter
    fun fromMessageChannel(value: MessageChannel): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageChannel(value: String): MessageChannel {
        return try {
            MessageChannel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageChannel.SMS
        }
    }
    
    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus {
        return try {
            MessageStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageStatus.PENDING
        }
    }
}
