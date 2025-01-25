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
import com.edu.admin.models.Theories
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class TheoriesListScreen(subject: Subject, lesson: Lesson) : Fragment(),
    TheoriesDeleteBottomSheet.OnButtonClickListener {

    private var subject: Subject
    private var lesson: Lesson

    init {
        this.subject = subject
        this.lesson = lesson
    }

    private lateinit var rcTheories: RecyclerView
    private lateinit var tvEmptyTheoriesList: TextView
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddTheories: ImageView
    private lateinit var progressBar: LottieAnimationView

    private val PICK_IMAGE_REQUEST = 1
    private var docsUrl = ""
    private var docName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_theories_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        rcTheories = view.findViewById(R.id.rvTheories)
        tvEmptyTheoriesList = view.findViewById(R.id.tvEmptyTheoriesList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddTheories = view.findViewById(R.id.ivAddTheories)
        ivAddTheories.setOnClickListener {
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

        loadTheoriesList(subject, lesson)
    }

    override fun onResume() {
        super.onResume()
        Log.e("Test", "onResume Called")
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // Accept all file types initially
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf", // PDF
                "application/msword", // DOC
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // DOCX
            ))
            addCategory(Intent.CATEGORY_OPENABLE) // Ensure only openable files are shown
        }
        startActivityForResult(Intent.createChooser(intent, "Select a document"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val docUri: Uri? = data.data
            if (docUri != null) {
                uploadDocumentsToFirebase(docUri)
            }
        }
    }

    private fun uploadDocumentsToFirebase(docsUri: Uri) {
        progressBar.visibility = View.VISIBLE
        docName = getReadableFileName(docsUri)?:"DocumentFile"
        // Split the file name into name and extension
        val dotIndex = docName.lastIndexOf(".")
        docName = if (dotIndex != -1) {
            val name = docName.substring(0, dotIndex) // Name before the dot
            val extension = docName.substring(dotIndex) // Extension including the dot
            "$name${System.currentTimeMillis()}$extension" // Add timestamp between name and extension
        } else {
            "$docName${System.currentTimeMillis()}" // If no extension, just append the timestamp
        }

        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference =
            storageReference.child("Theories/" + docName)

        val uploadTask = fileReference.putFile(docsUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                docsUrl = downloadUrl
                addTheory(
                    Theories(
                        theoriesId = UUID.randomUUID().toString(),
                        theoriesTitle = docName,
                        theoriesUrl = docsUrl)
                )
                Log.d("Firebase Storage", "Image URL: $downloadUrl")
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

    fun getAllTheories(subject: Subject, lesson: Lesson, onTheoriesRetrieved: (List<Theories>) -> Unit) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val TheoriesRef = db
                .collection("subjects").document(subject.id)
                .collection("lessons").document(lesson.id)
                .collection("theories")
            TheoriesRef
                .get()
                .addOnSuccessListener { result ->
                    val TheoriesList = mutableListOf<Theories>()
                    for (document in result) {
                        val Theories = document.toObject(Theories::class.java)
                        TheoriesList.add(Theories)
                    }
                    onTheoriesRetrieved(TheoriesList)
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    // Handle the error
                    println("Error getting Theories: $e")
                    progressBar.visibility = View.GONE
                }
        } catch (e:Exception) {
            progressBar.visibility = View.GONE
            tvEmptyTheoriesList.visibility = View.VISIBLE
            rcTheories.visibility = View.GONE
        }
    }

    fun deleteTheories(subject: Subject,lessons: Lesson, theoriesId: String) {
        try {
            progressBar.visibility = View.VISIBLE
            val db = FirebaseFirestore.getInstance()
            val TheoriesRef = db.collection("subjects").document(subject.id)
                .collection("lessons").document(lessons.id)
                .collection("theories")
            TheoriesRef
                .whereEqualTo("theoriesId", theoriesId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // For each matching document, delete it
                        TheoriesRef
                            .document(theoriesId)
                            .delete()
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                Snackbar.make(
                                    requireView(),
                                    "Theories successfully deleted!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                loadTheoriesList(subject, lessons)
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
            tvEmptyTheoriesList.visibility = View.VISIBLE
            rcTheories.visibility = View.GONE
        }
    }

    fun loadTheoriesList(subject: Subject, lesson: Lesson) {
        Log.e("Test", "LoadTheoriesList Called()")
        getAllTheories(subject, lesson) { theoriesList ->
            theoriesList.forEach {
                Log.e("Theories", "Theories - $it")
            }
            if (theoriesList.size > 0) {
                tvEmptyTheoriesList.visibility = View.GONE
                rcTheories.visibility = View.VISIBLE
                rcTheories.layoutManager = LinearLayoutManager(context)
                rcTheories.adapter = TheoriesListAdapter(
                    requireContext(),
                    requireActivity(),
                    parentFragmentManager,
                    fragment,
                    subject,
                    lesson,
                    theoriesList
                )
            } else {
                tvEmptyTheoriesList.visibility = View.VISIBLE
                rcTheories.visibility = View.GONE
            }
        }
    }

    override fun onButtonClicked(subject: Subject, lesson: Lesson, theoriesId: String) {
        deleteTheories(subject, lesson, theoriesId)
    }

    fun addTheory(theory: Theories) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects").document(subject.id)
            .collection("lessons").document(lesson.id)
            .collection("theories")
            .document(theory.theoriesId)
            .set(theory)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Theory Added Successfully", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                loadTheoriesList(subject, lesson)
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

class TheoriesListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val subject: Subject, private val lesson : Lesson,
                          private val theories: List<Theories>) :
    RecyclerView.Adapter<TheoriesListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_theories, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(subject, lesson, theories[position])
    }

    override fun getItemCount(): Int = theories.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private val tvTheoriesName: TextView = itemView.findViewById(R.id.tvTheoriesName)
        private val layoutDelete: LinearLayout = itemView.findViewById(R.id.layoutDelete)
        private val theoriesHolder: MaterialCardView = itemView.findViewById(R.id.TheoriesHolder)

        fun bind(subject: Subject, lesson: Lesson, theory: Theories) {

            tvTheoriesName.text = theory.theoriesTitle
            layoutDelete.setOnClickListener {
                deleteTheoriesDialog(subject,lesson, theory.theoriesId)
            }
            theoriesHolder.setOnClickListener {

            }
        }

        fun deleteTheoriesDialog(subject: Subject, lesson: Lesson, theoriesId: String) {
            val bottomSheetFragment = TheoriesDeleteBottomSheet(subject, lesson, theoriesId)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}

class TheoriesDeleteBottomSheet(subject : Subject, lesson: Lesson, theoriesId: String): BottomSheetDialogFragment() {

    private var subject: Subject
    private var lesson: Lesson
    private var theoriesId: String
    init {
        this.subject = subject
        this.lesson = lesson
        this.theoriesId = theoriesId
    }

    private lateinit var btnNo: Button
    private lateinit var btnRemoveTheories: Button

    // Define the interface
    interface OnButtonClickListener {
        fun onButtonClicked(subject : Subject, lesson: Lesson, theoriesId: String)
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
        return inflater.inflate(R.layout.delete_theory_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up views and listeners here
        btnNo = view.findViewById<Button>(R.id.btnNo)
        btnNo.setOnClickListener {
            // Handle button click
            dismiss() // Dismiss the BottomSheet
        }

        btnRemoveTheories = view.findViewById<Button>(R.id.btnRemoveTheory)
        btnRemoveTheories.setOnClickListener {
            listener?.onButtonClicked(subject, lesson, theoriesId)
            dismiss() // Dismiss the BottomSheet
        }
    }

    // Clean up listener to avoid memory leaks
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}