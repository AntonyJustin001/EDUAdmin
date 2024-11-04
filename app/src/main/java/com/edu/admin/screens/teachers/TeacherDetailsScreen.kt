package com.edu.admin.screens.teachers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.edu.admin.utils.loadImageFromUrl
import com.health.care.models.Teacher

class TeacherDetailsScreen(student :String) : Fragment() {

    private var student :String
    init {
        this.student = student
    }

    private lateinit var progressBar: LottieAnimationView
    private lateinit var tvStudentname: TextView
    private lateinit var tvSection: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvStudentEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var profileImage: ImageView
    private var imageUrl = ""

    private lateinit var ivBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        tvStudentname = view.findViewById(R.id.tvStudentname)
        tvSection = view.findViewById(R.id.tvSection)
        tvMobile = view.findViewById(R.id.tvMobile)
        tvStudentEmail = view.findViewById(R.id.tvStudentEmail)
        tvAddress = view.findViewById(R.id.tvAddress)
        profileImage = view.findViewById(R.id.profileImage)

        progressBar.visibility = View.VISIBLE
        getTeacherByName(student) { student ->
            student?.let {
                println("GetStudent - Student found: $student")
                tvStudentname.setText(student.name)
                tvAddress.setText(student.address)
                tvMobile.setText(student.mobileNo)
                tvSection.setText(student.section)
                tvStudentEmail.setText(student.emailId)
                loadImageFromUrl(requireContext(),student.profilePic,profileImage)
                imageUrl = student.profilePic
                progressBar.visibility = View.GONE
            } ?: run {
                println("GetStudent - Student not found")
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    fun getTeacherByName(studentName: String, onProductRetrieved: (Teacher?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(studentName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val teacher = document.toObject(Teacher::class.java)
                    onProductRetrieved(teacher)
                } else {
                    onProductRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting student: $e")
            }
    }

}