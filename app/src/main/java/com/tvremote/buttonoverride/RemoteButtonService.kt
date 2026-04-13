package com.tvremote.buttonoverride

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class RemoteButtonService : AccessibilityService() {

    private lateinit var mappingStore: MappingStore

    override fun onCreate() {
        super.onCreate()
        mappingStore = MappingStore(this)
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Allow all keys through when in detection mode
        if (isDetecting) return false

        // Only act on key-down to avoid double-triggering
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val mapping = mappingStore.getMapping(event.keyCode) ?: return false

        val launchIntent = packageManager.getLaunchIntentForPackage(mapping.appPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(launchIntent)
            return true // consume the event — prevent original button action
        }

        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for key interception
    }

    override fun onInterrupt() {
        // Service interrupted — nothing to clean up
    }

    companion object {
        // Expose instance so activities can check service state
        var instance: RemoteButtonService? = null
            private set

        // Set to true to let key events pass through (used during button detection)
        var isDetecting: Boolean = false

        fun isRunning(): Boolean = instance != null
    }
}
