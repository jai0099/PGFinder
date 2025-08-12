package com.example.pgfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.model.PGModel
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private val allList = ArrayList<PGModel>()
    private val shownList = ArrayList<PGModel>()
    private lateinit var adapter: PGAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private var userId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_search, container, false)
        searchView = v.findViewById(R.id.searchView)
        recyclerView = v.findViewById(R.id.searchRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // userId from auth if needed for adapter
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        //for search bar on click response
        searchView.setOnClickListener {
            searchView.isIconified = false
            searchView.requestFocusFromTouch()
        }
        searchView.isIconified = false
        searchView.clearFocus() // so keyboard doesn't pop up immediately

        adapter = PGAdapter(requireContext(), shownList, userId)
        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("PGs")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null) {
                        pg.id = pgSnap.key ?: ""
                        allList.add(pg)
                    }
                }
                // initially show all
                shownList.clear()
                shownList.addAll(allList)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText ?: "")
                return true
            }
        })

        return v
    }

    private fun filterList(q: String) {
        val query = q.trim().lowercase()
        shownList.clear()
        if (query.isEmpty()) {
            shownList.addAll(allList)
        } else {
            for (pg in allList) {
                val combined = "${pg.name} ${pg.location} ${pg.price}".lowercase()
                if (combined.contains(query)) {
                    shownList.add(pg)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}
