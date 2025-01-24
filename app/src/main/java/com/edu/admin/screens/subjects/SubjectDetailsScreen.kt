package com.edu.admin.screens.subjects

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
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.edu.admin.R
import com.edu.admin.models.Lesson
import com.edu.admin.models.Subject
import com.edu.admin.screens.lessons.LessonDeleteBottomSheet
import com.edu.admin.screens.lessons.LessonDetailAddEdit
import com.edu.admin.screens.lessons.TheoriesListScreen
import com.edu.admin.utils.loadScreen
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class SubjectDetailsScreen(subject: Subject) : Fragment(),
    LessonDeleteBottomSheet.OnButtonClickListener {

    private var subject: Subject

    init {
        this.subject = subject
    }

    private lateinit var rcLessons: RecyclerView
    private lateinit var tvEmptyLessonList: TextView
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddLesson: ImageView
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rcLessons = view.findViewById(R.id.rvLessons)
        tvEmptyLessonList = view.findViewById(R.id.tvEmptylessonList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddLesson = view.findViewById(R.id.ivAddlesson)
        ivAddLesson.setOnClickListener {
            loadScreen(requireActivity(), LessonDetailAddEdit(subject.id, ""), "Type", "Add")
        }
        loadlessonList()
    }

    override fun onResume() {
        super.onResume()
        Log.e("Test", "onResume Called")
    }

    fun getAllLessons(subjectId: String, onLessonsRetrieved: (List<Lesson>) -> Unit) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val lessonRef = db.collection("subjects")
                .document(subjectId)
                .collection("lessons")
            lessonRef
                .get()
                .addOnSuccessListener { result ->
                    val lessonList = mutableListOf<Lesson>()
                    for (document in result) {
                        val lesson = document.toObject(Lesson::class.java)
                        lessonList.add(lesson)
                    }
                    onLessonsRetrieved(lessonList)
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error getting lessons: $e")
                    progressBar.visibility = View.GONE
                }
        } catch (e:Exception) {
            progressBar.visibility = View.GONE
            tvEmptyLessonList.visibility = View.VISIBLE
            rcLessons.visibility = View.GONE
        }
    }

    fun deleteLesson(subjectId: String, lessonId: String) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val lessonRef = db.collection("subjects").document(subjectId)
                .collection("lessons")
            lessonRef
                .whereEqualTo("id", lessonId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // For each matching document, delete it
                        lessonRef
                            .document(lessonId)
                            .delete()
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "lesson successfully deleted!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                loadlessonList()
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
            tvEmptyLessonList.visibility = View.VISIBLE
            rcLessons.visibility = View.GONE
        }
    }

    fun loadlessonList() {
        Log.e("Test", "LoadsubjectList Called()")
        getAllLessons(subject.id) { lessons ->
            lessons.forEach {
                Log.e("lessons", "lesson - $it")
            }
            if (lessons.size > 0) {
                tvEmptyLessonList.visibility = View.GONE
                rcLessons.visibility = View.VISIBLE
                rcLessons.layoutManager = LinearLayoutManager(context)
                rcLessons.adapter = LessonsListAdapter(
                    requireContext(),
                    requireActivity(),
                    parentFragmentManager,
                    fragment,
                    subject,
                    lessons
                )
            } else {
                tvEmptyLessonList.visibility = View.VISIBLE
                rcLessons.visibility = View.GONE
            }
        }
    }

    override fun onButtonClicked(subjectId: String, lessonId: String) {
        deleteLesson(subjectId, lessonId)
    }
}

class LessonsListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                         private val subject: Subject, private val lessons: List<Lesson>) :
    RecyclerView.Adapter<LessonsListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_lesson, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(subject, lesson = lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private val tvLessonName: TextView = itemView.findViewById(R.id.tvLessonName)
        private val layoutTheoryViewer: LinearLayout = itemView.findViewById(R.id.layoutTheoryViewer)
        private val layoutVideoPlayer: LinearLayout = itemView.findViewById(R.id.layoutVideoPlayer)
        private val layoutQuiz: LinearLayout = itemView.findViewById(R.id.layoutQuiz)
        private val layoutDelete: LinearLayout = itemView.findViewById(R.id.layoutDelete)
        private val lessonHolder: MaterialCardView = itemView.findViewById(R.id.lessonHolder)

        fun bind(subject: Subject, lesson: Lesson) {

            tvLessonName.text = lesson.lessonTitle
            layoutTheoryViewer.setOnClickListener {
                loadScreen(activity, TheoriesListScreen(subject, lesson))
            }
            layoutVideoPlayer.setOnClickListener {

            }
            layoutQuiz.setOnClickListener {

            }
            layoutDelete.setOnClickListener {
                deleteLessonDialog(subject.id,lesson.id)
            }
            lessonHolder.setOnClickListener {

            }
        }

        fun deleteLessonDialog(subjectId: String, lessonId: String) {
            val bottomSheetFragment = LessonDeleteBottomSheet(subjectId, lessonId)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}