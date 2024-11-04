package com.edu.admin.screens.teachers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.edu.admin.R
import com.health.care.models.Teacher

class TeacherDeleteBottomSheet(student : Teacher): BottomSheetDialogFragment() {

    private var teacher: Teacher
    init {
        this.teacher = student
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveStudent: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(teacher : Teacher)
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
        return inflater.inflate(R.layout.delete_teacher_bottom_sheet, container, false)
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
            listener?.onButtonClicked(teacher)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}