import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.ApartmentListItemBinding
import com.example.homeswap_android.utils.Utils.updateLikeButton

class ApartmentAdapter(
    private val itemClickedCallback: (Apartment) -> Unit,
    private val onLikeClickListener: (Apartment) -> Unit
) : ListAdapter<Apartment, ApartmentAdapter.MyViewHolder>(ApartmentDiffCallback()) {

    val TAG = "ApartmentAdapter"

    class MyViewHolder(val binding: ApartmentListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ApartmentListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val apartment = getItem(position)
        val binding = holder.binding

        binding.apartmentTitleTV.text = apartment.title
        binding.apartmentCityTV.text = apartment.city

        apartment.coverPicture.let {
            binding.coverPictureIV.load(apartment.coverPicture)
        }

        binding.apartmentListCV.setOnClickListener {
            itemClickedCallback(apartment)
        }

        updateLikeButton(binding.favoriteBTN, apartment.liked)

        binding.favoriteBTN.setOnClickListener {
            onLikeClickListener(apartment)
        }

        apartment.rating?.let {
            binding.apartmentRatingBar.rating = it
        }

        binding.apartmentRatingTV.text = "${apartment.rating} (${apartment.reviewsCount} reviews)"
    }

    class ApartmentDiffCallback : DiffUtil.ItemCallback<Apartment>() {
        override fun areItemsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
            return oldItem.apartmentID == newItem.apartmentID
        }

        override fun areContentsTheSame(oldItem: Apartment, newItem: Apartment): Boolean {
            return oldItem == newItem
        }
    }
}