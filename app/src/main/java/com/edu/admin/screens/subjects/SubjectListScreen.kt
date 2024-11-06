package com.edu.admin.screens.subjects

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
import com.edu.admin.models.Subject
import com.edu.admin.screens.subjects.adapter.SubjectsListAdapter
import com.edu.admin.utils.loadScreen

class SubjectListScreen : Fragment(),SubjectDeleteBottomSheet.OnButtonClickListener {

    private lateinit var rcsubjects: RecyclerView
    private lateinit var tvEmptysubjectList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddsubject: ImageView


    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rcsubjects = view.findViewById(R.id.rvsubject)
        tvEmptysubjectList = view.findViewById(R.id.tvEmptysubjectList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddsubject = view.findViewById(R.id.ivAddsubject)
        ivAddsubject.setOnClickListener {
            loadScreen(requireActivity(), SubjectDetailAddEdit(""),"Type","Add")
        }

        loadsubjectList()


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
                    searchsubjects(lastWord) { searchedsubjects ->
                        if(searchedsubjects.size>0) {
                            tvEmptysubjectList.visibility = View.GONE
                            rcsubjects.visibility = View.VISIBLE
                            rcsubjects.layoutManager = LinearLayoutManager(context)
                            rcsubjects.adapter = SubjectsListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedsubjects)
                        } else {
                            tvEmptysubjectList.visibility = View.VISIBLE
                            rcsubjects.visibility = View.GONE
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

    fun getAllsubjects(onsubjectsRetrieved: (List<Subject>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects")
            .get()
            .addOnSuccessListener { result ->
                val subjectList = mutableListOf<Subject>()
                for (document in result) {
                    val subject = document.toObject(Subject::class.java)
                    subjectList.add(subject)
                }
                onsubjectsRetrieved(subjectList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting subjects: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchsubjects(searchWord: String, onResult: (List<Subject>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val subjectsRef = db.collection("subjects")

        // Simple search based on exact match
        subjectsRef.whereGreaterThanOrEqualTo("subjectTitle", searchWord)
            .whereLessThanOrEqualTo("subjectTitle", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val subjects = mutableListOf<Subject>()
                for (document in documents) {
                    val subject = document.toObject(Subject::class.java)
                    subjects.add(subject)
                }
                onResult(subjects)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }

    fun deletesubject(subjectId:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "subjects"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("id", subjectId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(subjectId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "subject successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadsubjectList()
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

    fun loadsubjectList() {
        Log.e("Test","LoadsubjectList Called()")
        getAllsubjects { subjects ->
            subjects.forEach {
                Log.e("subjects","subject - $it")
            }

            if(subjects.size>0) {
                tvEmptysubjectList.visibility = View.GONE
                rcsubjects.visibility = View.VISIBLE
                rcsubjects.layoutManager = LinearLayoutManager(context)
                rcsubjects.adapter = SubjectsListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, subjects)
            } else {
                tvEmptysubjectList.visibility = View.VISIBLE
                rcsubjects.visibility = View.GONE
            }

        }
    }

    override fun onButtonClicked(subject : Subject) {
        deletesubject(subject.id)
    }

}