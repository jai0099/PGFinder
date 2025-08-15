// on progress
package com.example.pgfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.databinding.ActivityWishlistBinding
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var pgAdapter: PGAdapter
    private var pgList = mutableListOf<PGModel>()
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val database = FirebaseDatabase.getInstance().getReference("Wishlist").child(userId)

        binding.recyclerWishlist.layoutManager = LinearLayoutManager(this)
        pgAdapter = PGAdapter(this, pgList, userId)
        binding.recyclerWishlist.adapter = pgAdapter

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnapshot in snapshot.children) {
                    val pg = pgSnapshot.getValue(PGModel::class.java)
                    if (pg != null) {
                        pgList.add(pg)
                    }
                }
                pgAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
