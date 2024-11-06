package com.edu.admin.screens.subjects.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.admin.R
import com.edu.admin.models.Subject
import com.google.android.material.card.MaterialCardView
import com.edu.admin.screens.subjects.SubjectDeleteBottomSheet
import com.edu.admin.screens.subjects.SubjectDetailAddEdit
import com.edu.admin.screens.subjects.SubjectDetailsScreen
import com.edu.admin.utils.loadImageFromUrl
import com.edu.admin.utils.loadScreen
import com.health.care.models.Student

class SubjectsListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val items: List<Subject>) :
    RecyclerView.Adapter<SubjectsListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_subject, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val subjectProfile: ImageView = itemView.findViewById(R.id.ivSubjectPic)
        private val subjectTitle: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val subjectDescription: TextView = itemView.findViewById(R.id.tvSubjectSection)
        private val subjectDelete: ImageView = itemView.findViewById(R.id.ivDeletesubject)
        private val subjectEdit: ImageView = itemView.findViewById(R.id.ivEditsubject)
        private val subjectHolder: MaterialCardView = itemView.findViewById(R.id.subjectHolder)

        fun bind(subject: Subject) {
            loadImageFromUrl(context,subject.imageUrl,subjectProfile)
            subjectTitle.text = subject.subjectTitle
            subjectDescription.text = subject.subjectDescription
            subjectDelete.setOnClickListener {
                deleteJobDialog(subject)
            }
            subjectEdit.setOnClickListener {
                loadScreen(activity, SubjectDetailAddEdit(subject.id),"Type","Edit")
            }
            subjectHolder.setOnClickListener {
                loadScreen(activity, SubjectDetailsScreen(subject))
            }
        }

        fun deleteJobDialog(subject: Subject) {
            val bottomSheetFragment = SubjectDeleteBottomSheet(subject)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}