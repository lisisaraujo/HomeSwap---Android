import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.ApartmentListItemBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel

class ApartmentAdapter(
    private var apartments: List<Apartment>,
    private val itemClickedCallback: (Apartment) -> Unit,
    private val onLikeClickListener: (Apartment) -> Unit,
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

        apartment.coverPicture.let {
            holder.binding.apartmentImageIV.load(apartment.coverPicture)
        }


        holder.binding.apartmentListCV.setOnClickListener {
            itemClickedCallback(apartment)
        }

        updateLikeButton(holder.binding.favoriteBTN, apartment.liked)

        holder.binding.favoriteBTN.setOnClickListener {
            onLikeClickListener(apartment)
        }
    }

    override fun getItemCount(): Int {
        return apartments.size
    }

    fun updateApartments(newApartments: List<Apartment>) {
        apartments = newApartments
        notifyDataSetChanged()
    }

    private fun updateLikeButton(button: ImageButton, liked: Boolean) {
        if (liked) {
            button.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            button.setImageResource(R.drawable.favorite_48px)
        }
    }
}