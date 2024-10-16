package tena.admin.app.screens.customers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tena.admin.app.R
import tena.admin.app.models.Customer
import tena.admin.app.screens.customers.CustomerDetailsScreen
import tena.admin.app.utils.loadScreen

class CustomerDetailsAdapter(context: Context, activity:FragmentActivity /*,checkOutScreen:CheckOutScreen*/ , customer: Customer) :
    RecyclerView.Adapter<CustomerDetailsAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    var customer: Customer

    init {

        this.context = context
        this.activity = activity
        this.customer = customer

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_customer_details, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(customer = customer)
    }

    override fun getItemCount(): Int = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvCustomerMail: TextView = itemView.findViewById(R.id.tvCustomerMail)
        private val tvCustomerMobile: TextView = itemView.findViewById(R.id.tvCustomerMobile)
        //private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(customer: Customer) {
            tvCustomerName.text = customer.name
            tvCustomerMail.text = customer.emailId
            tvCustomerMobile.text = customer.mobileNo
            //tvStatus.text = customer.status
        }

    }

}