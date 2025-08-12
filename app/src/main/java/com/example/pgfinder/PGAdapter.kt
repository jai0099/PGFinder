package com.example.pgfinder.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.R
import com.example.pgfinder.model.PGModel
import com.google.firebase.database.*

class PGAdapter(
    private val context: Context,
    private val pgList: List<PGModel>,
    private val userId: String
) : RecyclerView.Adapter<PGAdapter.PGViewHolder>() {

    private val wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(userId)
    private val bookmarkedPgIds = mutableSetOf<String>()

    init {
        wishlistRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookmarkedPgIds.clear()
                for (child in snapshot.children) {
                    bookmarkedPgIds.add(child.key.toString())
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    inner class PGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pgName: TextView = itemView.findViewById(R.id.pgNameTextView)
        val pgArea: TextView = itemView.findViewById(R.id.pgAreaTextView)
        val pgRent: TextView = itemView.findViewById(R.id.pgRentTextView)
        val callButton: Button = itemView.findViewById(R.id.callButton)
        val emailButton: Button = itemView.findViewById(R.id.emailButton)
        val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmarkImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pg, parent, false)
        return PGViewHolder(view)
    }

    override fun onBindViewHolder(holder: PGViewHolder, position: Int) {
        val pg = pgList[position]

        holder.pgName.text = pg.name
        holder.pgArea.text = pg.location
        holder.pgRent.text = "₹${pg.price}"

        holder.callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${pg.call}")
            context.startActivity(intent)
        }

        holder.emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${pg.email}")
            context.startActivity(intent)
        }

        if (bookmarkedPgIds.contains(pg.id)) {
            holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)
        } else {
            holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
        }

        holder.bookmarkIcon.setOnClickListener {
            if (bookmarkedPgIds.contains(pg.id)) {
                wishlistRef.child(pg.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "${pg.name} removed from wishlist", Toast.LENGTH_SHORT).show()
                    }
            } else {
                wishlistRef.child(pg.id).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(context, "${pg.name} added to wishlist", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int = pgList.size
}
