package com.example.pgfinder

import AdminPgAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pgfinder.databinding.ActivityAdminFormBinding
import com.example.pgfinder.model.AdminPgModel
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminFormBinding
    private lateinit var database: DatabaseReference
    private val pgList = mutableListOf<AdminPgModel>()
    private lateinit var adapter: AdminPgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("PGs")

        adapter = AdminPgAdapter(pgList) { pgToDelete ->
            deletePG(pgToDelete)
        }
        binding.rvPgList.layoutManager = LinearLayoutManager(this)
        binding.rvPgList.adapter = adapter

        binding.btnAddUpdate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val price = binding.etPrice.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val call = binding.etCall.text.toString().trim()

            if (name.isEmpty() || location.isEmpty() || price.isEmpty() || email.isEmpty() || call.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pgId = database.push().key ?: return@setOnClickListener
            val pg = AdminPgModel(pgId, name, location, price, email, call)

            database.child(pgId).setValue(pg).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "PG added successfully", Toast.LENGTH_SHORT).show()
                    clearFields()
                } else {
                    Toast.makeText(this, "Failed to add PG", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fetchPGList()
    }

    private fun fetchPGList() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnapshot in snapshot.children) {
                    val pg = pgSnapshot.getValue(AdminPgModel::class.java)
                    if (pg != null) pgList.add(pg)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminActivity, "Failed to load PGs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deletePG(pg: AdminPgModel) {
        pg.id?.let {
            database.child(it).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "PG deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete PG", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearFields() {
        binding.etName.text.clear()
        binding.etLocation.text.clear()
        binding.etPrice.text.clear()
        binding.etEmail.text.clear()
        binding.etCall.text.clear()
    }
}
