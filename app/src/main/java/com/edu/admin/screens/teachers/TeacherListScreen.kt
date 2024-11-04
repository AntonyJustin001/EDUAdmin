package com.edu.admin.screens.teachers

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
import com.edu.admin.screens.teachers.adapter.TeachersListAdapter
import com.edu.admin.utils.loadScreen
import com.health.care.models.Teacher

class TeacherListScreen : Fragment(),TeacherDeleteBottomSheet.OnButtonClickListener {

    private lateinit var rcStudents: RecyclerView
    private lateinit var tvEmptyStudentList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddStudent: ImageView


    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teachers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rcStudents = view.findViewById(R.id.rvStudent)
        tvEmptyStudentList = view.findViewById(R.id.tvEmptyStudentList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddStudent = view.findViewById(R.id.ivAddStudent)
        ivAddStudent.setOnClickListener {
            loadScreen(requireActivity(), TeacherDetailAddEdit(""),"Type","Add")
        }

        loadTeachersList()

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
                    searchTeachers(lastWord) { searchedStudents ->
                        if(searchedStudents.size>0) {
                            tvEmptyStudentList.visibility = View.GONE
                            rcStudents.visibility = View.VISIBLE
                            rcStudents.layoutManager = LinearLayoutManager(context)
                            rcStudents.adapter = TeachersListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedStudents)
                        } else {
                            tvEmptyStudentList.visibility = View.VISIBLE
                            rcStudents.visibility = View.GONE
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

    fun getAllTeachers(onProductsRetrieved: (List<Teacher>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .get()
            .addOnSuccessListener { result ->
                val teacherList = mutableListOf<Teacher>()
                for (document in result) {
                    val teacher = document.toObject(Teacher::class.java)
                    teacherList.add(teacher)
                }
                onProductsRetrieved(teacherList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting teachers: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchTeachers(searchWord: String, onResult: (List<Teacher>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val studentsRef = db.collection("teacher")

        // Simple search based on exact match
        studentsRef.whereGreaterThanOrEqualTo("name", searchWord)
            .whereLessThanOrEqualTo("name", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val teachers = mutableListOf<Teacher>()
                for (document in documents) {
                    val teacher = document.toObject(Teacher::class.java)
                    teachers.add(teacher)
                }
                onResult(teachers)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                onResult(emptyList())
            }
    }

    fun deleteTeacher(nameToDelete:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "teacher"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("name", nameToDelete)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(nameToDelete)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "Teacher successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadTeachersList()
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

    fun loadTeachersList() {
        Log.e("Test","LoadTeacherList Called()")
        getAllTeachers { students ->
            students.forEach {
                Log.e("Students","Student - $it")
            }

            if(students.size>0) {
                tvEmptyStudentList.visibility = View.GONE
                rcStudents.visibility = View.VISIBLE
                rcStudents.layoutManager = LinearLayoutManager(context)
                rcStudents.adapter = TeachersListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, students)
            } else {
                tvEmptyStudentList.visibility = View.VISIBLE
                rcStudents.visibility = View.GONE
            }

        }
    }

    override fun onButtonClicked(teacher : Teacher) {
        deleteTeacher(teacher.name)
    }

}