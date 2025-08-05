package com.example.pgfinder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.model.PGModel
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PGAdapter
    private lateinit var pgList: ArrayList<PGModel>
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.pgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pgList = ArrayList()
        adapter = PGAdapter(this, pgList)
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
}
