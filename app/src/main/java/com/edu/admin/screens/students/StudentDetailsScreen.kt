package com.edu.admin.screens.students

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Student
import com.google.firebase.firestore.FirebaseFirestore

class StudentDetailsScreen(studentId:String) : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    lateinit var db: FirebaseFirestore
    private lateinit var progressBar: LottieAnimationView
    private var studentId = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentMobile: TextView
    private lateinit var tvStudentMail: TextView
    private lateinit var tvTotalPoints: TextView
    //private lateinit var tvNoOfAttempts: TextView
    private lateinit var tvCompletedLevel: TextView

    init {
        this.studentId = studentId
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)

        tvStudentName = view.findViewById(R.id.tvStudentName)
        tvStudentMobile = view.findViewById(R.id.tvStudentMobile)
        tvStudentMail = view.findViewById(R.id.tvStudentMail)
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints)
        //tvNoOfAttempts = view.findViewById(R.id.tvNoOfAttempts)
        tvCompletedLevel = view.findViewById(R.id.tvCompletedLevel)


        val StudentDetailRef = db.collection("student").document(studentId)
        StudentDetailRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }


            if (snapshot != null && snapshot.exists()) {

                val student = snapshot.toObject(Student::class.java)
                Log.e("Firestore", "Student Details - $student")
                if(isAdded) {
                    if(student != null){
                        tvStudentName.setText(student.name)
                        tvStudentMobile.setText(student.mobileNo)
                        tvStudentMail.setText(student.emailId)
                        tvTotalPoints.setText("${student.pointsEarned}")
                        val rawData = student.completedHistory
                        val histories = rawData.split("-").filter { it.isNotBlank() }
                        val formattedHistories = histories.joinToString("\n") { it.trim() }
                        tvCompletedLevel.setText(formattedHistories)
                    } else {
                        Log.e("Firestore", "Student data is null")
                        //requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                progressBar.visibility  = View.GONE

            } else {
                Log.e("Firestore", "Student data is null")
            }
        }
    }
}