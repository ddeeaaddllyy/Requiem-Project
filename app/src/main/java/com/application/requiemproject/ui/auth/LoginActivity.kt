package com.application.requiemproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.requiemproject.MainActivity
import com.application.requiemproject.R
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.managers.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val db = AppDatabase.getDatabase(this)
        val sm = SessionManager(this)
        val intent = Intent(this, MainActivity::class.java)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonRegister = findViewById<Button>(R.id.button_register)
        val inputLogin = findViewById<TextInputEditText>(R.id.input_login)
        val inputPassword = findViewById<TextInputEditText>(R.id.input_password)

        if (sm.isLoggedIn()) {
            startActivity(intent)
            finish()
            return Unit
        }

        buttonLogin.setOnClickListener {
            val enteredLogin = inputLogin.text.toString().trim()
            val enteredPassword = inputPassword.text.toString().trim()

            if (enteredLogin.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(
                    this,
                    "Сначала заполните все поля",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.userDao().getUserByLogin(enteredLogin)

                if (user != null && enteredPassword == user.password) {
                    sm.saveSession(user.id)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Неверный логин или пароль",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

}