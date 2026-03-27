package com.application.requiemproject.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.application.requiemproject.R
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.ui.auth.LoginActivity
import com.application.requiemproject.managers.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val sm by lazy { SessionManager(requireContext()) }
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutButton = view.findViewById<Button>(R.id.button_logout)
        val settingsButton = view.findViewById<Button>(R.id.button_settings)
        val usernameText = view.findViewById<TextView>(R.id.text_username)

        loadUserData(usernameText)

        logoutButton.setOnClickListener {
            sm.clearSession()

            Toast.makeText(requireContext(), "Выход из системы...", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }

        settingsButton.setOnClickListener {
            SettingsBottomSheet().show(parentFragmentManager, "SettingsSheet")
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(0, insets.top, 0, 0)
            windowInsets

        }
    }

    override fun onResume() {
        try {
            super.onResume()
            loadUserData(view!!.findViewById(R.id.text_username))
        } catch (e: Exception) {
            Log.e("ProfileFragment", "onResume have an error: $e")
        }
    }

    private fun loadUserData(textView: TextView) {
        val userId = sm.getUserId()

        if (userId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val user = db.userDao().getUserById(userId)
                if (user != null) {
                    textView.text = user.login
                } else {
                    textView.text = "ERROR id = $userId\n user = $user"
                }
            }
        } else return
    }

}