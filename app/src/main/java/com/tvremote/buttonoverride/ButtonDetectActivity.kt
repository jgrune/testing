package com.tvremote.buttonoverride

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class ButtonDetectActivity : FragmentActivity() {

    private var detectedKeyCode: Int = -1

    private lateinit var promptText: TextView
    private lateinit var keyInfoText: TextView
    private lateinit var nameField: EditText
    private lateinit var confirmSection: LinearLayout
    private lateinit var btnProceed: Button
    private lateinit var btnRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_detect)

        promptText = findViewById(R.id.prompt_text)
        keyInfoText = findViewById(R.id.key_info_text)
        nameField = findViewById(R.id.name_field)
        confirmSection = findViewById(R.id.confirm_section)
        btnProceed = findViewById(R.id.btn_proceed)
        btnRetry = findViewById(R.id.btn_retry)

        confirmSection.visibility = View.GONE

        // Tell the service to pass all key events through while we're detecting
        RemoteButtonService.isDetecting = true

        btnProceed.setOnClickListener {
            if (detectedKeyCode != -1) {
                val name = nameField.text.toString().trim().ifEmpty {
                    KeyEvent.keyCodeToString(detectedKeyCode)
                }
                val intent = Intent(this, AppPickerActivity::class.java).apply {
                    putExtra(AppPickerActivity.EXTRA_KEY_CODE, detectedKeyCode)
                    putExtra(AppPickerActivity.EXTRA_BUTTON_NAME, name)
                }
                startActivity(intent)
                finish()
            }
        }

        btnRetry.setOnClickListener {
            resetDetection()
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Restore normal service behavior
        RemoteButtonService.isDetecting = false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Let Back and Home keys pass through normally
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return super.onKeyDown(keyCode, event)
        }

        // Capture the keycode
        detectedKeyCode = keyCode
        val keyName = KeyEvent.keyCodeToString(keyCode)

        keyInfoText.text = getString(R.string.key_detected_info, keyName, keyCode)
        promptText.text = getString(R.string.key_detected_prompt)
        nameField.setText(keyName.replace("KEYCODE_", "").replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() })
        confirmSection.visibility = View.VISIBLE

        return true // consume the event
    }

    private fun resetDetection() {
        detectedKeyCode = -1
        promptText.text = getString(R.string.detect_prompt)
        keyInfoText.text = ""
        nameField.setText("")
        confirmSection.visibility = View.GONE
    }
}
