package com.edu.admin.screens.customers

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
import com.edu.admin.models.Customer
import com.edu.admin.screens.customers.adapter.CustomersListAdapter

class CustomersListScreen : Fragment() {

    private lateinit var rccustomers: RecyclerView
    private lateinit var tvEmptycustomerList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddcustomer: ImageView
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rccustomers = view.findViewById(R.id.rvCustomers)
        tvEmptycustomerList = view.findViewById(R.id.tvEmptyCustomerList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddcustomer = view.findViewById(R.id.ivAddcustomer)
        ivAddcustomer.setOnClickListener {
            //loadScreen(requireActivity(), customerDetailAddEdit(""),"Type","Add")
        }

        loadcustomerList()


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
                    searchcustomers(lastWord) { searchedcustomers ->
                        if(searchedcustomers.size>0) {
                            tvEmptycustomerList.visibility = View.GONE
                            rccustomers.visibility = View.VISIBLE
                            rccustomers.layoutManager = LinearLayoutManager(context)
                            rccustomers.adapter = CustomersListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedcustomers)
                        } else {
                            tvEmptycustomerList.visibility = View.VISIBLE
                            rccustomers.visibility = View.GONE
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

    fun getAllcustomers(oncustomersRetrieved: (List<Customer>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .get()
            .addOnSuccessListener { result ->
                val customerList = mutableListOf<Customer>()
                for (document in result) {
                    val customer = document.toObject(Customer::class.java)
                    customerList.add(customer)
                }
                oncustomersRetrieved(customerList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting customers: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchcustomers(searchWord: String, onResult: (List<Customer>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val customersRef = db.collection("user")

        // Simple search based on exact match
        customersRef.whereGreaterThanOrEqualTo("name", searchWord)
            .whereLessThanOrEqualTo("name", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val customers = mutableListOf<Customer>()
                for (document in documents) {
                    val customer = document.toObject(Customer::class.java)
                    customers.add(customer)
                }
                onResult(customers)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }

    fun deletecustomer(customerId:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "customer"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("id", customerId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(customerId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "customer successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadcustomerList()
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

    fun loadcustomerList() {
        Log.e("Test","LoadcustomerList Called()")
        getAllcustomers { customers ->
            customers.forEach {
                Log.e("customers","customer - $it")
            }

            if(customers.size>0) {
                tvEmptycustomerList.visibility = View.GONE
                rccustomers.visibility = View.VISIBLE
                rccustomers.layoutManager = LinearLayoutManager(context)
                rccustomers.adapter = CustomersListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, customers)
            } else {
                tvEmptycustomerList.visibility = View.VISIBLE
                rccustomers.visibility = View.GONE
            }

        }
    }

}