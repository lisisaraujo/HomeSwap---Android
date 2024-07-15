package com.example.homeswap_android.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.UserListItemBinding

class UserAdapter(
    private var users: List<UserData>,
    private val itemClickedCallback: (UserData) -> Unit
) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UserListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = users[position]
        val binding = holder.binding

        binding.userListNameTV.text = user.name
        binding.userListLocationTV.text = "${user.city}, ${user.country}"
        binding.ratingNumberTV.text = String.format("%.1f", user.rating)
        binding.userListSwapsTV.text = "${user.swaps} swaps"
        binding.userListProfilePicIV.load(user.profilePic)


        binding.userListCV.setOnClickListener {
            Log.d("ClickedUser", user.name)
            itemClickedCallback(user)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun updateUsers(newUsers: List<UserData>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
