package com.edu.admin.screens.customers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.admin.R
import com.edu.admin.models.Customer
import com.edu.admin.screens.customers.CustomerDetailsScreen
import com.edu.admin.utils.loadScreen

class CustomersListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                           private val items: List<Customer>) :
    RecyclerView.Adapter<CustomersListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_customer, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmailId: TextView = itemView.findViewById(R.id.tvEmailId)
        private val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val customerHolder: LinearLayout = itemView.findViewById(R.id.customerHolder)

        fun bind(customer: Customer) {

            tvName.text = customer.name
            tvEmailId.text = customer.emailId
            tvMobile.text = customer.mobileNo
            tvStatus.text = customer.status

            customerHolder.setOnClickListener {
                //Log.e("Test","Order Id ${order.orderId}")
                loadScreen(activity, CustomerDetailsScreen(customer.userId))
            }

        }
    }
}