package com.edu.admin.screens.lessons

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Lesson
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class LessonDetailAddEdit(Lesson: String) : Fragment() {

    private var LessonId: String

    init {
        this.LessonId = Lesson
    }

    private lateinit var etLessonNameTitle: EditText
    private lateinit var etLessonDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etLessonStock: EditText
    private lateinit var etLessonSize: EditText
    private lateinit var btnStore: Button
    private lateinit var progressBar: LottieAnimationView
    private var imageUrl = ""

    private val PICK_IMAGE_REQUEST = 1

    private var type = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvAddEditLessonHeading: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type") ?: ""
        return inflater.inflate(R.layout.fragment_lesson_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etLessonNameTitle = view.findViewById(R.id.etLessonNameTitle)
        etLessonDescription = view.findViewById(R.id.etLessonDescription)
        etPrice = view.findViewById(R.id.etLessonPrice)
        etLessonSize = view.findViewById(R.id.etLessonSize)
        etLessonStock = view.findViewById(R.id.etLessonStock)
        btnStore = view.findViewById(R.id.btnSave)
        btnStore.setOnClickListener {
            if (etLessonNameTitle.text.toString() != "") {
                if (etLessonDescription.text.toString() != "") {
                    if (imageUrl != "") {
                        progressBar.visibility = View.VISIBLE
                        if (type == "Edit") {
                            val updatedDetails = hashMapOf(
                                "LessonTitle" to etLessonNameTitle.text.toString(),
                                "LessonDescription" to etLessonDescription.text.toString(),
                            )
                            updateLessonDetailsByName(LessonId, updatedDetails)
                        } else {
                            addLesson(
                                Lesson(
                                    id = UUID.randomUUID().toString(),
                                    lessonTitle = etLessonNameTitle.text.toString(),
                                    lessonDescription = etLessonDescription.text.toString()
                                )
                            )
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Please Pick Lesson Image",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter Lesson Description",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter Lesson Name", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvAddEditLessonHeading = view.findViewById(R.id.tvAddEditLessonHeading)

        if (type == "Edit") {
            tvAddEditLessonHeading.text = "Edit Lesson"
            progressBar.visibility = View.VISIBLE
            getLessonByName(LessonId) { Lesson ->
                Lesson?.let {
                    println("GetLesson - Lesson found: $Lesson")
                    etLessonNameTitle.setText(Lesson.lessonTitle)
                    etLessonDescription.setText(Lesson.lessonDescription)
                    progressBar.visibility = View.GONE
                } ?: run {
                    println("GetLesson - Lesson not found")
                }
            }
        } else {
            tvAddEditLessonHeading.text = "Add Lesson"
            etLessonNameTitle.isEnabled = true
        }

    }

    fun addLesson(Lesson: Lesson) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Lessons")
            .document(Lesson.id)
            .set(Lesson)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Lesson Added Successfully", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    requireView(),
                    "Something went wrong try again later",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }

    fun getLessonByName(LessonName: String, onLessonRetrieved: (Lesson?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Lessons")
            .document(LessonName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val Lesson = document.toObject(Lesson::class.java)
                    onLessonRetrieved(Lesson)
                } else {
                    onLessonRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting Lesson: $e")
            }
    }
    fun updateLessonDetailsByName(LessonId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("Lessons")
            .whereEqualTo("id", LessonId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        db.collection("Lessons")
                            .document(LessonId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                Snackbar.make(
                                    requireView(),
                                    "Lesson Edited Successfully",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                Snackbar.make(
                                    requireView(),
                                    "Something went wrong please try again",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    Snackbar.make(
                        requireView(),
                        "Something went wrong please try again",
                        Snackbar.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                    parentFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Snackbar.make(
                    requireView(),
                    "Something went wrong please try again",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}