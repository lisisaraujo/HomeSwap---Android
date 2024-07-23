import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.MyApartmentsListItemBinding
import com.example.homeswap_android.ui.personal.options.MyListingsFragmentDirections

class EditApartmentAdapter(
    private val itemClickedCallback: (Apartment) -> Unit
) : ListAdapter<Apartment, EditApartmentAdapter.MyViewHolder>(ApartmentDiffCallback()) {

    class MyViewHolder(val binding: MyApartmentsListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MyApartmentsListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val apartment = getItem(position)
        Log.d("ApartmentID", apartment.apartmentID)

        holder.binding.apartmentTitleTV.text = apartment.title
        holder.binding.apartmentCityTV.text = apartment.city

        holder.binding.coverPictureIV.load(apartment.coverPicture)

        holder.binding.myApartmentsCV.setOnClickListener {
            Log.d("ClickedApartment", apartment.title)
            itemClickedCallback(apartment)
        }

        holder.binding.editButton.setOnClickListener {
            it.findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(
                    apartment.apartmentID
                )
            )
        }
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