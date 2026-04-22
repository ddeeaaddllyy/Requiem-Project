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
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.application.requiemproject.App
import com.application.requiemproject.R
import com.application.requiemproject.model.AppLanguage
import com.application.requiemproject.model.ScanSource
import com.application.requiemproject.services.ScreenCaptureService
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var sourceInput: AutoCompleteTextView
    private lateinit var targetInput: AutoCompleteTextView
    private val settingsRepository by lazy {
        (requireActivity().application as App).translationSettingsRepository
    }

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
            Toast.makeText(
                requireContext(),
                "notification permission RECEIVED",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "notification permission DENIED",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageDropdowns(view)
        setupAccessibilitySwitch(view)

        projectionManager = requireContext().getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        view.findViewById<Button>(R.id.button_start_translation).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@setOnClickListener
            }

            if (!canDrawOverlays(requireContext())) {
                startActivity(
                    Intent(
                        ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${requireContext().packageName}".toUri()
                    )
                )
                return@setOnClickListener
            }

            requestScreenCapture()
        }
    }

    private fun setupLanguageDropdowns(view: View) {
        val settings = settingsRepository.getSettings()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            AppLanguage.entries.map(AppLanguage::displayName)
        )

        sourceInput = view.findViewById(R.id.input_source_lang)
        targetInput = view.findViewById(R.id.input_target_lang)

        sourceInput.setAdapter(adapter)
        targetInput.setAdapter(adapter)
        sourceInput.setText(settings.sourceLanguage.displayName, false)
        targetInput.setText(settings.targetLanguage.displayName, false)

        sourceInput.setOnItemClickListener { _, _, position, _ ->
            settingsRepository.updateSourceLanguage(AppLanguage.entries[position])
        }
        targetInput.setOnItemClickListener { _, _, position, _ ->
            settingsRepository.updateTargetLanguage(AppLanguage.entries[position])
        }
    }

    private fun setupAccessibilitySwitch(view: View) {
        val settings = settingsRepository.getSettings()
        val accessibilitySwitch = view.findViewById<SwitchMaterial>(R.id.button_switch_accessibility)
        val statusText = view.findViewById<TextView>(R.id.text_accessibility_status)
        val warningText = view.findViewById<TextView>(R.id.text_accessibility_warning)

        accessibilitySwitch.isChecked = settings.scanSource == ScanSource.ACCESSIBILITY
        updateAccessibilityModeUi(statusText, warningText, accessibilitySwitch.isChecked)

        accessibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.updateScanSource(
                if (isChecked) ScanSource.ACCESSIBILITY else ScanSource.OCR
            )
            updateAccessibilityModeUi(statusText, warningText, isChecked)
        }
    }

    private fun updateAccessibilityModeUi(
        statusText: TextView,
        warningText: TextView,
        isEnabled: Boolean
    ) {
        statusText.text = if (isEnabled) {
            getString(R.string.accessibility_mode_enabled)
        } else {
            getString(R.string.ocr_only_mode)
        }
        warningText.isVisible = isEnabled
    }

    private fun requestScreenCapture() {
        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun startBackgroundWork(resultCode: Int, data: Intent) {
        persistLanguageSelection()
        Toast.makeText(requireContext(), "Starting capture in background...", Toast.LENGTH_SHORT)
            .show()

        val serviceIntent = Intent(requireContext(), ScreenCaptureService::class.java).apply {
            putExtra("RESULT_CODE", resultCode)
            putExtra("DATA", data)
        }

        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }

    private fun persistLanguageSelection() {
        val currentSettings = settingsRepository.getSettings()
        settingsRepository.updateSourceLanguage(
            AppLanguage.fromDisplayName(
                sourceInput.text?.toString(),
                currentSettings.sourceLanguage
            )
        )
        settingsRepository.updateTargetLanguage(
            AppLanguage.fromDisplayName(
                targetInput.text?.toString(),
                currentSettings.targetLanguage
            )
        )
    }
}
