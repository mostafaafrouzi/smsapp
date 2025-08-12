package com.autoconnect.sms.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.autoconnect.sms.R
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.databinding.ActivitySettingsBinding
import com.autoconnect.sms.viewmodel.SettingsViewModel
import com.autoconnect.sms.viewmodel.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this)
        val preferencesManager = PreferencesManager(this)
        val factory = SettingsViewModelFactory(preferencesManager, database)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
    }
    
    private fun setupUI() {
        // Setup switches
        binding.smsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSmsEnabled(isChecked)
        }
        
        binding.whatsappSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateWhatsAppEnabled(isChecked)
        }
        
        binding.telegramSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTelegramEnabled(isChecked)
        }
        
        // Setup slider defaults and listener (moved attrs from XML to code)
        binding.dedupSlider.valueFrom = 1f
        binding.dedupSlider.valueTo = 168f
        binding.dedupSlider.stepSize = 1f
        binding.dedupSlider.value = 24f
        binding.dedupSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.updateDedupHours(value.toInt())
                updateDedupValueText(value.toInt())
            }
        }
        
        // Setup message inputs
        binding.incomingMessageEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateMessageIncoming(binding.incomingMessageEdit.text.toString())
            }
        }
        
        binding.outgoingMessageEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateMessageOutgoing(binding.outgoingMessageEdit.text.toString())
            }
        }
        
        binding.missedMessageEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateMessageMissed(binding.missedMessageEdit.text.toString())
            }
        }
        
        // Setup API key inputs
        binding.whatsappApiKeyEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateWhatsAppApiKey(binding.whatsappApiKeyEdit.text.toString())
            }
        }
        
        binding.telegramBotTokenEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateTelegramBotToken(binding.telegramBotTokenEdit.text.toString())
            }
        }
        
        binding.telegramChatIdEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateTelegramChatId(binding.telegramChatIdEdit.text.toString())
            }
        }
        
        // Setup save button
        binding.saveButton.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.settings.collect { settings ->
            // Update UI with current settings
            binding.smsSwitch.isChecked = settings.isSmsEnabled
            binding.whatsappSwitch.isChecked = settings.isWhatsAppEnabled
            binding.telegramSwitch.isChecked = settings.isTelegramEnabled
            
            val clamped = settings.dedupHours.coerceIn(1, 168)
            binding.dedupSlider.value = clamped.toFloat()
            updateDedupValueText(clamped)
            
            binding.incomingMessageEdit.setText(settings.messageIncoming)
            binding.outgoingMessageEdit.setText(settings.messageOutgoing)
            binding.missedMessageEdit.setText(settings.messageMissed)
            
            binding.whatsappApiKeyEdit.setText(settings.whatsappApiKey)
            binding.telegramBotTokenEdit.setText(settings.telegramBotToken)
            binding.telegramChatIdEdit.setText(settings.telegramChatId)
            }
        }
        
        lifecycleScope.launchWhenStarted {
            viewModel.saveResult.collect { success ->
                when (success) {
                    true -> Toast.makeText(this@SettingsActivity, R.string.success, Toast.LENGTH_SHORT).show()
                    false -> Toast.makeText(this@SettingsActivity, R.string.error, Toast.LENGTH_SHORT).show()
                    null -> Unit
                }
            }
        }
    }
    
    private fun updateDedupValueText(hours: Int) {
        val text = when {
            hours == 1 -> "1 hour"
            hours < 24 -> "$hours hours"
            hours == 24 -> "1 day"
            hours < 168 -> "${hours / 24} days"
            hours == 168 -> "1 week"
            else -> "${hours / 168} weeks"
        }
        binding.dedupValueText.text = text
    }
    
    private fun saveSettings() {
        // Save all current values
        viewModel.updateMessageIncoming(binding.incomingMessageEdit.text.toString())
        viewModel.updateMessageOutgoing(binding.outgoingMessageEdit.text.toString())
        viewModel.updateMessageMissed(binding.missedMessageEdit.text.toString())
        viewModel.updateWhatsAppApiKey(binding.whatsappApiKeyEdit.text.toString())
        viewModel.updateTelegramBotToken(binding.telegramBotTokenEdit.text.toString())
        viewModel.updateTelegramChatId(binding.telegramChatIdEdit.text.toString())
        
        Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show()
    }
}
