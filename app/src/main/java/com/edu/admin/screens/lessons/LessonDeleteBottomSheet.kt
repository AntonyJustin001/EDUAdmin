package com.edu.admin.screens.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.edu.admin.R
import com.edu.admin.models.Lesson
import com.edu.admin.models.Subject

class LessonDeleteBottomSheet(subjectId : String, lessonId: String): BottomSheetDialogFragment() {

    private var subjectId: String
    private var lessonId: String
    init {
        this.subjectId = subjectId
        this.lessonId = lessonId
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveLesson: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(subjectId : String, lessonId: String)
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
        return inflater.inflate(R.layout.delete_lesson_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveLesson = view.findViewById<Button>(R.id.btnRemoveLesson)
        btnRemoveLesson.setOnClickListener {
            listener?.onButtonClicked(subjectId, lessonId)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}