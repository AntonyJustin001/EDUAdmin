package com.edu.admin.screens.students

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Student
import com.edu.admin.screens.subjects.SubjectDetailAddEdit
import com.edu.admin.utils.loadScreen
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class StudentListScreen : Fragment(), StudentDeleteBottomSheet.OnButtonClickListener {

    private lateinit var rcStudents: RecyclerView
    private lateinit var tvEmptyStudentList: TextView
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_students_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rcStudents = view.findViewById(R.id.rvStudent)
        tvEmptyStudentList = view.findViewById(R.id.tvEmptyStudentList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        loadStudentList()

    }

    override fun onResume() {
        super.onResume()
        Log.e("Test","onResume Called")
    }

    override fun onButtonClicked(studentId: String) {
        deleteStudent(studentId)
    }

    fun deleteStudent(studentId: String) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val StudentRef = db.collection("students")
            StudentRef
                .whereEqualTo("id", studentId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // For each matching document, delete it
                        StudentRef
                            .document(studentId)
                            .delete()
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "Student successfully deleted!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                loadStudentList()
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "Something went wrong try again",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Snackbar.make(
                        requireView(),
                        "Something went wrong try again",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            tvEmptyStudentList.visibility = View.VISIBLE
            rcStudents.visibility = View.GONE
        }
    }
    fun getAllStudents(onStudentsRetrieved: (List<Student>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("student")
            .get()
            .addOnSuccessListener { result ->
                val StudentList = mutableListOf<Student>()
                for (document in result) {
                    val Student = document.toObject(Student::class.java)
                    StudentList.add(Student)
                }
                onStudentsRetrieved(StudentList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting Students: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchStudents(searchWord: String, onResult: (List<Student>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val StudentsRef = db.collection("students")

        // Simple search based on exact match
        StudentsRef.whereGreaterThanOrEqualTo("name", searchWord)
            .whereLessThanOrEqualTo("name", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val Students = mutableListOf<Student>()
                for (document in documents) {
                    val Student = document.toObject(Student::class.java)
                    Students.add(Student)
                }
                onResult(Students)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }

    fun loadStudentList() {
        Log.e("Test","LoadStudentList Called()")
        getAllStudents { Students ->
            Students.forEach {
                Log.e("Students","Student - $it")
            }

            if(Students.size>0) {
                tvEmptyStudentList.visibility = View.GONE
                rcStudents.visibility = View.VISIBLE
                rcStudents.layoutManager = LinearLayoutManager(context)
                rcStudents.adapter = StudentListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, Students)
            } else {
                tvEmptyStudentList.visibility = View.VISIBLE
                rcStudents.visibility = View.GONE
            }

        }
    }

}

class StudentListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                           private val items: List<Student>) :
    RecyclerView.Adapter<StudentListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_student_list, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        //private val layoutDelete: LinearLayout = itemView.findViewById(R.id.layoutDelete)
        private val StudentHolder: MaterialCardView = itemView.findViewById(R.id.StudentHolder)

        fun bind(student: Student) {

            tvStudentName.text = student.name
//            layoutDelete.setOnClickListener {
//            }
            
            StudentHolder.setOnClickListener {
                loadScreen(activity, StudentDetailsScreen(student.userId))
            }

        }
    }
}

class StudentDeleteBottomSheet(studentId: String): BottomSheetDialogFragment() {
    
    private var studentId: String
    init {
        this.studentId = studentId
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveStudent: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(studentId: String)
    }

    private var listener: OnButtonClickListener? = null

    // Attach the listener in onAttach
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as OnButtonClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.delete_student_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveStudent = view.findViewById<Button>(R.id.btnRemoveStudent)
        btnRemoveStudent.setOnClickListener {
            listener?.onButtonClicked(studentId)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}