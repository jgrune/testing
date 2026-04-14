package com.tvremote.buttonoverride

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : FragmentActivity() {

    private lateinit var mappingStore: MappingStore
    private lateinit var adapter: MappingAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var statusIndicator: TextView
    private lateinit var btnEnableService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mappingStore = MappingStore(this)

        statusIndicator = findViewById(R.id.status_indicator)
        btnEnableService = findViewById(R.id.btn_enable_service)
        recyclerView = findViewById(R.id.recycler_mappings)
        emptyView = findViewById(R.id.empty_view)

        adapter = MappingAdapter(
            onEdit = { mapping ->
                openAppPicker(mapping.keyCode, mapping.buttonName)
            },
            onDelete = { mapping ->
                mappingStore.removeMapping(mapping.keyCode)
                refreshMappingList()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnEnableService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btn_detect_button).setOnClickListener {
            startActivity(Intent(this, ButtonDetectActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMappingList()
        updateServiceStatus()
    }

    private fun refreshMappingList() {
        val mappings = mappingStore.getAllMappings()
        adapter.submitList(mappings)

        if (mappings.isEmpty()) {
            emptyView.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            emptyView.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE
        }
    }

    private fun updateServiceStatus() {
        val enabled = isServiceEnabled()
        if (enabled) {
            statusIndicator.text = getString(R.string.service_active)
            statusIndicator.setTextColor(getColor(R.color.status_active))
            btnEnableService.visibility = android.view.View.GONE
        } else {
            statusIndicator.text = getString(R.string.service_inactive)
            statusIndicator.setTextColor(getColor(R.color.status_inactive))
            btnEnableService.visibility = android.view.View.VISIBLE
        }
    }

    private fun isServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    private fun openAppPicker(keyCode: Int, buttonName: String) {
        val intent = Intent(this, AppPickerActivity::class.java).apply {
            putExtra(AppPickerActivity.EXTRA_KEY_CODE, keyCode)
            putExtra(AppPickerActivity.EXTRA_BUTTON_NAME, buttonName)
        }
        startActivity(intent)
    }
}
