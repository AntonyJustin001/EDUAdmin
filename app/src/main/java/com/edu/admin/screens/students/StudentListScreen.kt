package com.edu.admin.screens.students

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.edu.admin.utils.loadScreen
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class StudentListScreen : Fragment() {

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
        return inflater.inflate(R.layout.fragment_students_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
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
            //loadScreen(requireActivity(), StudentDetailAddEdit(""),"Type","Add")
        }

        loadStudentList()


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                try {
                    // This method is called after the text has been changed
                    s?.let {
                        val words = it.split(" ")
                        val lastWord = if (words.isNotEmpty()) words.last() else ""
                        Log.e("Test","lastWord - $lastWord")
                        searchStudents(lastWord) { searchedStudents ->
                            if(searchedStudents.size>0) {
                                tvEmptyStudentList.visibility = View.GONE
                                rcStudents.visibility = View.VISIBLE
                                rcStudents.layoutManager = LinearLayoutManager(context)
                                rcStudents.adapter = StudentListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedStudents)
                            } else {
                                tvEmptyStudentList.visibility = View.VISIBLE
                                rcStudents.visibility = View.GONE
                            }
                        }
//                    if(lastWord!="") {
//                    }
                    }
                } catch (e:Exception) {
                    Log.e("Exception", "${e.message}")
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.e("Test","onResume Called")
    }

    fun getAllStudents(onStudentsRetrieved: (List<Student>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("students")
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

    fun deleteStudent(StudentId:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "Student"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("id", StudentId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(StudentId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "Student successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadStudentList()
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
            .inflate(R.layout.rc_item_Student, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmailId: TextView = itemView.findViewById(R.id.tvEmailId)
        private val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val StudentHolder: LinearLayout = itemView.findViewById(R.id.StudentHolder)

        fun bind(Student: Student) {

            tvName.text = Student.name
            tvEmailId.text = Student.emailId
            tvMobile.text = Student.mobileNo
            tvStatus.text = Student.status

            StudentHolder.setOnClickListener {
                //Log.e("Test","Order Id ${order.orderId}")
                loadScreen(activity, StudentDetailsScreen(Student.userId))
            }

        }
    }
}