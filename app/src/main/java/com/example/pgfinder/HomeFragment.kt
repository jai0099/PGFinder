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
    private var pgList = mutableListOf<PGModel>()
    private var databaseRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("PGs")
    private var userId: String = ""
    private var selectedCity: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        binding.pgRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        pgAdapter = PGAdapter(requireContext(), pgList, userId)
        binding.pgRecyclerView.adapter = pgAdapter

        loadPGs()
    }

    private fun loadPGs() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pgList.clear()
                for (pgSnapshot in snapshot.children) {
                    val pg = pgSnapshot.getValue(PGModel::class.java)
                    if (pg != null) {
                        if (selectedCity == null || pg.location == selectedCity) {
                            pgList.add(pg)
                        }
                    }
                }
                pgAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // PUBLIC so MainActivity can call it
    fun applyCityFilter(city: String?) {
        selectedCity = city
        loadPGs()
    }
}
