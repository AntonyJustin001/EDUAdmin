package com.edu.admin.screens.videos

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.edu.admin.R
import com.health.care.models.RecordedVideo

class VideosApproveBottomSheet(recordedVideo :RecordedVideo): BottomSheetDialogFragment() {

    private var recordedVideo: RecordedVideo
    init {
        this.recordedVideo = recordedVideo
    }

    private lateinit var btnNo: Button
    private lateinit var btnApproveVideo: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onApproveVideo(videoName:String, deleteVideo: Map<String, Any>)
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
        return inflater.inflate(R.layout.approve_recorded_videos_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnApproveVideo = view.findViewById<Button>(R.id.btnApproveVideo)
        btnApproveVideo.setOnClickListener {
            val approvedDetails = hashMapOf(
                "date" to recordedVideo.date,
                "section" to recordedVideo.section,
                "status" to "Approved",
                "subject" to recordedVideo.subject,
                "teacherName" to recordedVideo.teacherName,
                "videoName" to recordedVideo.videoName,
                "videoUrl" to recordedVideo.videoUrl,
            )
            listener?.onApproveVideo(recordedVideo.videoName, approvedDetails)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}