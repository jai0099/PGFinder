package com.example.pgfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("Users")

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // Real-time fetch of role
                        usersRef.child(userId).child("role")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val role = snapshot.getValue(String::class.java)
                                    if (role != null) {
                                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                        if (role == "admin") {
                                            startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                                        } else {
                                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        }
                                        finish()
                                    } else {
                                        Toast.makeText(this@LoginActivity, "Role not found", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    } else {
                        Toast.makeText(this,"Login failed: ${task.exception?.message}",Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.goToRegisterText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
