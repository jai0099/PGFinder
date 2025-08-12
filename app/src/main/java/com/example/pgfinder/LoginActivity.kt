package com.example.pgfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var goToRegisterText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailInput = findViewById(R.id.emailEditText)
        passwordInput = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        goToRegisterText = findViewById(R.id.goToRegisterText)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val databaseRef = FirebaseDatabase.getInstance().reference

                        databaseRef.child("Users") // Capital U - consistent
                            .child(userId)
                            .child("role")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val role = snapshot.getValue(String::class.java)

                                if (role != null) {
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                    if (role == "admin") {
                                        startActivity(Intent(this, AdminActivity::class.java))
                                    } else {
                                        startActivity(Intent(this, MainActivity::class.java))
                                    }
                                    finish()
                                } else {
                                    Toast.makeText(this, "Role not found in database", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to fetch role", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        goToRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
