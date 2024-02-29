package com.example.petsapp26

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StaffListAdapter(private val staffList: MutableList<User>,private val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<StaffListAdapter.ViewHolder>() {
    init {
        Log.d(TAG, "StaffListAdapter initialized")
    }

    // Call this method to clear the adapter's data
    fun clear() {
        staffList.clear()
        notifyDataSetChanged()
        Log.d(TAG, "Staff list cleared")
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.textViewUsername)
        val roleTextView: TextView = itemView.findViewById(R.id.textViewRole)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = staffList[position]
                    onItemClick(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "Activated stafflistadapter")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val staff = staffList[position]
        holder.usernameTextView.text = staff.username
        holder.roleTextView.text = staff.role

        // Log the username and role for each item
        Log.d(TAG, "Username: ${staff.username}, Role: ${staff.role}")
    }

    override fun getItemCount(): Int = staffList.size
    // Additional function to update data
    companion object {
        private const val TAG = "StaffListAdapter"
    }
}
