package com.edu.admin.screens.teachers

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.edu.admin.R
import com.edu.admin.dialog.CustomDialog
import com.edu.admin.utils.loadImageFromUrl
import com.health.care.models.Teacher

class TeacherDetailAddEdit(student :String) : Fragment() {

    private var student :String
    init {
        this.student = student
    }

    private lateinit var etStudentNameTitle: EditText
    private lateinit var etStudentSection: EditText
    private lateinit var etMobile: EditText
    private lateinit var etGmailID: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnStore: Button
    private lateinit var progressBar: LottieAnimationView

    private lateinit var ivStudentPic: ImageView
    private var imageUrl = ""

    lateinit var customDialog: CustomDialog
    private val PICK_IMAGE_REQUEST = 1

    private var type = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvAddEditStudentHeading:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type")?:""
        return inflater.inflate(R.layout.fragment_teacher_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etStudentNameTitle = view.findViewById(R.id.etStudentNameTitle)
        etStudentSection = view.findViewById(R.id.etStudentSection)
        etMobile = view.findViewById(R.id.etMobile)
        etGmailID = view.findViewById(R.id.etGmailID)
        etAddress = view.findViewById(R.id.etAddress)
        btnStore = view.findViewById(R.id.btnSave)
        btnStore.setOnClickListener {
            Log.e(
                "Test", "Student Name - ${etStudentNameTitle.text.toString()}" +
                        "Section - ${etStudentSection.text.toString()} mobile - ${etMobile.text.toString()}" +
                        "emailId - ${etGmailID.text.toString()}" +
                        "address - ${etAddress.text.toString()}"
            )
            progressBar.visibility = View.VISIBLE
            if (type == "Edit") {
                val updatedDetails = hashMapOf(
                    "address" to etAddress.text.toString(),
                    "section" to etStudentSection.text.toString(),
                    "mobileNo" to etMobile.text.toString(),
                    "emailId" to etGmailID.text.toString(),
                    "profilePic" to imageUrl,
                )
                updateTeachersDetailsByName(etStudentNameTitle.text.toString(), updatedDetails)
            } else {
                if (!etStudentNameTitle.text.toString().equals("")) {
                    addTeacher(
                        Teacher(
                            name = etStudentNameTitle.text.toString(),
                            section = etStudentSection.text.toString(),
                            mobileNo = etMobile.text.toString(),
                            emailId = etGmailID.text.toString(),
                            address = etAddress.text.toString(),
                            profilePic = imageUrl
                        )
                    )
                } else {
                    progressBar.visibility = View.GONE
                    customDialog = CustomDialog(
                        resources.getDrawable(R.drawable.ic_wrong),
                        resources.getString(R.string.error), "Please Enter Student Name"
                    )
                    customDialog.show(childFragmentManager, "CustomDialog")
                }
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvAddEditStudentHeading = view.findViewById(R.id.tvAddEditStudentHeading)

        ivStudentPic = view.findViewById(R.id.ivStudentPic)
        ivStudentPic.setOnClickListener {
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
            tvAddEditStudentHeading.text = "Edit Teacher"
            etStudentNameTitle.isEnabled = false
            progressBar.visibility = View.VISIBLE
            getTeacherByName(student) { student ->
                student?.let {
                    println("GetStudent - Student found: $student")
                    etStudentNameTitle.setText(student.name)
                    etAddress.setText(student.address)
                    etMobile.setText(student.mobileNo)
                    etStudentSection.setText(student.section)
                    etGmailID.setText(student.emailId)
                    loadImageFromUrl(requireContext(),student.profilePic,ivStudentPic)
                    imageUrl = student.profilePic
                    progressBar.visibility = View.GONE
                } ?: run {
                    println("GetStudent - Student not found")
                }
            }
        } else {
            tvAddEditStudentHeading.text = "Add Teacher"
            etStudentNameTitle.isEnabled = true
        }

    }

    fun addTeacher(teacher: Teacher) {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(teacher.name)
            .set(teacher)
            .addOnSuccessListener {
                customDialog = CustomDialog(
                    resources.getDrawable(R.drawable.setting_icon),
                    "Success", "teacher Added Successfully"
                ) {
                    parentFragmentManager.popBackStack()
                }
                customDialog.show(childFragmentManager, "CustomDialog")
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error adding teacher: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun getTeacherByName(studentName: String, onProductRetrieved: (Teacher?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("teacher")
            .document(studentName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val teacher = document.toObject(Teacher::class.java)
                    onProductRetrieved(teacher)
                } else {
                    onProductRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting student: $e")
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
        val fileReference = storageReference.child("teachers/" + System.currentTimeMillis() + ".jpg")

        val uploadTask = fileReference.putFile(imageUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                loadImageFromUrl(requireContext(), downloadUrl, ivStudentPic)
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

    fun updateTeachersDetailsByName(newName: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        // Assume the collection is called "students"
        db.collection("teacher")
            .whereEqualTo("name", newName) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        // Update the document found with new details
                        db.collection("teacher")
                            .document(newName)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                customDialog = CustomDialog(
                                    resources.getDrawable(R.drawable.setting_icon),
                                    "Success", "Teacher Edited Successfully"
                                ) {
                                    parentFragmentManager.popBackStack()
                                }
                                customDialog.show(childFragmentManager, "CustomDialog")
                                progressBar.visibility = View.GONE
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error updating document", e)
                                customDialog = CustomDialog(
                                    resources.getDrawable(R.drawable.ic_wrong),
                                    resources.getString(R.string.error), "Something went wrong please try again"
                                )
                                customDialog.show(childFragmentManager, "CustomDialog")
                                progressBar.visibility = View.GONE
                            }
                    }
                } else {
                    Log.d("Firestore", "No such document found!")
                    customDialog = CustomDialog(
                        resources.getDrawable(R.drawable.ic_wrong),
                        resources.getString(R.string.error), "No such document found!"
                    )
                    customDialog.show(childFragmentManager, "CustomDialog")
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                customDialog = CustomDialog(
                    resources.getDrawable(R.drawable.ic_wrong),
                    resources.getString(R.string.error), "Something went wrong please try again"
                )
                customDialog.show(childFragmentManager, "CustomDialog")
                progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}