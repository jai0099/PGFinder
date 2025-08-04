package com.example.pgfinder

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.model.PGModel
import com.google.firebase.firestore.FirebaseFirestore


class AdminActivity : AppCompatActivity() {

    private lateinit var pgName: EditText
    private lateinit var area: EditText
    private lateinit var rent: EditText
    private lateinit var contact: EditText
    private lateinit var email: EditText
    private lateinit var submitBtn: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_form)

        pgName = findViewById(R.id.etPgName)
        area = findViewById(R.id.etArea)
        rent = findViewById(R.id.etRent)
        contact = findViewById(R.id.etContact)
        email = findViewById(R.id.etEmail)
        submitBtn = findViewById(R.id.btnSubmit)

        db = FirebaseFirestore.getInstance()

        submitBtn.setOnClickListener {
            savePGData()
        }
    }

    private fun savePGData() {
        val pg = PGModel(
            pgName = pgName.text.toString(),
            area = area.text.toString(),
            rent = rent.text.toString(),
            contact = contact.text.toString(),
            email = email.text.toString()
        )

        db.collection("pgs")
            .add(pg)
            .addOnSuccessListener {
                Toast.makeText(this, "PG Added Successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        pgName.text.clear()
        area.text.clear()
        rent.text.clear()
        contact.text.clear()
        email.text.clear()
    }
}
