package com.edu.admin.screens.lessons.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.admin.R
import com.edu.admin.models.Lesson
import com.edu.admin.models.Subject
import com.edu.admin.screens.lessons.LessonDeleteBottomSheet
import com.google.android.material.card.MaterialCardView
import com.edu.admin.screens.subjects.SubjectDeleteBottomSheet
import com.edu.admin.screens.subjects.SubjectDetailAddEdit
import com.edu.admin.screens.subjects.SubjectDetailsScreen
import com.edu.admin.utils.loadImageFromUrl
import com.edu.admin.utils.loadScreen
import com.health.care.models.Student

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
                //loadScreen(activity, SubjectDetailAddEdit(subject.subjectId),"Type","Edit")
            }
            layoutVideoPlayer.setOnClickListener {

            }
            layoutQuiz.setOnClickListener {

            }
            layoutDelete.setOnClickListener {
                deleteLessonDialog(subject.id,lesson.id)
            }
            lessonHolder.setOnClickListener {
                //loadScreen(activity, SubjectDetailsScreen(subject.subjectId))
            }
        }

        fun deleteLessonDialog(subjectId: String, lessonId: String) {
            val bottomSheetFragment = LessonDeleteBottomSheet(subjectId, lessonId)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}