package com.example.pgfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PGAdapter
    private val pgList = ArrayList<PGModel>()
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = v.findViewById(R.id.pgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        adapter = PGAdapter(requireContext(), pgList, currentUser?.uid ?: "")
        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("PGs")
        fetchData()

        return v
    }

    private fun fetchData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null) {
                        pg.pgId = pgSnap.key ?: ""
                        pgList.add(pg)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
