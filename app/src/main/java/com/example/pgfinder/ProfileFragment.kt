package com.example.pgfinder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        val tvEmail = v.findViewById<TextView>(R.id.tvEmail)
        val btnLogout = v.findViewById<Button>(R.id.btnLogout)

        val auth = FirebaseAuth.getInstance()
        tvEmail.text = auth.currentUser?.email ?: "Not logged in"

        btnLogout.setOnClickListener {
            auth.signOut()
            val i = Intent(requireContext(), LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        return v
    }
}
