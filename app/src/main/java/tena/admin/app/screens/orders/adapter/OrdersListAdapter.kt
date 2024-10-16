package tena.admin.app.screens.orders.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import tena.admin.app.R
import tena.admin.app.models.Order
import tena.admin.app.models.Total
import tena.admin.app.screens.orders.OrderDetailsScreen
import tena.admin.app.utils.loadScreen

class OrdersListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                        private val items: List<Order>) :
    RecyclerView.Adapter<OrdersListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_order, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderPlacedDate: TextView = itemView.findViewById(R.id.tvOrderPlacedDate)
        private val tvOrderQty: TextView = itemView.findViewById(R.id.tvQty)
        private val tvOrderTotalPrice: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val orderHolder: LinearLayout = itemView.findViewById(R.id.orderHolder)
        private val tvProducts: TextView = itemView.findViewById(R.id.tvProducts)

        fun bind(order: Order) {
            var subTotal = 0.0
            var Total = 0.0
            var totalPrice: Total = Total()
            var TotalQty = 0
            var products = ""

            //Total Qty, Price, Product Name Calculation
            order.cardItems.forEach {
                TotalQty = TotalQty.plus(it?.quantity?:0)
                products = products + it?.name + ","
                subTotal = subTotal + (it?.quantity!! * it.price)
            }

            totalPrice = totalPrice.copy(subTotal = subTotal)
            Total = totalPrice.subTotal + totalPrice.tax +
                    totalPrice.deliveryCharge
            totalPrice = totalPrice.copy(total = Total)

            tvOrderId.text = order.orderId
            tvOrderPlacedDate.text = order.orderPlacedDate
            tvOrderQty.text = "${TotalQty}"
            tvOrderTotalPrice.text = "${totalPrice.total}"

            orderHolder.setOnClickListener {
                Log.e("Test","Order Id ${order.orderId}")
                loadScreen(activity, OrderDetailsScreen(order.orderId))
            }

            tvProducts.text = products

        }
    }
}