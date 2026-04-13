package com.tvremote.buttonoverride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppPickerActivity : FragmentActivity() {

    companion object {
        const val EXTRA_KEY_CODE = "extra_key_code"
        const val EXTRA_BUTTON_NAME = "extra_button_name"
    }

    private lateinit var mappingStore: MappingStore
    private var keyCode: Int = -1
    private var buttonName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        keyCode = intent.getIntExtra(EXTRA_KEY_CODE, -1)
        buttonName = intent.getStringExtra(EXTRA_BUTTON_NAME) ?: ""

        if (keyCode == -1) {
            finish()
            return
        }

        mappingStore = MappingStore(this)

        val titleText = findViewById<TextView>(R.id.title_text)
        titleText.text = getString(R.string.pick_app_for, buttonName)

        // Show "Remove override" button only if there's an existing mapping
        val btnRemove = findViewById<Button>(R.id.btn_remove_mapping)
        if (mappingStore.getMapping(keyCode) != null) {
            btnRemove.visibility = View.VISIBLE
            btnRemove.setOnClickListener {
                mappingStore.removeMapping(keyCode)
                setResult(RESULT_OK)
                finish()
            }
        } else {
            btnRemove.visibility = View.GONE
        }

        val apps = loadInstalledApps()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_apps)
        // 5 columns gives good density on a TV screen
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = AppPickerAdapter(apps) { selectedApp ->
            mappingStore.saveMapping(
                ButtonMapping(
                    keyCode = keyCode,
                    buttonName = buttonName,
                    appPackage = selectedApp.packageName,
                    appName = selectedApp.name
                )
            )
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadInstalledApps(): List<AppInfo> {
        val pm = packageManager

        // Prefer TV (leanback) apps, then fall back to all launchable apps
        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val allActivities = pm.queryIntentActivities(tvIntent, 0)
            .plus(pm.queryIntentActivities(mainIntent, 0))

        return allActivities
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName } // exclude self
            .map { ri ->
                AppInfo(
                    name = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName,
                    icon = ri.loadIcon(pm)
                )
            }
            .sortedBy { it.name }
    }
}
