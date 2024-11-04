package com.edu.admin.screens.videos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.admin.R
import com.health.care.models.RecordedVideo

class ShowAllRecordedVideos : Fragment(), VideosDeleteBottomSheet.OnButtonClickListener,
        VideosApproveBottomSheet.OnButtonClickListener{

    private lateinit var rvRecordedVideos: RecyclerView
    private lateinit var tvEmptyRecordedList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment

    private lateinit var progressBar: LottieAnimationView
    private lateinit var ivBack: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recorded_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rvRecordedVideos = view.findViewById(R.id.rvRecordedVideos)
        tvEmptyRecordedList = view.findViewById(R.id.tvEmptyRecordedList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text has been changed
                s?.let {
                    val words = it.split(" ")
                    val lastWord = if (words.isNotEmpty()) words.last() else ""
                    Log.e("Test","lastWord - $lastWord")
                    searchVideos(lastWord) { searchVideos ->
                        val confirmedVideos = searchVideos.toMutableList()
                        confirmedVideos.removeIf { it.status == "Deleted" }
                        searchVideos.forEach {
                            Log.e("videos","video - $it")
                        }
                        if(searchVideos.size>0) {
                            tvEmptyRecordedList.visibility = View.GONE
                            rvRecordedVideos.visibility = View.VISIBLE
                            rvRecordedVideos.layoutManager = LinearLayoutManager(context)
                            rvRecordedVideos.adapter = ShowAllRecordedListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, confirmedVideos)
                        } else {
                            tvEmptyRecordedList.visibility = View.VISIBLE
                            rvRecordedVideos.visibility = View.GONE
                        }
                    }
                }
            }
        })

        tvEmptyRecordedList.visibility = View.VISIBLE

        listAllVideos()

    }

    override fun onResume() {
        super.onResume()
        Log.e("Test", "onResume Called")
    }

    override fun OnRemoveClickListener(videoName:String, updatedDetails: Map<String, Any>) {
        updateVideoByName(videoName, updatedDetails,"Video successfully deleted!")
    }

    override fun onApproveVideo(videoName: String, approvedVideo: Map<String, Any>) {
        updateVideoByName(videoName, approvedVideo,"Video successfully Approved!")
    }

    fun listAllVideos() {

        getAllRecordedVideos { videos ->
            val confirmedVideos = videos.toMutableList()
            confirmedVideos.removeIf { it.status == "Deleted" }
            videos.forEach {
                Log.e("videos","video - $it")
            }
            if(confirmedVideos.size>0) {
                tvEmptyRecordedList.visibility = View.GONE
                rvRecordedVideos.visibility = View.VISIBLE
                rvRecordedVideos.layoutManager = LinearLayoutManager(context)
                rvRecordedVideos.adapter =
                    ShowAllRecordedListAdapter(requireContext(), requireActivity(), parentFragmentManager, fragment, confirmedVideos)
            } else {
                tvEmptyRecordedList.visibility = View.VISIBLE
                rvRecordedVideos.visibility = View.GONE
            }
        }

    }

    fun getAllRecordedVideos(onProductsRetrieved: (List<RecordedVideo>) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        db.collection("videos")
            .get()
            .addOnSuccessListener { result ->
                val videoList = mutableListOf<RecordedVideo>()
                for (document in result) {
                    val video = document.toObject(RecordedVideo::class.java)
                    videoList.add(video)
                }
                onProductsRetrieved(videoList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting products: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchVideos(searchWord: String, onResult: (List<RecordedVideo>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val videosRef = db.collection("videos")
        // Simple search based on exact match
        videosRef.whereGreaterThanOrEqualTo("videoName", searchWord)
            .whereLessThanOrEqualTo("videoName", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val videos = mutableListOf<RecordedVideo>()
                for (document in documents) {
                    val video = document.toObject(RecordedVideo::class.java)
                    videos.add(video)
                }
                onResult(videos)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                onResult(emptyList())
            }
    }

    fun updateVideoByName(videoName: String, updatedDetails: Map<String, Any>, msg:String) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        // Assume the collection is called "students"
        db.collection("videos")
            .whereEqualTo("videoName", videoName) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        // Update the document found with new details
                        db.collection("videos")
                            .document(videoName)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
                                listAllVideos()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                progressBar.visibility = View.GONE
                                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
    }

}