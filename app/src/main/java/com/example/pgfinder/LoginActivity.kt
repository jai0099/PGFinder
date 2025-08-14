package com.example.pgfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Fetch role from DB for redirect
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .child("role")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val role = snapshot.getValue(String::class.java)
                            if(role == "admin") {
                                startActivity(Intent(this, AdminActivity::class.java))
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            // fallback: normal user
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }

                } else {
                    Toast.makeText(this,"Login failed: ${task.exception?.message}",Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.goToRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
