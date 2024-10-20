package tena.admin.app.screens.orders

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import tena.admin.app.R
import tena.admin.app.models.Order
import tena.admin.app.models.Total
import tena.admin.app.screens.orders.adapter.OrderDetailsAdapter
import tena.health.care.models.CartItem

class OrderDetailsScreen(orderId:String) : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rcOrderDetails: RecyclerView
    lateinit var db: FirebaseFirestore
    val cartItems = mutableListOf<CartItem?>()
    private lateinit var progressBar: LottieAnimationView
    private var orderId = ""

    init {
        this.orderId = orderId
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        rcOrderDetails = view.findViewById(R.id.rcOrderDetails)
        rcOrderDetails.layoutManager = LinearLayoutManager(context)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)

        val orderDetailRef = db.collection("order").document(orderId)
        orderDetailRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                val order = snapshot.toObject(Order::class.java)
                Log.e("Firestore", "Order Details - $order")
                if(isAdded) {
                    if(order != null){
                        rcOrderDetails.adapter = OrderDetailsAdapter(
                            requireContext(),
                            requireActivity(),
                            order
                        ) { status ->
                            orderDetailRef.update("orderStatus", status)
                                .addOnSuccessListener {
                                    Snackbar.make(
                                        requireView(),
                                        "Order Status Updated Successfully",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error updating document", e)
                                }
                        }
                    } else {
                        Log.e("Firestore", "Order data is null")
                        //requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                progressBar.visibility  = View.GONE

            } else {
                Log.e("Firestore", "Order data is null")
            }
        }
    }

    fun TotalCalculation(): Total {
        var totalPrice: Total = Total()

        var subTotal = 0.0
        var Total = 0.0

        //Calculate Total
        cartItems.forEach {
            subTotal = subTotal + (it?.quantity!! * it.price)
        }
        totalPrice = totalPrice.copy(subTotal = subTotal)
        Total = totalPrice.subTotal + totalPrice.tax +
                totalPrice.deliveryCharge
        totalPrice = totalPrice.copy(total = Total)
        return totalPrice
    }
}