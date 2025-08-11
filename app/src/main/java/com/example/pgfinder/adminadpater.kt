import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pgfinder.R
import com.example.pgfinder.model.AdminPgModel

class AdminPgAdapter(
    private val pgList: MutableList<AdminPgModel>,
    private val onDeleteClick: (AdminPgModel) -> Unit
) : RecyclerView.Adapter<AdminPgAdapter.PgViewHolder>() {

    inner class PgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val location: TextView = itemView.findViewById(R.id.tvLocation)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val email: TextView = itemView.findViewById(R.id.tvEmail)
        val call: TextView = itemView.findViewById(R.id.tvCall)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admin_item_pg, parent, false)
        return PgViewHolder(view)
    }

    override fun onBindViewHolder(holder: PgViewHolder, position: Int) {
        val pg = pgList[position]
        holder.name.text = pg.name
        holder.location.text = pg.location
        holder.price.text = "₹${pg.price}"
        holder.email.text = pg.email
        holder.call.text = pg.call

        holder.btnDelete.setOnClickListener {
            onDeleteClick(pg)
        }
    }

    override fun getItemCount(): Int = pgList.size
}
