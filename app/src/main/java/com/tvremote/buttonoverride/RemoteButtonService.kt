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
        if (isDetecting) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_HOME) {
                return false
            }
            if (event.action == KeyEvent.ACTION_DOWN) {
                lastDetectedKeyCode = event.keyCode
                detectionCallback?.invoke(event.keyCode)
            }
            return true
        }

        // Consume the UP event of the key we just detected to prevent system action
        if (event.action == KeyEvent.ACTION_UP && event.keyCode == lastDetectedKeyCode) {
            lastDetectedKeyCode = -1
            return true
        }

        val mapping = mappingStore.getMapping(event.keyCode) ?: return false

        if (event.action == KeyEvent.ACTION_DOWN) {
            val launchIntent = packageManager.getLaunchIntentForPackage(mapping.appPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(launchIntent)
            }
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    companion object {
        var instance: RemoteButtonService? = null
            private set

        var isDetecting: Boolean = false
        var detectionCallback: ((Int) -> Unit)? = null
        private var lastDetectedKeyCode: Int = -1

        fun isRunning(): Boolean = instance != null
    }
}
