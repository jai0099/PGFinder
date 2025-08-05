package com.example.pgfinder

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.model.PGModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var pgName: EditText
    private lateinit var pgLocation: EditText
    private lateinit var pgPrice: EditText
    private lateinit var pgContact: EditText
    private lateinit var addEmail: EditText
    private lateinit var addButton: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_form)

        // UI Components
        pgName = findViewById(R.id.etPgName)
        pgLocation = findViewById(R.id.etArea)
        pgPrice = findViewById(R.id.etRent)
        pgContact = findViewById(R.id.etContact)
        addEmail = findViewById(R.id.etEmail)
        addButton = findViewById(R.id.btnSubmit)

        // Firebase Realtime DB reference
        database = FirebaseDatabase.getInstance().getReference("PGs")

        addButton.setOnClickListener {
            val name = pgName.text.toString().trim()
            val location = pgLocation.text.toString().trim()
            val price = pgPrice.text.toString().trim()
            val contact = pgContact.text.toString().trim()
            val email = addEmail.text.toString().trim()
            //val imageUrl = "" // No image logic

            if (name.isNotEmpty() && location.isNotEmpty() && price.isNotEmpty() &&
                contact.isNotEmpty() && email.isNotEmpty()) {

                val pgId = database.push().key ?: return@setOnClickListener

                val pg = PGModel(name, location, price, contact, email)

                database.child(pgId).setValue(pg)
                    .addOnSuccessListener {
                        Toast.makeText(this, "PG Added", Toast.LENGTH_SHORT).show()
                        clearFields()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add PG: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }

            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearFields() {
        pgName.text.clear()
        pgLocation.text.clear()
        pgPrice.text.clear()
        pgContact.text.clear()
        addEmail.text.clear()
    }
}
