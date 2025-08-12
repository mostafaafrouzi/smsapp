package com.autoconnect.sms.data.db

import androidx.room.*
import com.autoconnect.sms.data.model.CallLogItem
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CallLogDao {
    
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogItem>>
    
    @Query("SELECT * FROM call_logs WHERE callType = :callType ORDER BY timestamp DESC")
    fun getCallLogsByType(callType: CallType): Flow<List<CallLogItem>>
    
    @Query("SELECT * FROM call_logs WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getCallLogsByDateRange(startDate: Date, endDate: Date): Flow<List<CallLogItem>>
    
    @Query("SELECT * FROM call_logs WHERE callType = :callType AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getCallLogsByTypeAndDateRange(callType: CallType, startDate: Date, endDate: Date): Flow<List<CallLogItem>>
    
    @Query("SELECT COUNT(*) FROM call_logs WHERE status = :status AND timestamp BETWEEN :startDate AND :endDate")
    fun getMessageCountByStatusAndDateRange(status: MessageStatus, startDate: Date, endDate: Date): Flow<Int>
    
    @Query("SELECT * FROM call_logs WHERE status = :status ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessageByStatus(status: MessageStatus): Flow<CallLogItem?>
    
    @Query("SELECT * FROM call_logs WHERE phoneNumber = :phoneNumber AND timestamp > :since ORDER BY timestamp DESC")
    fun getRecentMessagesForNumber(phoneNumber: String, since: Date): Flow<List<CallLogItem>>
    
    @Insert
    suspend fun insertCallLog(callLog: CallLogItem)
    
    @Update
    suspend fun updateCallLog(callLog: CallLogItem)
    
    @Delete
    suspend fun deleteCallLog(callLog: CallLogItem)
    
    @Query("DELETE FROM call_logs WHERE timestamp < :beforeDate")
    suspend fun deleteOldLogs(beforeDate: Date)
    
    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs()
    
    @Query("SELECT COUNT(*) FROM call_logs WHERE timestamp BETWEEN :startDate AND :endDate")
    suspend fun getMessageCountInDateRange(startDate: Date, endDate: Date): Int
}
