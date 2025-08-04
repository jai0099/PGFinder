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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pgfinder.R
import com.example.pgfinder.model.PGModel

class PGAdapter(
    private val context: Context,
    private val pgList: List<PGModel>
) : RecyclerView.Adapter<PGAdapter.PGViewHolder>() {

    inner class PGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pgImage: ImageView = itemView.findViewById(R.id.pgImageView)
        val pgName: TextView = itemView.findViewById(R.id.pgNameTextView)
        val pgArea: TextView = itemView.findViewById(R.id.pgAreaTextView)
        val pgRent: TextView = itemView.findViewById(R.id.pgRentTextView)
        val callButton: Button = itemView.findViewById(R.id.callButton)
        val emailButton: Button = itemView.findViewById(R.id.emailButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pg, parent, false)
        return PGViewHolder(view)
    }

    override fun onBindViewHolder(holder: PGViewHolder, position: Int) {
        val pg = pgList[position]

        holder.pgName.text = pg.pgName
        holder.pgArea.text = pg.area
        holder.pgRent.text = "₹${pg.rent}"

        Glide.with(context).load(pg.imageUrl).into(holder.pgImage)

        holder.callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${pg.contact}")
            context.startActivity(intent)
        }

        holder.emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${pg.email}")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pgList.size
}
