package com.edu.admin.screens.orders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.edu.admin.utils.loadImageFromUrl
import com.health.care.models.CartItem

class OrderProductsAdapter(val context: Context, activity: FragmentActivity, private val items: List<CartItem?>) :
    RecyclerView.Adapter<OrderProductsAdapter.ItemViewHolder>() {

    var activity: FragmentActivity
    var db: FirebaseFirestore

    init {
        this.activity = activity

        db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_order_products, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position]?: CartItem())
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)

        fun bind(product: CartItem) {
            loadImageFromUrl(context,product.productImageUrl,ivProductImage)
            tvProductTitle.text = product.name
            tvProductPrice.text = "${product.price}"
            tvProductQuantity.text = "${product.quantity}"
        }
    }
}