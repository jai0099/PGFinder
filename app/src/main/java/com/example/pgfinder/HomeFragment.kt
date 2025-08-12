package com.example.pgfinder

import android.os.Bundle
import android.util.Log
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

    // keep listener reference so we can remove it
    private var dbListener: ValueEventListener? = null

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
        // prevent creating multiple listeners if fetchData called twice
        if (dbListener != null) return

        dbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // If fragment not attached, skip UI operations
                if (!isAdded) {
                    Log.w("HomeFragment", "Fragment not added — skipping onDataChange")
                    return
                }

                Log.d("HomeFragment", "Snapshot exists? ${snapshot.exists()}, count=${snapshot.childrenCount}")
                pgList.clear()
                for (pgSnap in snapshot.children) {
                    // Make sure PGModel fields match the DB keys (id/name/location/price/call/email)
                    val pg = pgSnap.getValue(PGModel::class.java)
                    Log.d("HomeFragment", "PG fetched: $pg")
                    if (pg != null) {
                        // set id from key (if model has mutable id var)
                        // If your model field is named `id`, set pg.id; if it's `pgId`, set pg.pgId accordingly.
                        // Example assuming model has `var id: String = ""`
                        try {
                            pg.id = pgSnap.key ?: pg.id
                        } catch (e: Exception) {
                            // ignore if model field is immutable or named differently
                        }
                        pgList.add(pg)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // log always
                Log.e("HomeFragment", "Failed to fetch PGs: ${error.message}")

                // show a toast only if fragment is attached and activity isn't null
                activity?.let { act ->
                    // act's type inferred automatically — no explicit Activity type needed
                    Toast.makeText(act, "Failed to fetch PGs: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dbRef.addValueEventListener(dbListener as ValueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // remove listener so callbacks stop when fragment view destroyed
        dbListener?.let {
            dbRef.removeEventListener(it)
            dbListener = null
        }
    }
}
