package com.example.pgfinder.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.R
import com.example.pgfinder.model.PGModel
import com.google.firebase.database.*

class PGAdapter(
    private val context: Context,
    private val pgList: List<PGModel>,
    private val userId: String
) : RecyclerView.Adapter<PGAdapter.PGViewHolder>() {

    private val wishlistRef: DatabaseReference? =
        if (userId.isNotEmpty())
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("wishlist")
        else null

    private val bookmarkedPgIds = mutableSetOf<String>()
    private var wishlistListener: ValueEventListener? = null

    init {
        // Listen to user's wishlist changes
        if (wishlistRef != null) {
            wishlistListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookmarkedPgIds.clear()
                    for (child in snapshot.children) {
                        child.key?.let { bookmarkedPgIds.add(it) }
                    }
                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            wishlistRef.addValueEventListener(wishlistListener!!)
        }
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pg, parent, false)

        // Ensure bookmark is clickable even inside CardView
        view.findViewById<ImageView>(R.id.bookmarkImageView)?.apply {
            isClickable = true
            isFocusable = true
        }

        return PGViewHolder(view)
    }

    override fun onBindViewHolder(holder: PGViewHolder, position: Int) {
        val pg = pgList[position]

        // Set data
        holder.pgName.text = pg.name
        holder.pgArea.text = pg.location
        holder.pgRent.text = "₹${pg.price}"

        // Call
        holder.callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${pg.call}")
            }
            context.startActivity(intent)
        }

        // Email
        holder.emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${pg.email}")
            }
            context.startActivity(intent)
        }

        // Bookmark State
        val isBookmarked = bookmarkedPgIds.contains(pg.id)
        holder.bookmarkIcon.setImageResource(
            if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
        )

        // Bookmark click handling
        holder.bookmarkIcon.setOnClickListener {
            if (wishlistRef == null || pg.id.isEmpty()) {
                Toast.makeText(context, "Login required for wishlist", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val node = wishlistRef.child(pg.id)

            if (isBookmarked) {
                bookmarkedPgIds.remove(pg.id)
                holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border)
                node.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                }
            } else {
                bookmarkedPgIds.add(pg.id)
                holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled)
                node.setValue(true).addOnSuccessListener {
                    Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = pgList.size

    fun cleanup() {
        wishlistRef?.let { ref ->
            wishlistListener?.let { ref.removeEventListener(it) }
        }
        wishlistListener = null
    }
}
