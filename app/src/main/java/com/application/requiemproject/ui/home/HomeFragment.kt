package com.application.requiemproject.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.application.requiemproject.R
import com.application.requiemproject.services.ScreenCaptureService
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var projectionManager: MediaProjectionManager
    private var useAccessibilityMode: Boolean = false
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startBackgroundWork(result.resultCode, result.data!!)
        } else {
            Toast.makeText(requireContext(), "permission DENIED", Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            Toast.makeText(requireContext(), "notification permission RECEIVED", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "notification permission DENIED", Toast.LENGTH_SHORT)
                .show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageDropdowns(view)
        setupAccessibilitySwitch(view)

        projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val startButton = view.findViewById<Button>(R.id.button_start_translation)
        startButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnClickListener
                }
            }

            if (!canDrawOverlays(requireContext())) {
                val intent = Intent(
                    ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${requireContext().packageName}".toUri()
                )
                startActivity(intent)
                return@setOnClickListener
            }

            requestScreenCapture()
        }
    }

    private fun setupLanguageDropdowns(view: View) {
        val languages = listOf("English", "Russian", "Japanese", "German", "French")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)

        val sourceInput = view.findViewById<AutoCompleteTextView>(R.id.input_source_lang)
        val targetInput = view.findViewById<AutoCompleteTextView>(R.id.input_target_lang)

        sourceInput.setAdapter(adapter)
        targetInput.setAdapter(adapter)
    }

    private fun setupAccessibilitySwitch(view: View) {
        val accessibilitySwitch =
            view.findViewById<SwitchMaterial>(R.id.button_switch_accessibility)
        val statusText = view.findViewById<TextView>(R.id.text_accessibility_status)

        updateAccessibilityStatus(statusText, accessibilitySwitch.isChecked)
        useAccessibilityMode = accessibilitySwitch.isChecked

        accessibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            useAccessibilityMode = isChecked
            updateAccessibilityStatus(statusText, isChecked)
        }
    }

    private fun updateAccessibilityStatus(statusText: TextView, isEnabled: Boolean) {
        statusText.text = if (isEnabled) {
            "Accessibility + OCR enabled"
        } else {
            "OCR only mode"
        }
    }

    private fun requestScreenCapture() {
        val captureIntent = projectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun startBackgroundWork(resultCode: Int, data: Intent) {
        Toast.makeText(requireContext(), "Запуск в фоне...", Toast.LENGTH_SHORT).show()

        val serviceIntent = Intent(requireContext(), ScreenCaptureService::class.java)
        serviceIntent.putExtra("RESULT_CODE", resultCode)
        serviceIntent.putExtra("DATA", data)
        serviceIntent.putExtra(ScreenCaptureService.EXTRA_USE_ACCESSIBILITY_MODE, useAccessibilityMode)

        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }

}
