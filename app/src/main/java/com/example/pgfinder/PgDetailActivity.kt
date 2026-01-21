package com.example.pgfinder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfinder.databinding.ActivityPgDetailBinding
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PgDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPgDetailBinding
    private lateinit var dbRef: DatabaseReference
    private var pgId: String = ""
    private var currentPg: PGModel? = null
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPgDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        pgId = intent.getStringExtra("pgId") ?: ""
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (pgId.isEmpty()) {
            Toast.makeText(this, "Invalid PG", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dbRef = FirebaseDatabase.getInstance().getReference("PGs").child(pgId)
        loadPgDetails()

        binding.btnCall.setOnClickListener {
            currentPg?.call?.let {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
            }
        }

        binding.btnEmail.setOnClickListener {
            currentPg?.email?.let {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$it")))
            }
        }

        binding.btnShare.setOnClickListener {
            currentPg?.let {
                val shareText =
                    "PG: ${it.name}\nLocation: ${it.location}\nRent: ₹${it.price}"
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, shareText)
                startActivity(Intent.createChooser(intent, "Share PG"))
            }
        }

        binding.btnMap.setOnClickListener {
            currentPg?.location?.let {
                val uri = Uri.parse("geo:0,0?q=$it")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }

        binding.btnWishlist.setOnClickListener {
            if (userId.isEmpty()) {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("wishlist")
                .child(pgId)
                .setValue(true)
            Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPgDetails() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentPg = snapshot.getValue(PGModel::class.java)
                if (currentPg == null) {
                    Toast.makeText(this@PgDetailActivity, "PG not available", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                bindData()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun bindData() {
        currentPg?.let {
            binding.tvName.text = it.name
            binding.tvLocation.text = it.location
            binding.tvPrice.text = "₹${it.price}"
        }
    }
}
