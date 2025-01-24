package com.edu.admin.screens.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.edu.admin.models.Order
import com.edu.admin.models.Product
import com.edu.admin.models.User
import com.edu.admin.screens.notification.NotificationCreate
import com.edu.admin.screens.orders.OrdersListScreen
import com.edu.admin.screens.students.StudentListScreen
import com.edu.admin.screens.subjects.SubjectListScreen
import com.edu.admin.utils.loadScreen

class HomeScreen : Fragment() {

    private lateinit var cvSubjects: MaterialCardView
    private lateinit var cvOrders: MaterialCardView
    private lateinit var cvStudents: MaterialCardView
    private lateinit var cvTeachers: MaterialCardView

    private lateinit var tvSubjectsCount: TextView
    private lateinit var tvStudentsCount: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var tvNotificationCount: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cvSubjects = view.findViewById(R.id.cvSubjects)
        cvOrders = view.findViewById(R.id.cvOrders)
        cvStudents = view.findViewById(R.id.cvStudents)
        tvSubjectsCount = view.findViewById(R.id.tvSubjectsCount)
        tvOrderCount = view.findViewById(R.id.tvOrderCount)
        tvStudentsCount = view.findViewById(R.id.tvStudentsCount)
        tvNotificationCount = view.findViewById(R.id.tvTeachersCount)
        cvTeachers =  view.findViewById(R.id.cvTeachers)

        cvSubjects.setOnClickListener {
            loadScreen(requireActivity(), SubjectListScreen())
        }
        cvOrders.setOnClickListener {
            loadScreen(requireActivity(), OrdersListScreen())
        }
        cvStudents.setOnClickListener {
            loadScreen(requireActivity(), StudentListScreen())
        }
        cvTeachers.setOnClickListener {
            loadScreen(requireActivity(), NotificationCreate())
        }

        getAllSubjects { subjects ->
            subjects.forEach {
                Log.e("Subjects","Subject - $it")
            }
            tvSubjectsCount.text = "${subjects.size}"
        }

        getAllOrders { orders ->
            orders.forEach {
                Log.e("orders","order - $it")
            }
            tvOrderCount.text = "${orders.size}"
        }

        getAllStudents { Students ->
            Students.forEach {
                Log.e("Students","customer - $it")
            }
            tvStudentsCount.text = "${Students.size}"
        }

    }

    fun getAllSubjects(onProductsRetrieved: (List<Product>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects")
            .get()
            .addOnSuccessListener { result ->
                val productList = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                onProductsRetrieved(productList)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting products: $e")
            }
    }

    fun getAllOrders(onProductsRetrieved: (List<Order>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("order")
            .get()
            .addOnSuccessListener { result ->
                val orders = mutableListOf<Order>()
                for (document in result) {
                    val order = document.toObject(Order::class.java)
                    orders.add(order)
                }
                onProductsRetrieved(orders)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting orders: $e")
            }
    }

    fun getAllStudents(onProductsRetrieved: (List<User>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("user")
            .get()
            .addOnSuccessListener { result ->
                val users = mutableListOf<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    users.add(user)
                }
                onProductsRetrieved(users)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting users: $e")
            }
    }

}