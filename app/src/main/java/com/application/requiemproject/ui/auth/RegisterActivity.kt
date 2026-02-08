package com.application.requiemproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.requiemproject.MainActivity
import com.application.requiemproject.R
import com.application.requiemproject.data.local.db.AppDatabase
import com.application.requiemproject.data.local.entities.User
import com.application.requiemproject.managers.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterActivity: AppCompatActivity() {
    lateinit var goToLoginButton: Button
    lateinit var createAccountButton: Button
    lateinit var inputLogin: TextInputEditText
    lateinit var inputPassword: TextInputEditText

    private val db by lazy { AppDatabase.getDatabase(this) }

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
            finish()
        }

        createAccountButton.isEnabled = false
        createAccountButton.setOnClickListener {
            registerUser()
        }

        inputLogin.addTextChangedListener(textWatcher)
        inputPassword.addTextChangedListener(textWatcher)
    }

    private fun registerUser() {
        val login = inputLogin.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        lifecycleScope.launch {
            val existingUser = db.userDao().getUserByLogin(login)

            if (existingUser != null) {
                inputLogin.error = "такой пользователь уже существует"

            } else {
                val newUser = User(
                    login = login,
                    password = password,
                    privilege = listOf("NEWCOMER!")
                )

                db.userDao().insertUser(newUser)

                val sm = SessionManager(this@RegisterActivity)
                sm.saveSession(newUser.id)

                Toast.makeText(this@RegisterActivity, "Аккаунт создан", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            checkInputState()

        }
    }

    private fun checkInputState() {
        val login = inputLogin.text.toString()
        val password = inputPassword.text.toString()
        val isLoginValid = login.length >= 5
        val isPasswordValid = isPasswordValid(password)

        if (!isLoginValid && login.isNotEmpty()) {
            inputLogin.error = "Логин слишком короткий"

        } else {
            inputLogin.error = null
        }

        if (!isPasswordValid && password.isNotEmpty()) {
            inputPassword.error = getPasswordErrorText(password)
        } else {
            inputPassword.error = null
        }

        createAccountButton.isEnabled = isLoginValid && isPasswordValid
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
