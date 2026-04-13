package com.tvremote.buttonoverride

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Programmatically add the key-intercept capability so we don't need
        // android:canInterceptKeyEvents in the XML (AAPT2 rejects it on some SDK versions)
        val info = serviceInfo
        info.capabilities = info.capabilities or
                AccessibilityServiceInfo.CAPABILITY_CAN_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (isDetecting) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val mapping = mappingStore.getMapping(event.keyCode) ?: return false

        val launchIntent = packageManager.getLaunchIntentForPackage(mapping.appPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(launchIntent)
            return true
        }

        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    companion object {
        var instance: RemoteButtonService? = null
            private set

        var isDetecting: Boolean = false

        fun isRunning(): Boolean = instance != null
    }
}
