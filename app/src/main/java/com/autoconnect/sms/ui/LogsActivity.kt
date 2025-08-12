package com.autoconnect.sms.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoconnect.sms.R
import com.autoconnect.sms.data.db.AppDatabase
import com.autoconnect.sms.data.prefs.PreferencesManager
import com.autoconnect.sms.databinding.ActivityLogsBinding
import com.autoconnect.sms.viewmodel.LogsViewModel
import com.autoconnect.sms.viewmodel.LogsViewModelFactory
import com.autoconnect.sms.data.model.CallType

class LogsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLogsBinding
    private lateinit var viewModel: LogsViewModel
    private lateinit var logsAdapter: LogsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupRecyclerView()
        setupUI()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this)
        val preferencesManager = PreferencesManager(this)
        val factory = LogsViewModelFactory(preferencesManager, database)
        viewModel = ViewModelProvider(this, factory)[LogsViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        logsAdapter = LogsAdapter()
        binding.logsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LogsActivity)
            adapter = logsAdapter
        }
    }
    
    private fun setupUI() {
        // Setup filter chips
        binding.filterAllChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(null)
        }
        
        binding.filterIncomingChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(CallType.INCOMING)
        }
        
        binding.filterOutgoingChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(CallType.OUTGOING)
        }
        
        binding.filterMissedChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilter(CallType.MISSED)
        }
        
        // Setup action buttons
        binding.exportCsvButton.setOnClickListener {
            exportCsv()
        }
        
        binding.clearLogsButton.setOnClickListener {
            showClearLogsDialog()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.logs.collect { logs ->
                if (logs.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.logsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.logsRecyclerView.visibility = View.VISIBLE
                    logsAdapter.submitList(logs)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.exportResult.collect { success ->
                success?.let {
                    if (it) Toast.makeText(this@LogsActivity, "CSV exported successfully", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this@LogsActivity, "Failed to export CSV", Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.clearResult.collect { success ->
                success?.let {
                    if (it) Toast.makeText(this@LogsActivity, "Logs cleared successfully", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this@LogsActivity, "Failed to clear logs", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun exportCsv() {
        viewModel.exportCsv()
    }
    
    private fun showClearLogsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Logs")
            .setMessage("Are you sure you want to clear all logs? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.clearLogs()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
