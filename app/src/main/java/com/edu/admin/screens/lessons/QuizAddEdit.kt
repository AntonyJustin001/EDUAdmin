package com.edu.admin.screens.lessons

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Quiz
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class QuizAddEdit(quizId: String) : Fragment() {

    private var quizId: String

    init {
        this.quizId = quizId
    }

    private lateinit var etQuestion: EditText
    private lateinit var etOptionA: EditText
    private lateinit var etOptionB: EditText
    private lateinit var etOptionC: EditText
    private lateinit var etAnswer: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: LottieAnimationView
    
    private var type = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvAddEditQuizHeading: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type") ?: ""
        return inflater.inflate(R.layout.fragment_quiz_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        etQuestion = view.findViewById(R.id.etQuestion)
        etOptionA = view.findViewById(R.id.etOptionA)
        etOptionB = view.findViewById(R.id.etOptionB)
        etOptionC = view.findViewById(R.id.etOptionC)
        etAnswer = view.findViewById(R.id.etAnswer)
        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            if (etQuestion.text.toString() != "") {
                if (etOptionA.text.toString() != "") {
                    if (etOptionB.text.toString() != "") {
                        if (etOptionC.text.toString() != "") {
                            if (etAnswer.text.toString() != "") {
                                progressBar.visibility = View.VISIBLE
                                if (type == "Edit") {
                                    val updatedDetails = hashMapOf(
                                        "question" to etQuestion.text.toString(),
                                        "optionA" to etOptionA.text.toString(),
                                        "optionB" to etOptionA.text.toString(),
                                        "optionC" to etOptionB.text.toString(),
                                        "answer" to etAnswer.text.toString(),
                                    )
                                    updateQuizDetailsByName(quizId, updatedDetails)
                                } else {
                                    addQuiz(
                                        Quiz(
                                            id = UUID.randomUUID().toString(),
                                            question = etQuestion.text.toString(),
                                            optionA = etOptionA.text.toString(),
                                            optionB = etOptionB.text.toString(),
                                            optionC = etOptionC.text.toString(),
                                            answer = etAnswer.text.toString(),
                                        )
                                    )
                                }
                            } else {
                                Snackbar.make(
                                    requireView(),
                                    "Please Enter Answer",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Snackbar.make(
                                requireView(),
                                "Please Enter OptionC",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }   
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Please Enter OptionB",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter OptionA",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter Question", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvAddEditQuizHeading = view.findViewById(R.id.tvAddEditQuizHeading)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
        }

        if (type == "Edit") {
            tvAddEditQuizHeading.text = "Edit Quiz"
            progressBar.visibility = View.VISIBLE
            getQuizByName(quizId) { Quiz ->
                Quiz?.let {
                    println("GetQuiz - Quiz found: $Quiz")
                    etQuestion.setText(Quiz.question)
                    etOptionA.setText(Quiz.optionA)
                    etOptionB.setText(Quiz.optionB)
                    etOptionC.setText(Quiz.optionC)
                    etAnswer.setText(Quiz.answer)
                    progressBar.visibility = View.GONE
                } ?: run {
                    println("GetQuiz - Quiz not found")
                }
            }
        } else {
            tvAddEditQuizHeading.text = "Add Quiz"
        }

    }

    fun addQuiz(quiz: Quiz) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Quizes")
            .document(quiz.id)
            .set(quiz)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Quiz Added Successfully", Snackbar.LENGTH_LONG)
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

    fun getQuizByName(QuizId: String, onQuizRetrieved: (Quiz?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Quizes")
            .document(QuizId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val Quiz = document.toObject(Quiz::class.java)
                    onQuizRetrieved(Quiz)
                } else {
                    onQuizRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting Quiz: $e")
            }
    }
    fun updateQuizDetailsByName(QuizId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("Quizes")
            .whereEqualTo("id", QuizId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        db.collection("Quizes")
                            .document(QuizId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                Snackbar.make(
                                    requireView(),
                                    "Quiz Edited Successfully",
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