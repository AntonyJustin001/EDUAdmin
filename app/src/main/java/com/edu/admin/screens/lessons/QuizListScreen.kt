package com.edu.admin.screens.lessons

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Lesson
import com.edu.admin.models.Quiz
import com.edu.admin.models.Subject
import com.edu.admin.screens.subjects.SubjectDetailAddEdit
import com.edu.admin.utils.loadScreen
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class QuizListScreen(subject: Subject, lesson: Lesson) : Fragment(),
    QuizDeleteBottomSheet.OnButtonClickListener {

    private var subject: Subject
    private var lesson: Lesson

    init {
        this.subject = subject
        this.lesson = lesson
    }

    private lateinit var rcQuiz: RecyclerView
    private lateinit var tvEmptyQuizList: TextView
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddQuiz: ImageView
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rcQuiz = view.findViewById(R.id.rvQuiz)
        tvEmptyQuizList = view.findViewById(R.id.tvEmptyQuizList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddQuiz = view.findViewById(R.id.ivAddQuiz)
        ivAddQuiz.setOnClickListener {
            loadScreen(requireActivity(), QuizAddEdit(subject, lesson, ""),"Type","Add")
        }

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

        loadQuizList(subject, lesson)
    }

    override fun onResume() {
        super.onResume()
        Log.e("Test", "onResume Called")
    }

    fun getAllQuiz(subject: Subject, lesson: Lesson, onQuizRetrieved: (List<Quiz>) -> Unit) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val QuizRef = db
                .collection("subjects").document(subject.id)
                .collection("lessons").document(lesson.id)
                .collection("quiz")
            QuizRef
                .get()
                .addOnSuccessListener { result ->
                    val QuizList = mutableListOf<Quiz>()
                    for (document in result) {
                        val Quiz = document.toObject(Quiz::class.java)
                        QuizList.add(Quiz)
                    }
                    onQuizRetrieved(QuizList)
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error getting Quiz: $e")
                    progressBar.visibility = View.GONE
                }
        } catch (e:Exception) {
            progressBar.visibility = View.GONE
            tvEmptyQuizList.visibility = View.VISIBLE
            rcQuiz.visibility = View.GONE
        }
    }

    fun deleteQuiz(subject: Subject,lessons: Lesson, QuizId: String) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val QuizRef = db.collection("subjects").document(subject.id)
                .collection("lessons").document(lessons.id)
                .collection("quiz")
            QuizRef
                .whereEqualTo("id", QuizId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // For each matching document, delete it
                        QuizRef
                            .document(QuizId)
                            .delete()
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "Quiz successfully deleted!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                loadQuizList(subject, lessons)
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
            tvEmptyQuizList.visibility = View.VISIBLE
            rcQuiz.visibility = View.GONE
        }
    }

    fun loadQuizList(subject: Subject, lesson: Lesson) {
        Log.e("Test", "LoadQuizList Called()")
        getAllQuiz(subject, lesson) { QuizList ->
            QuizList.forEach {
                Log.e("Quiz", "Quiz - $it")
            }
            if (QuizList.size > 0) {
                tvEmptyQuizList.visibility = View.GONE
                rcQuiz.visibility = View.VISIBLE
                rcQuiz.layoutManager = LinearLayoutManager(context)
                rcQuiz.adapter = QuizListAdapter(
                    requireContext(),
                    requireActivity(),
                    parentFragmentManager,
                    fragment,
                    subject,
                    lesson,
                    QuizList
                )
            } else {
                tvEmptyQuizList.visibility = View.VISIBLE
                rcQuiz.visibility = View.GONE
            }
        }
    }

    override fun onButtonClicked(subject: Subject, lesson: Lesson, QuizId: String) {
        deleteQuiz(subject, lesson, QuizId)
    }

    fun addQuiz(Quiz: Quiz) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects").document(subject.id)
            .collection("lessons").document(lesson.id)
            .collection("quiz")
            .document(Quiz.id)
            .set(Quiz)
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
}

class QuizListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val subject: Subject, private val lesson : Lesson,
                          private val Quiz: List<Quiz>) :
    RecyclerView.Adapter<QuizListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_quiz, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(subject, lesson, Quiz[position])
    }

    override fun getItemCount(): Int = Quiz.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private val tvQuizName: TextView = itemView.findViewById(R.id.tvQuizName)
        private val layoutDelete: LinearLayout = itemView.findViewById(R.id.layoutDelete)
        private val layoutEdit: LinearLayout = itemView.findViewById(R.id.layoutEdit)
        private val QuizHolder: MaterialCardView = itemView.findViewById(R.id.QuizHolder)

        fun bind(subject: Subject, lesson: Lesson, Quiz: Quiz) {

            tvQuizName.text = Quiz.question
            layoutDelete.setOnClickListener {
                deleteQuizDialog(subject,lesson, Quiz.id)
            }
            layoutEdit.setOnClickListener {

            }
            QuizHolder.setOnClickListener {

            }

        }

        fun deleteQuizDialog(subject: Subject, lesson: Lesson, QuizId: String) {
            val bottomSheetFragment = QuizDeleteBottomSheet(subject, lesson, QuizId)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}

class QuizDeleteBottomSheet(subject : Subject, lesson: Lesson, QuizId: String): BottomSheetDialogFragment() {

    private var subject: Subject
    private var lesson: Lesson
    private var QuizId: String
    init {
        this.subject = subject
        this.lesson = lesson
        this.QuizId = QuizId
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveQuiz: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(subject : Subject, lesson: Lesson, QuizId: String)
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
        return inflater.inflate(R.layout.delete_quiz_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveQuiz = view.findViewById<Button>(R.id.btnRemoveQuiz)
        btnRemoveQuiz.setOnClickListener {
            listener?.onButtonClicked(subject, lesson, QuizId)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}