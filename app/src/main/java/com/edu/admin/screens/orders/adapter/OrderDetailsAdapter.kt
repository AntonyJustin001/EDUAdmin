package com.edu.admin.screens.orders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.admin.R
import com.edu.admin.models.Order

data class Total(val tax:Double = 10.00, val deliveryCharge:Double = 20.00, val subTotal:Double = 0.00,
                 val total:Double = 0.00)

class OrderDetailsAdapter(context: Context, activity:FragmentActivity /*,checkOutScreen:CheckOutScreen*/ ,
                          order: Order, selectedItemListener: (String) -> Unit) :
    RecyclerView.Adapter<OrderDetailsAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    var order: Order
    //var orderDetailsRef:DocumentReference
    var selectedItemListener: (String) -> Unit

    var isItFirst = true
    init {

        this.context = context
        this.activity = activity
        this.order = order
        //this.orderDetailsRef = orderDetailsRef
        this.selectedItemListener = selectedItemListener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_order_details, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(order = order)
    }

    override fun getItemCount(): Int = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rcOrderedProductList: RecyclerView = itemView.findViewById(R.id.rcOrderedProductList)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val tvDevliveryCharge: TextView = itemView.findViewById(R.id.tvDevliveryCharge)
        val tvTax: TextView = itemView.findViewById(R.id.tvTax)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvSelectedPayment: TextView = itemView.findViewById(R.id.tvSelectedPayment)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val spOrderStatus: Spinner = itemView.findViewById(R.id.spOrderStatus)

        fun bind(order: Order) {
            tvCustomerName.text = order.customerName
            tvMobile.text = order.customerMobile
            tvAddress.text = order.shippingAddress
            tvSubtotal.text = order.subTotal
            tvTax.text = order.tax
            tvDevliveryCharge.text = order.deliveryCharge
            tvTotal.text = order.total
            tvSelectedPayment.text = order.selectedPayment

//            if(!isItFirst) {
//                when (order.orderStatus) {
//                    "Pending" -> {
//                        spOrderStatus.setSelection(0)
//                    }
//                    "Confirmed" -> {
//                        spOrderStatus.setSelection(1)
//                    }
//                    "Processing" -> {
//                        spOrderStatus.setSelection(2)
//                    }
//                    "Shipped" -> {
//                        spOrderStatus.setSelection(3)
//                    }
//                    "Canceled" -> {
//                        spOrderStatus.setSelection(4)
//                    }
//                }
//            }


            ArrayAdapter.createFromResource(
                context,
                R.array.order_status,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spOrderStatus.adapter = adapter
            }

                when (order.orderStatus) {
                    "Pending" -> {
                        spOrderStatus.setSelection(0)
                    }
                    "Confirmed" -> {
                        spOrderStatus.setSelection(1)
                    }
                    "Processing" -> {
                        spOrderStatus.setSelection(2)
                    }
                    "Shipped" -> {
                        spOrderStatus.setSelection(3)
                    }
                    "Canceled" -> {
                        spOrderStatus.setSelection(4)
                    }
                }

            spOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (isItFirst) {
                        isItFirst = false
                        return // Skip the first call, which happens when initializing the Spinner
                    }
                    val status = parent.getItemAtPosition(position).toString()
                    selectedItemListener(status)
//                    orderDetailsRef.update("orderStatus", status)
//                        .addOnSuccessListener {
////                            Snackbar.make(
////                                activity.requireView(),
////                                "Order Status Updated Successfully",
////                                Snackbar.LENGTH_LONG
////                            ).show()
//                            Log.e("Firestore", "Order Status Updated Successfully")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.w("Firestore", "Error updating document", e)
//                        }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

            rcOrderedProductList.layoutManager = LinearLayoutManager(context)
            rcOrderedProductList.adapter = OrderProductsAdapter(context, activity,order.cardItems)


        }

    }

}