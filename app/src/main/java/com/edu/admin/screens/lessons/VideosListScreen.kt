package com.edu.admin.screens.lessons

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.edu.admin.models.Subject
import com.edu.admin.models.Videos
import com.edu.admin.screens.videos.videoPlayer.VideoPlayerActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class VideosListScreen(subject: Subject, lesson: Lesson) : Fragment(),
    VideosDeleteBottomSheet.OnButtonClickListener {

    private var subject: Subject
    private var lesson: Lesson

    init {
        this.subject = subject
        this.lesson = lesson
    }

    private lateinit var rcVideos: RecyclerView
    private lateinit var tvEmptyVideosList: TextView
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddVideos: ImageView
    private lateinit var progressBar: LottieAnimationView

    private val PICK_IMAGE_REQUEST = 1
    private var videosUrl = ""
    private var videoName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_videos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rcVideos = view.findViewById(R.id.rvVideos)
        tvEmptyVideosList = view.findViewById(R.id.tvEmptyVideosList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddVideos = view.findViewById(R.id.ivAddVideos)
        ivAddVideos.setOnClickListener {
            openFileChooser()
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

        loadVideosList(subject, lesson)
    }

    override fun onResume() {
        super.onResume()
        Log.e("Test", "onResume Called")
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*" // Set MIME type to video
            addCategory(Intent.CATEGORY_OPENABLE) // Ensure only openable files are shown
        }
        startActivityForResult(Intent.createChooser(intent, "Select a Video"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val videoUri: Uri? = data.data
            if (videoUri != null) {
                uploadVideosToFirebase(videoUri)
            }
        }
    }

    private fun uploadVideosToFirebase(docsUri: Uri) {
        progressBar.visibility = View.VISIBLE

        videoName = getReadableFileName(docsUri)?:"DocumentFile"
        // Split the file name into name and extension
        val dotIndex = videoName.lastIndexOf(".")
        videoName = if (dotIndex != -1) {
            val name = videoName.substring(0, dotIndex) // Name before the dot
            val extension = videoName.substring(dotIndex) // Extension including the dot
            "$name${System.currentTimeMillis()}$extension" // Add timestamp between name and extension
        } else {
            "$videoName${System.currentTimeMillis()}" // If no extension, just append the timestamp
        }

        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference =
            storageReference.child("Videos/" + System.currentTimeMillis() + ".mp4")

        val uploadTask = fileReference.putFile(docsUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                videosUrl = downloadUrl
                addVideo(
                    Videos(
                        videosId = UUID.randomUUID().toString(),
                        videosTitle = videoName,
                        videosUrl = videosUrl)
                )
                Log.d("Firebase Storage", "Video URL: $downloadUrl")
                progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            // Handle failed upload
            Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }.addOnProgressListener { taskSnapshot ->
            // You can display the progress of the upload here
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            Log.d("Firebase Storage", "Upload is $progress% done")
        }
    }

    fun getReadableFileName(uri: Uri): String? {
        val decodedPath = URLDecoder.decode(uri.lastPathSegment ?: "", StandardCharsets.UTF_8.name())
        return decodedPath.substringAfterLast("/") // Extract only the file name
    }

    fun getAllVideos(subject: Subject, lesson: Lesson, onVideosRetrieved: (List<Videos>) -> Unit) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val VideosRef = db
                .collection("subjects").document(subject.id)
                .collection("lessons").document(lesson.id)
                .collection("videos")
            VideosRef
                .get()
                .addOnSuccessListener { result ->
                    val VideosList = mutableListOf<Videos>()
                    for (document in result) {
                        val Videos = document.toObject(Videos::class.java)
                        VideosList.add(Videos)
                    }
                    onVideosRetrieved(VideosList)
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error getting Videos: $e")
                    progressBar.visibility = View.GONE
                }
        } catch (e:Exception) {
            progressBar.visibility = View.GONE
            tvEmptyVideosList.visibility = View.VISIBLE
            rcVideos.visibility = View.GONE
        }
    }

    fun deleteVideos(subject: Subject,lessons: Lesson, VideosId: String) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val VideosRef = db.collection("subjects").document(subject.id)
                .collection("lessons").document(lessons.id)
                .collection("videos")
            VideosRef
                .whereEqualTo("id", VideosId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // For each matching document, delete it
                        VideosRef
                            .document(VideosId)
                            .delete()
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "Videos successfully deleted!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                loadVideosList(subject, lessons)
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
            tvEmptyVideosList.visibility = View.VISIBLE
            rcVideos.visibility = View.GONE
        }
    }

    fun loadVideosList(subject: Subject, lesson: Lesson) {
        Log.e("Test", "LoadVideosList Called()")
        getAllVideos(subject, lesson) { VideosList ->
            VideosList.forEach {
                Log.e("Videos", "Videos - $it")
            }
            if (VideosList.size > 0) {
                tvEmptyVideosList.visibility = View.GONE
                rcVideos.visibility = View.VISIBLE
                rcVideos.layoutManager = LinearLayoutManager(context)
                rcVideos.adapter = VideosListAdapter(
                    requireContext(),
                    requireActivity(),
                    parentFragmentManager,
                    fragment,
                    subject,
                    lesson,
                    VideosList
                )
            } else {
                tvEmptyVideosList.visibility = View.VISIBLE
                rcVideos.visibility = View.GONE
            }
        }
    }

    override fun onButtonClicked(subject: Subject, lesson: Lesson, VideosId: String) {
        deleteVideos(subject, lesson, VideosId)
    }

    fun addVideo(Video: Videos) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects").document(subject.id)
            .collection("lessons").document(lesson.id)
            .collection("videos")
            .document(Video.videosId)
            .set(Video)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Video Added Successfully", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                loadVideosList(subject, lesson)
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    requireView(),
                    "Something went wrong try again later",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
            }
    }
}

class VideosListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val subject: Subject, private val lesson : Lesson,
                          private val Videos: List<Videos>) :
    RecyclerView.Adapter<VideosListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_videos, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(subject, lesson, Videos[position])
    }

    override fun getItemCount(): Int = Videos.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private val tvVideosName: TextView = itemView.findViewById(R.id.tvVideosName)
        private val layoutDelete: LinearLayout = itemView.findViewById(R.id.layoutDelete)
        private val VideosHolder: MaterialCardView = itemView.findViewById(R.id.VideosHolder)

        fun bind(subject: Subject, lesson: Lesson, video: Videos) {

            tvVideosName.text = video.videosTitle
            layoutDelete.setOnClickListener {
                deleteVideosDialog(subject,lesson, video.videosId)
            }
            VideosHolder.setOnClickListener {
                val intent = Intent(activity, VideoPlayerActivity::class.java)
                intent.putExtra("VideoUrl", video.videosUrl)
                context.startActivity(intent)
            }

        }

        fun deleteVideosDialog(subject: Subject, lesson: Lesson, VideosId: String) {
            val bottomSheetFragment = VideosDeleteBottomSheet(subject, lesson, VideosId)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}

class VideosDeleteBottomSheet(subject : Subject, lesson: Lesson, VideosId: String): BottomSheetDialogFragment() {

    private var subject: Subject
    private var lesson: Lesson
    private var VideosId: String
    init {
        this.subject = subject
        this.lesson = lesson
        this.VideosId = VideosId
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveVideos: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(subject : Subject, lesson: Lesson, VideosId: String)
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
        return inflater.inflate(R.layout.delete_video_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveVideos = view.findViewById<Button>(R.id.btnRemoveVideo)
        btnRemoveVideos.setOnClickListener {
            listener?.onButtonClicked(subject, lesson, VideosId)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}