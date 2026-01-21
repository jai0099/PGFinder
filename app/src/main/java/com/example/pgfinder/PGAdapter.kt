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
import com.example.pgfinder.PgDetailActivity
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
        val name: TextView = itemView.findViewById(R.id.pgNameTextView)
        val area: TextView = itemView.findViewById(R.id.pgAreaTextView)
        val rent: TextView = itemView.findViewById(R.id.pgRentTextView)
        val callBtn: Button = itemView.findViewById(R.id.callButton)
        val emailBtn: Button = itemView.findViewById(R.id.emailButton)
        val bookmark: ImageView = itemView.findViewById(R.id.bookmarkImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_pg, parent, false)
        return PGViewHolder(view)
    }

    override fun onBindViewHolder(holder: PGViewHolder, position: Int) {
        val pg = pgList[position]

        holder.name.text = pg.name
        holder.area.text = pg.location
        holder.rent.text = "₹${pg.price}"

        // 🔹 Call
        holder.callBtn.setOnClickListener {
            context.startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${pg.call}"))
            )
        }

        // 🔹 Email
        holder.emailBtn.setOnClickListener {
            context.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${pg.email}"))
            )
        }

        // 🔹 Bookmark state
        val isBookmarked = bookmarkedPgIds.contains(pg.id)
        holder.bookmark.setImageResource(
            if (isBookmarked) R.drawable.ic_bookmark_filled
            else R.drawable.ic_bookmark_border
        )

        holder.bookmark.setOnClickListener {
            if (wishlistRef == null || pg.id.isEmpty()) {
                Toast.makeText(context, "Login required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val node = wishlistRef.child(pg.id)
            if (isBookmarked) {
                node.removeValue()
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
            } else {
                node.setValue(true)
                Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
            }
        }

        // ITEM CLICK → DETAIL ACTIVITY
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PgDetailActivity::class.java)
            intent.putExtra("pgId", pg.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pgList.size
}
