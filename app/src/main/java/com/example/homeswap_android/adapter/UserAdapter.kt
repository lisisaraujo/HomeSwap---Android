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


        holder.binding.userListNameTV.text = user.name
        holder.binding.userListEmailTV.text = user.email
        holder.binding.userListReviewsTV.text = user.reviews.size.toString()
        holder.binding.userListProfilePicIV.load(user.profilePic)

        holder.binding.userListCV.setOnClickListener {
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
