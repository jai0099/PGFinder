package com.example.pgfinder

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PGAdapter
    private lateinit var pgList: ArrayList<PGModel>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return

        recyclerView = findViewById(R.id.pgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pgList = ArrayList()
        adapter = PGAdapter(this, pgList, currentUser.uid)
        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("PGs")
        fetchData()
    }

    private fun fetchData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null) {
                        pg.id = pgSnap.key ?: ""
                        pgList.add(pg)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Toolbar me wishlist icon add karne ke liye
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_wishlist -> {
                startActivity(Intent(this, WishlistActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
