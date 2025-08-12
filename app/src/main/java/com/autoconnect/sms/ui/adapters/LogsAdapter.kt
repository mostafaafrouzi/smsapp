package com.autoconnect.sms.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autoconnect.sms.R
import com.autoconnect.sms.data.model.CallLogItem
import com.autoconnect.sms.data.model.CallType
import com.autoconnect.sms.data.model.MessageChannel
import com.autoconnect.sms.data.model.MessageStatus
import com.autoconnect.sms.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.*

class LogsAdapter : ListAdapter<CallLogItem, LogsAdapter.LogViewHolder>(LogDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        
        fun bind(log: CallLogItem) {
            binding.apply {
                phoneNumberText.text = log.phoneNumber
                callTypeText.text = getCallTypeText(log.callType)
                messageText.text = log.message
                channelText.text = getChannelText(log.channel)
                statusText.text = getStatusText(log.status)
                timestampText.text = dateFormat.format(log.timestamp)
                
                // Set status color
                val statusColor = when (log.status) {
                    MessageStatus.SUCCESS -> R.color.success
                    MessageStatus.FAILED -> R.color.error
                    MessageStatus.PENDING -> R.color.warning
                }
                statusText.setTextColor(itemView.context.getColor(statusColor))
            }
        }
        
        private fun getCallTypeText(callType: CallType): String {
            return when (callType) {
                CallType.INCOMING -> itemView.context.getString(R.string.call_type_incoming)
                CallType.OUTGOING -> itemView.context.getString(R.string.call_type_outgoing)
                CallType.MISSED -> itemView.context.getString(R.string.call_type_missed)
                CallType.UNKNOWN -> itemView.context.getString(R.string.call_type_unknown)
            }
        }
        
        private fun getChannelText(channel: MessageChannel): String {
            return when (channel) {
                MessageChannel.SMS -> itemView.context.getString(R.string.channel_sms)
                MessageChannel.WHATSAPP -> itemView.context.getString(R.string.channel_whatsapp)
                MessageChannel.TELEGRAM -> itemView.context.getString(R.string.channel_telegram)
            }
        }
        
        private fun getStatusText(status: MessageStatus): String {
            return when (status) {
                MessageStatus.SUCCESS -> itemView.context.getString(R.string.status_success)
                MessageStatus.FAILED -> itemView.context.getString(R.string.status_failed)
                MessageStatus.PENDING -> itemView.context.getString(R.string.status_pending)
            }
        }
    }
    
    private class LogDiffCallback : DiffUtil.ItemCallback<CallLogItem>() {
        override fun areItemsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: CallLogItem, newItem: CallLogItem): Boolean {
            return oldItem == newItem
        }
    }
}
