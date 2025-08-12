package com.autoconnect.sms.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.autoconnect.sms.R
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.databinding.ActivityMainBinding
import com.autoconnect.sms.viewmodel.MainViewModel
import com.autoconnect.sms.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SEND_SMS,
        Manifest.permission.POST_NOTIFICATIONS
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            setupApp()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        checkPermissions()
        setupUI()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this)
        val preferencesManager = PreferencesManager(this)
        val factory = MainViewModelFactory(preferencesManager, database)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
    
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            setupApp()
        }
    }
    
    private fun setupApp() {
        // Check battery optimization
        checkBatteryOptimization()
    }
    
    private fun setupUI() {
        binding.mainEnableSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAppEnabled(isChecked)
        }
        
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        binding.logsButton.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.settings.collect { settings ->
                binding.mainEnableSwitch.isChecked = settings.isAppEnabled
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.messagesThisWeek.collect { count ->
                binding.messagesThisWeekText.text = count.toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.lastSuccessfulMessage.collect { message ->
                binding.lastSuccessfulText.text = message ?: getString(R.string.main_no_data)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.lastFailedMessage.collect { message ->
                binding.lastFailedText.text = message ?: getString(R.string.main_no_data)
            }
        }
    }
    
    private fun checkBatteryOptimization() {
        // This is a simplified check - in a real app you'd use PowerManager.isIgnoringBatteryOptimizations
        // For now, we'll just show a dialog suggesting the user check battery optimization
        showBatteryOptimizationDialog()
    }
    
    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.battery_optimization_title)
            .setMessage(R.string.battery_optimization_message)
            .setPositiveButton(R.string.battery_optimization_settings) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
            }
            .setNegativeButton(R.string.battery_optimization_later, null)
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_denied)
            .setPositiveButton(R.string.permission_grant) { _, _ ->
                checkPermissions()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshStatistics()
    }
}
