package com.example.pgfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirm = binding.confirmPasswordEditText.text.toString().trim()

            if(email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(password != confirm){
                Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase Auth
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = mapOf("email" to email, "role" to "user") // default role user

                    // Save user info in Realtime DB
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Registered successfully",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this,"DB error: ${e.message}",Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this,"Registration failed: ${task.exception?.message}",Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.goToLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
