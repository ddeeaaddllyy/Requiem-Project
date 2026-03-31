package com.application.requiemproject.ui.profile

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.application.requiemproject.R
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.managers.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class SettingsBottomSheet: BottomSheetDialogFragment() {
    lateinit var rejectButton: Button
    lateinit var applyButton: Button
    lateinit var emailInputEditText: EditText
    lateinit var changePasswordButton: Button

    private val sm by lazy { SessionManager(requireContext()) }
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    override fun getTheme(): Int = R.style.BottomSheetTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_settings_button_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        rejectButton = view.findViewById(R.id.rejectButton)
        applyButton = view.findViewById(R.id.applyButton)
        emailInputEditText = view.findViewById(R.id.emailInput)

        rejectButton.setOnClickListener { dismiss() }

        changePasswordButton.setOnClickListener {
            Toast.makeText(requireContext(), "do nothing yet", Toast.LENGTH_SHORT).show()
        }

        applyButton.setOnClickListener {
            val email = emailInputEditText.text.toString().trim()
            val userId = sm.getUserId()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputEditText.error = "Email is not valid"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                db.userDao().updateEmail(userId, email)

                Toast.makeText(requireContext(), "Email is added", Toast.LENGTH_SHORT).show()

                dismiss()
            }
        }

    }

}