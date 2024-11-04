package com.edu.admin.screens.orders

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.edu.admin.models.Order
import com.edu.admin.screens.orders.adapter.OrdersListAdapter

class OrdersListScreen : Fragment() {

    private lateinit var rcorders: RecyclerView
    private lateinit var tvEmptyorderList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddorder: ImageView


    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rcorders = view.findViewById(R.id.rvOrders)
        tvEmptyorderList = view.findViewById(R.id.tvEmptyOrderList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddorder = view.findViewById(R.id.ivAddorder)
        ivAddorder.setOnClickListener {
            //loadScreen(requireActivity(), orderDetailAddEdit(""),"Type","Add")
        }

        loadorderList()


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text has been changed
                s?.let {
                    val words = it.split(" ")
                    val lastWord = if (words.isNotEmpty()) words.last() else ""
                    Log.e("Test","lastWord - $lastWord")
                    searchorders(lastWord) { searchedorders ->
                        if(searchedorders.size>0) {
                            tvEmptyorderList.visibility = View.GONE
                            rcorders.visibility = View.VISIBLE
                            rcorders.layoutManager = LinearLayoutManager(context)
                            rcorders.adapter = OrdersListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedorders)
                        } else {
                            tvEmptyorderList.visibility = View.VISIBLE
                            rcorders.visibility = View.GONE
                        }
                    }
//                    if(lastWord!="") {
//                    }
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.e("Test","onResume Called")
    }

    fun getAllorders(onordersRetrieved: (List<Order>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("order")
            .get()
            .addOnSuccessListener { result ->
                val orderList = mutableListOf<Order>()
                for (document in result) {
                    val order = document.toObject(Order::class.java)
                    orderList.add(order)
                }
                onordersRetrieved(orderList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting orders: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchorders(searchWord: String, onResult: (List<Order>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("order")

        // Simple search based on exact match
        ordersRef.whereGreaterThanOrEqualTo("orderId", searchWord)
            .whereLessThanOrEqualTo("orderId", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val orders = mutableListOf<Order>()
                for (document in documents) {
                    val order = document.toObject(Order::class.java)
                    orders.add(order)
                }
                onResult(orders)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }

    fun deleteorder(orderId:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "order"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("id", orderId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(orderId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "order successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadorderList()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
            }
    }

    fun loadorderList() {
        Log.e("Test","LoadorderList Called()")
        getAllorders { orders ->
            orders.forEach {
                Log.e("orders","order - $it")
            }

            if(orders.size>0) {
                tvEmptyorderList.visibility = View.GONE
                rcorders.visibility = View.VISIBLE
                rcorders.layoutManager = LinearLayoutManager(context)
                rcorders.adapter = OrdersListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, orders)
            } else {
                tvEmptyorderList.visibility = View.VISIBLE
                rcorders.visibility = View.GONE
            }

        }
    }

}