package com.tvremote.buttonoverride

import android.content.Context

class MappingStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveMapping(mapping: ButtonMapping) {
        prefs.edit()
            .putString(keyButtonName(mapping.keyCode), mapping.buttonName)
            .putString(keyPkg(mapping.keyCode), mapping.appPackage)
            .putString(keyAppName(mapping.keyCode), mapping.appName)
            .apply()
    }

    fun getMapping(keyCode: Int): ButtonMapping? {
        val buttonName = prefs.getString(keyButtonName(keyCode), null) ?: return null
        val appPackage = prefs.getString(keyPkg(keyCode), null) ?: return null
        val appName = prefs.getString(keyAppName(keyCode), "") ?: ""
        return ButtonMapping(keyCode, buttonName, appPackage, appName)
    }

    fun removeMapping(keyCode: Int) {
        prefs.edit()
            .remove(keyButtonName(keyCode))
            .remove(keyPkg(keyCode))
            .remove(keyAppName(keyCode))
            .apply()
    }

    fun getAllMappings(): List<ButtonMapping> {
        return prefs.all.keys
            .filter { it.startsWith(PREFIX_BUTTON_NAME) }
            .mapNotNull { key ->
                val keyCode = key.removePrefix(PREFIX_BUTTON_NAME).toIntOrNull() ?: return@mapNotNull null
                getMapping(keyCode)
            }
            .sortedBy { it.buttonName }
    }

    companion object {
        private const val PREFS_NAME = "button_mappings"
        private const val PREFIX_BUTTON_NAME = "btn_"
        private const val PREFIX_PKG = "pkg_"
        private const val PREFIX_APP_NAME = "app_"

        private fun keyButtonName(keyCode: Int) = "btn_$keyCode"
        private fun keyPkg(keyCode: Int) = "pkg_$keyCode"
        private fun keyAppName(keyCode: Int) = "app_$keyCode"
    }
}
