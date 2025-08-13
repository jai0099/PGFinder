package com.example.pgfinder

import AdminPgAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pgfinder.databinding.ActivityAdminFormBinding
import com.example.pgfinder.model.AdminPgModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminFormBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val pgList = mutableListOf<AdminPgModel>()
    private lateinit var adapter: AdminPgAdapter

    private var selectedPgId: String? = null // update ke liye

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("PGs")

        adapter = AdminPgAdapter(pgList,
            onDeleteClick = { pg -> deletePG(pg) },
            onItemClick = { pg -> fillFormForUpdate(pg) }
        )

        binding.rvPgList.layoutManager = LinearLayoutManager(this)
        binding.rvPgList.adapter = adapter

        // Add PG
        binding.btnAdd.setOnClickListener {
            addPG()
        }

        // Update PG
        binding.btnUpdate.setOnClickListener {
            updatePG()
        }

        // Logout
        binding.ivLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchPGList()
    }

    private fun addPG() {
        val name = binding.etName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val call = binding.etCall.text.toString().trim()

        if (name.isEmpty() || location.isEmpty() || price.isEmpty() || email.isEmpty() || call.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val pgId = database.push().key ?: return
        val pg = AdminPgModel(pgId, name, location, price, email, call)

        database.child(pgId).setValue(pg).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "PG added successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            } else {
                Toast.makeText(this, "Failed to add PG", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePG() {
        val name = binding.etName.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val call = binding.etCall.text.toString().trim()

        if (selectedPgId == null) {
            Toast.makeText(this, "Select a PG to update", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedPg = AdminPgModel(selectedPgId!!, name, location, price, email, call)

        database.child(selectedPgId!!).setValue(updatedPg).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "PG updated successfully", Toast.LENGTH_SHORT).show()
                clearFields()
                selectedPgId = null
            } else {
                Toast.makeText(this, "Failed to update PG", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fillFormForUpdate(pg: AdminPgModel) {
        selectedPgId = pg.id
        binding.etName.setText(pg.name)
        binding.etLocation.setText(pg.location)
        binding.etPrice.setText(pg.price)
        binding.etEmail.setText(pg.email)
        binding.etEmail.isEnabled = false // email change nahi hoga
        binding.etCall.setText(pg.call)
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
        binding.etEmail.isEnabled = true
        binding.etCall.text.clear()
    }
}
