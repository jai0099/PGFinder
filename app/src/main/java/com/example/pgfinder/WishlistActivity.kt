package com.example.pgfinder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WishlistActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PGAdapter
    private lateinit var pgList: ArrayList<PGModel>
    private lateinit var wishlistRef: DatabaseReference
    private lateinit var pgRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Back button enable
        supportActionBar?.title = "Wishlist"

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        recyclerView = findViewById(R.id.pgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pgList = ArrayList()
        adapter = PGAdapter(this, pgList, userId)
        recyclerView.adapter = adapter

        wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(userId)
        pgRef = FirebaseDatabase.getInstance().getReference("PGs")

        fetchWishlist()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun fetchWishlist() {
        wishlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pgIds = mutableListOf<String>()
                for (child in snapshot.children) {
                    pgIds.add(child.key.toString())
                }
                fetchPGDetails(pgIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@WishlistActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPGDetails(pgIds: List<String>) {
        pgList.clear()
        if (pgIds.isEmpty()) {
            adapter.notifyDataSetChanged()
            return
        }
        pgRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (pgSnap in snapshot.children) {
                    if (pgSnap.key in pgIds) {
                        val pg = pgSnap.getValue(PGModel::class.java)
                        if (pg != null) {
                            pg.id = pgSnap.key ?: ""
                            pgList.add(pg)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@WishlistActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
