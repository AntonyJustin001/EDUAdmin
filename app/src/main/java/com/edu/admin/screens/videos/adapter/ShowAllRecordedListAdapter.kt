package com.edu.admin.screens.videos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.edu.admin.R
import com.edu.admin.screens.videos.videoPlayer.VideoPlayerActivity
import com.health.care.models.RecordedVideo

class ShowAllRecordedListAdapter(
    val context: Context,
    val activity: Activity,
    val parentFragmentManager: FragmentManager,
    val fragment: Fragment,
    private val items: List<RecordedVideo>
) :
    RecyclerView.Adapter<ShowAllRecordedListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_recorded_videos, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val tvVideoName: TextView = itemView.findViewById(R.id.tvVideoName)
        private val tvSection: TextView = itemView.findViewById(R.id.tvSection)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ivConfirm: ImageView = itemView.findViewById(R.id.ivConfirm)
        private val ivRemove: ImageView = itemView.findViewById(R.id.ivRemove)
        private val confirmRemoveHolder: LinearLayout = itemView.findViewById(R.id.confirmRemoveHolder)
        private val videoHolder: ConstraintLayout = itemView.findViewById(R.id.videoHolder)
        private val tvApproveStatus: TextView = itemView.findViewById(R.id.tvApproveStatus)

        fun bind(recordedVideo: RecordedVideo) {

            tvVideoName.text = recordedVideo.videoName
            tvSection.text = recordedVideo.section
            tvDate.text = recordedVideo.subject

            if(recordedVideo.status == "Approved") {
                tvApproveStatus.visibility = View.VISIBLE
                confirmRemoveHolder.visibility = View.GONE
            } else {
                tvApproveStatus.visibility = View.GONE
                confirmRemoveHolder.visibility = View.VISIBLE
            }

            videoHolder.setOnClickListener {
                val intent = Intent(activity, VideoPlayerActivity::class.java)
                intent.putExtra("VideoUrl", recordedVideo.videoUrl)
                context.startActivity(intent)
            }
            ivConfirm.setOnClickListener {
                approvedVideoDialog(recordedVideo)
            }
            ivRemove.setOnClickListener {
                removeVideoDialog(recordedVideo)
            }

        }

        fun removeVideoDialog(recordedVideo: RecordedVideo) {
            val bottomSheetFragment = VideosDeleteBottomSheet(recordedVideo)
            bottomSheetFragment.setTargetFragment(fragment, 0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        fun approvedVideoDialog(recordedVideo: RecordedVideo) {
            val bottomSheetFragment = VideosApproveBottomSheet(recordedVideo)
            bottomSheetFragment.setTargetFragment(fragment, 0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }

}