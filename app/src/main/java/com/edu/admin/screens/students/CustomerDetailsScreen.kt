package com.edu.admin.screens.students

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
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.edu.admin.models.Student
import com.edu.admin.screens.students.adapter.CustomerDetailsAdapter

class CustomerDetailsScreen(customerId:String) : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rccustomerDetails: RecyclerView
    lateinit var db: FirebaseFirestore
    private lateinit var progressBar: LottieAnimationView
    private var customerId = ""

    init {
        this.customerId = customerId
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        rccustomerDetails = view.findViewById(R.id.rccustomerDetails)
        rccustomerDetails.layoutManager = LinearLayoutManager(context)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)

        val customerDetailRef = db.collection("user").document(customerId)
        customerDetailRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {

                val customer = snapshot.toObject(Student::class.java)
                Log.e("Firestore", "customer Details - $customer")
                if(isAdded) {
                    if(customer != null){
                        rccustomerDetails.adapter = CustomerDetailsAdapter(
                            requireContext(),
                            requireActivity(),
                            customer
                        )
                    } else {
                        Log.e("Firestore", "customer data is null")
                        //requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                progressBar.visibility  = View.GONE

            } else {
                Log.e("Firestore", "customer data is null")
            }
        }
    }
}