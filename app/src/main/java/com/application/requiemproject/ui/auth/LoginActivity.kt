package com.application.requiemproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.application.requiemproject.MainActivity
import com.application.requiemproject.R
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonLogin = findViewById<Button>(R.id.button_login)
        val buttonRegister = findViewById<Button>(R.id.button_register)
        val inputLogin = findViewById<TextInputEditText>(R.id.input_login)
        val inputPassword = findViewById<TextInputEditText>(R.id.input_password)

        buttonLogin.setOnClickListener {
            val enteredEmail = inputLogin.text.toString().trim()
            val enteredPassword = inputPassword.text.toString().trim()

            if (true) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        buttonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }

}