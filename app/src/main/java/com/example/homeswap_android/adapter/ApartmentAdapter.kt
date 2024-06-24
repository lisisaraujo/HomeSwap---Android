import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.ApartmentListItemBinding

class ApartmentAdapter(
    private var apartments: List<Apartment>,
    private val itemClickedCallback: (Apartment) -> Unit,
    private val onLikeClickListener: (Apartment) -> Unit
) : RecyclerView.Adapter<ApartmentAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ApartmentListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ApartmentListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val apartment = apartments[position]
        holder.binding.apartmentTitleTV.text = apartment.title
        holder.binding.apartmentCityTV.text = apartment.city

        if (apartment.pictures.isNotEmpty()) {
            holder.binding.apartmentImageIV.load(apartment.pictures.first())
        } else {
            holder.binding.apartmentImageIV.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.binding.apartmentListCV.setOnClickListener {
            itemClickedCallback(apartment)
        }

        updateLikeButton(holder.binding.favoriteBTN, apartment.liked)

        holder.binding.favoriteBTN.setOnClickListener {
            Log.d("LikeClicked", "Like button clicked for apartment ${apartment.apartmentID}")
            onLikeClickListener(apartment)
        }
    }

    override fun getItemCount(): Int {
        return apartments.size
    }

    fun updateApartments(newApartments: List<Apartment>) {
        Log.d("ApartmentAdapter", "Updating apartments, new size: ${newApartments.size}")
        apartments = newApartments
        notifyDataSetChanged()
    }

    private fun updateLikeButton(button: ImageButton, isLiked: Boolean) {
        if (isLiked) {
            button.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            button.setImageResource(R.drawable.favorite_48px)
        }
    }
}