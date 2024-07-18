import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.ApartmentListItemBinding
import com.example.homeswap_android.utils.Utils.updateLikeButton
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.button.MaterialButton

class ApartmentAdapter(
    private var apartments: List<Apartment>,
    private val itemClickedCallback: (Apartment) -> Unit,
    private val onLikeClickListener: (Apartment) -> Unit,
) : RecyclerView.Adapter<ApartmentAdapter.MyViewHolder>() {

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


        val apartment = apartments[position]
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

        apartment.rating.let {
            binding.apartmentRatingBar.rating = apartment.rating!!
        }

       binding.apartmentRatingTV.text = "${apartment.rating} (${apartment.reviewsCount} reviews)"
    }

    override fun getItemCount(): Int {
        return apartments.size
    }

    fun updateApartments(newApartments: List<Apartment>) {
        apartments = newApartments
        notifyDataSetChanged()
    }

}