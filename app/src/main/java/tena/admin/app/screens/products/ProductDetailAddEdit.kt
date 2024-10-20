package tena.admin.app.screens.products

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
import tena.admin.app.R
import tena.admin.app.models.Product
import tena.admin.app.utils.loadImageFromUrl
import tena.health.care.models.Student
import java.util.UUID

class ProductDetailAddEdit(product: String) : Fragment() {

    private var productId: String

    init {
        this.productId = product
    }

    private lateinit var etproductNameTitle: EditText
    private lateinit var etproductDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etProductStock: EditText
    private lateinit var etProductSize: EditText
    private lateinit var btnStore: Button
    private lateinit var progressBar: LottieAnimationView
    private lateinit var ivproductPic: ImageView
    private var imageUrl = ""

    private val PICK_IMAGE_REQUEST = 1

    private var type = ""
    private lateinit var ivBack: ImageView
    private lateinit var tvAddEditproductHeading: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        type = arguments?.getString("Type") ?: ""
        return inflater.inflate(R.layout.fragment_product_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById<LottieAnimationView>(R.id.progressBar)
        etproductNameTitle = view.findViewById(R.id.etproductNameTitle)
        etproductDescription = view.findViewById(R.id.etProductDescription)
        etPrice = view.findViewById(R.id.etProductPrice)
        etProductSize = view.findViewById(R.id.etProductSize)
        etProductStock = view.findViewById(R.id.etProductStock)
        btnStore = view.findViewById(R.id.btnSave)
        btnStore.setOnClickListener {
            if (etproductNameTitle.text.toString() != "") {
                if (etproductDescription.text.toString() != "") {
                    if (etPrice.text.toString() != "") {

                        if (etProductSize.text.toString() != "") {
                            if (imageUrl != "") {
                                if (etProductStock.text.toString() != "") {
                                    progressBar.visibility = View.VISIBLE
                                    if (type == "Edit") {
                                        val updatedDetails = hashMapOf(
                                            "productTitle" to etproductNameTitle.text.toString(),
                                            "productDescription" to etproductDescription.text.toString(),
                                            "price" to etPrice.text.toString().toDouble(),
                                            "imageUrl" to imageUrl,
                                            "productStock" to etProductStock.text.toString().toInt(),
                                            "productSize" to etProductSize.text.toString(),
                                        )
                                        updateproductDetailsByName(productId, updatedDetails)
                                    } else {
                                        addproduct(
                                            Product(
                                                id = UUID.randomUUID().toString(),
                                                productTitle = etproductNameTitle.text.toString(),
                                                productDescription = etproductDescription.text.toString(),
                                                price = if (etPrice.text.toString() != "") etPrice.text.toString()
                                                    .toDouble() else 0.0,
                                                imageUrl = imageUrl,
                                                productStock = if (etProductStock.text.toString() != "") etProductStock.text.toString()
                                                    .toInt() else 0,
                                                productSize = etProductSize.text.toString()
                                            )
                                        )
                                    }
                                } else {
                                    Snackbar.make(
                                        requireView(),
                                        "Please Enter product Stock",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Snackbar.make(
                                    requireView(),
                                    "Please Select product Image",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Snackbar.make(
                                requireView(),
                                "Please Enter product Size",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Please Enter product Price",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please Enter product Description",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Enter product Name", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvAddEditproductHeading = view.findViewById(R.id.tvAddEditproductHeading)
        ivproductPic = view.findViewById(R.id.ivproductPic)
        ivproductPic.setOnClickListener {
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
            tvAddEditproductHeading.text = "Edit product"
            progressBar.visibility = View.VISIBLE
            getproductByName(productId) { product ->
                product?.let {
                    println("Getproduct - product found: $product")
                    etproductNameTitle.setText(product.productTitle)
                    etproductDescription.setText(product.productDescription)
                    etPrice.setText(product.price.toString())
                    loadImageFromUrl(requireContext(), product.imageUrl, ivproductPic)
                    imageUrl = product.imageUrl
                    etProductStock.setText(product.productStock.toString())
                    etProductSize.setText(product.productSize)
                    progressBar.visibility = View.GONE
                } ?: run {
                    println("Getproduct - product not found")
                }
            }
        } else {
            tvAddEditproductHeading.text = "Add product"
            etproductNameTitle.isEnabled = true
        }

    }

    fun addproduct(product: Product) {
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .document(product.id)
            .set(product)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Product Added Successfully", Snackbar.LENGTH_LONG)
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

    fun getproductByName(productName: String, onProductRetrieved: (Product?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .document(productName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    onProductRetrieved(product)
                } else {
                    onProductRetrieved(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting product: $e")
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
            storageReference.child("productImages/" + System.currentTimeMillis() + ".jpg")

        val uploadTask = fileReference.putFile(imageUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Toast.makeText(requireContext(), "Upload successful", Toast.LENGTH_SHORT).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                loadImageFromUrl(requireContext(), downloadUrl, ivproductPic)
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

    fun updateproductDetailsByName(productId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("product")
            .whereEqualTo("id", productId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        db.collection("product")
                            .document(productId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                Snackbar.make(
                                    requireView(),
                                    "product Edited Successfully",
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