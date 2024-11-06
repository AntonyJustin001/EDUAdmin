package com.edu.admin.screens.subjects

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.edu.admin.R
import com.edu.admin.models.Subject
import com.edu.admin.utils.loadImageFromUrl
import java.util.UUID

class SubjectDetailAddEdit(Subject: String) : Fragment() {

    private var SubjectId: String

    init {
        this.SubjectId = Subject
    }

    private lateinit var etSubjectNameTitle: EditText
    private lateinit var etSubjectDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etSubjectStock: EditText
    private lateinit var etSubjectSize: EditText
    private lateinit var btnStore: Button
    private lateinit var progressBar: LottieAnimationView
    private lateinit var ivSubjectPic: ImageView
    private var imageUrl = ""

    private val PICK_IMAGE_REQUEST = 1

    private var type = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvAddEditSubjectHeading: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type") ?: ""
        return inflater.inflate(R.layout.fragment_subject_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etSubjectNameTitle = view.findViewById(R.id.etSubjectNameTitle)
        etSubjectDescription = view.findViewById(R.id.etSubjectDescription)
        etPrice = view.findViewById(R.id.etSubjectPrice)
        etSubjectSize = view.findViewById(R.id.etSubjectSize)
        etSubjectStock = view.findViewById(R.id.etSubjectStock)
        btnStore = view.findViewById(R.id.btnSave)
        btnStore.setOnClickListener {
            if (etSubjectNameTitle.text.toString() != "") {
                if (etSubjectDescription.text.toString() != "") {
                    if (imageUrl != "") {
                        progressBar.visibility = View.VISIBLE
                        if (type == "Edit") {
                            val updatedDetails = hashMapOf(
                                "subjectTitle" to etSubjectNameTitle.text.toString(),
                                "subjectDescription" to etSubjectDescription.text.toString(),
                                "imageUrl" to imageUrl,
                            )
                            updateSubjectDetailsByName(SubjectId, updatedDetails)
                        } else {
                            addSubject(
                                Subject(
                                    id = UUID.randomUUID().toString(),
                                    subjectTitle = etSubjectNameTitle.text.toString(),
                                    subjectDescription = etSubjectDescription.text.toString(),
                                    imageUrl = imageUrl
                                )
                            )
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Please Pick Subject Image",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter Subject Description",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter Subject Name", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvAddEditSubjectHeading = view.findViewById(R.id.tvAddEditSubjectHeading)
        ivSubjectPic = view.findViewById(R.id.ivSubjectPic)
        ivSubjectPic.setOnClickListener {
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

        if (type == "Edit") {
            tvAddEditSubjectHeading.text = "Edit Subject"
            progressBar.visibility = View.VISIBLE
            getSubjectByName(SubjectId) { Subject ->
                Subject?.let {
                    println("GetSubject - Subject found: $Subject")
                    etSubjectNameTitle.setText(Subject.subjectTitle)
                    etSubjectDescription.setText(Subject.subjectDescription)
                    //etPrice.setText(Subject.price.toString())
                    loadImageFromUrl(requireContext(), Subject.imageUrl, ivSubjectPic)
                    imageUrl = Subject.imageUrl
                    //etSubjectStock.setText(Subject.SubjectStock.toString())
                    //etSubjectSize.setText(Subject.SubjectSize)
                    progressBar.visibility = View.GONE
                } ?: run {
                    println("GetSubject - Subject not found")
                }
            }
        } else {
            tvAddEditSubjectHeading.text = "Add Subject"
            etSubjectNameTitle.isEnabled = true
        }

    }

    fun addSubject(Subject: Subject) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects")
            .document(Subject.id)
            .set(Subject)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Subject Added Successfully", Snackbar.LENGTH_LONG)
                    .show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    requireView(),
                    "Something went wrong try again later",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }

    fun getSubjectByName(SubjectName: String, onSubjectRetrieved: (Subject?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("subjects")
            .document(SubjectName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val Subject = document.toObject(Subject::class.java)
                    onSubjectRetrieved(Subject)
                } else {
                    onSubjectRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting Subject: $e")
            }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        progressBar.visibility = View.VISIBLE
        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference =
            storageReference.child("subjectImages/" + System.currentTimeMillis() + ".jpg")

        val uploadTask = fileReference.putFile(imageUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                loadImageFromUrl(requireContext(), downloadUrl, ivSubjectPic)
                imageUrl = downloadUrl
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

    fun updateSubjectDetailsByName(SubjectId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("subjects")
            .whereEqualTo("id", SubjectId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        db.collection("subjects")
                            .document(SubjectId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                Snackbar.make(
                                    requireView(),
                                    "Subject Edited Successfully",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                Snackbar.make(
                                    requireView(),
                                    "Something went wrong please try again",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    Snackbar.make(
                        requireView(),
                        "Something went wrong please try again",
                        Snackbar.LENGTH_LONG
                    ).show()
                    progressBar.visibility = View.GONE
                    parentFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                Snackbar.make(
                    requireView(),
                    "Something went wrong please try again",
                    Snackbar.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}