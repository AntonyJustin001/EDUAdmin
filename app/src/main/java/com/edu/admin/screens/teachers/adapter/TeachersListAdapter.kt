package com.edu.admin.screens.teachers.adapter

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
import com.google.android.material.card.MaterialCardView
import com.edu.admin.R
import com.edu.admin.screens.teachers.TeacherDeleteBottomSheet
import com.edu.admin.screens.teachers.TeacherDetailAddEdit
import com.edu.admin.screens.teachers.TeacherDetailsScreen
import com.edu.admin.utils.loadImageFromUrl
import com.edu.admin.utils.loadScreen
import com.health.care.models.Teacher

class TeachersListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val items: List<Teacher>) :
    RecyclerView.Adapter<TeachersListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_teacher, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val studentProfile: ImageView = itemView.findViewById(R.id.ivStudentPic)
        private val studentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val studentSection: TextView = itemView.findViewById(R.id.tvStudentSection)
        private val studentDelete: ImageView = itemView.findViewById(R.id.ivDeleteStudent)
        private val studentEdit: ImageView = itemView.findViewById(R.id.ivEditStudent)
        private val studentHolder: MaterialCardView = itemView.findViewById(R.id.studentHolder)

        fun bind(teacher: Teacher) {
            loadImageFromUrl(context,teacher.profilePic,studentProfile)
            studentName.text = teacher.name
            studentSection.text = teacher.section
            studentDelete.setOnClickListener {
                deleteTeacherDialog(teacher)
            }
            studentEdit.setOnClickListener {
                loadScreen(activity, TeacherDetailAddEdit(teacher.name),"Type","Edit")
            }
            studentHolder.setOnClickListener {
                loadScreen(activity, TeacherDetailsScreen(teacher.name))
            }
        }

        fun deleteTeacherDialog(teacher: Teacher) {
            val bottomSheetFragment = TeacherDeleteBottomSheet(teacher)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}