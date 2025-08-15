package com.example.pgfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pgfinder.adapter.PGAdapter
import com.example.pgfinder.databinding.FragmentHomeBinding
import com.example.pgfinder.model.PGModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var pgAdapter: PGAdapter
    private val pgList = mutableListOf<PGModel>()
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("PGs")
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var selectedCity: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView setup
        binding.pgRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        pgAdapter = PGAdapter(requireContext(), pgList, userId)
        binding.pgRecyclerView.adapter = pgAdapter

        // Real-time listener for PGs
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null && (selectedCity == null || pg.location.equals(selectedCity, true))) {
                        pgList.add(pg)
                    }
                }
                pgAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Public function to apply city filter dynamically
    fun applyCityFilter(city: String?) {
        selectedCity = city

        // Update TextView to show active filter
        binding.tvActiveFilter.text = "Showing: ${city ?: "All cities"}"

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    val pg = pgSnap.getValue(PGModel::class.java)
                    if (pg != null && (selectedCity == null || pg.location.equals(selectedCity, true))) {
                        pgList.add(pg)
                    }
                }
                pgAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
