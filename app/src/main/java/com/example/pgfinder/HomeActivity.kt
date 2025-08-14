package com.example.pgfinder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.databinding.ActivityHomeBinding
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var pgAdapter: PGAdapter
    private val pgList = mutableListOf<PGModel>()
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("PGs")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        binding.pgRecyclerView.layoutManager = LinearLayoutManager(this)
        pgAdapter = PGAdapter(this, pgList, userId)
        binding.pgRecyclerView.adapter = pgAdapter

        fetchPGs()
    }

    private fun fetchPGs(cityFilter: String? = null) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null) {
                        if (cityFilter == null || pg.location.equals(cityFilter, ignoreCase = true)) {
                            pgList.add(pg)
                        }
                    }
                }
                pgAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showFilterDialog() {
        val cities = arrayOf("All", "Ahmedabad", "Surat", "Rajkot")
        AlertDialog.Builder(this)
            .setTitle("Select City")
            .setItems(cities) { _, which ->
                if (cities[which] == "All") {
                    fetchPGs()
                } else {
                    fetchPGs(cities[which])
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_wishlist -> {
                startActivity(Intent(this, WishlistActivity::class.java))
                true
            }
            R.id.menu_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
