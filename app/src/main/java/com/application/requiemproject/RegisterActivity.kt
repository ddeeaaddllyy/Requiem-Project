package com.application.requiemproject

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity: AppCompatActivity() {
    lateinit var goToLoginButton: Button
    lateinit var createAccountButton: Button
    lateinit var inputLogin: TextInputEditText
    lateinit var inputPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        goToLoginButton = findViewById(R.id.button_go_to_log_in)
        createAccountButton = findViewById(R.id.button_create_account)
        inputLogin = findViewById(R.id.input_login)
        inputPassword = findViewById(R.id.input_password)

        goToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        createAccountButton.isEnabled = false
        createAccountButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        inputLogin.addTextChangedListener(loginTextWatcher)
        inputPassword.addTextChangedListener(passwordTextWatcher)
    }

    private val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int ) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val password = s.toString()
            val isValid = isPasswordValid(password)

            if (isValid) {
                inputPassword.error = null
                createAccountButton.isEnabled = true

            } else {
                inputPassword.error = getPasswordErrorText(password)
                createAccountButton.isEnabled = false
            }

        }
    }

    private val loginTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val login = s.toString()

            if (login.length >= 6) {
                inputPassword.error = null
                createAccountButton.isEnabled = true
            } else {
                inputPassword.error = "Пароль должен содержать минимум 6 символов"
                createAccountButton.isEnabled = false
            }

        }
    }

}

private fun isPasswordValid(password: String): Boolean {
    val isLengthValid = password.length >= 6

    val hasDigit = password.contains(Regex(".*\\d.*"))

    return isLengthValid && hasDigit
}

private fun getPasswordErrorText(password: String): String {
    val errors = mutableListOf<String>()

    if (password.length < 6) {
        errors.add("Минимум 6 символов")
    }

    if (!password.contains(Regex(".*\\d.*"))) {
        errors.add("Хотя бы одну цифру")
    }

    return "Пароль должен содержать:\n" + errors.joinToString("\n")
}

