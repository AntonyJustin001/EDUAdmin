package tena.admin.app.screens.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import tena.admin.app.R
import tena.admin.app.utils.loadImageFromUrl
import tena.admin.app.models.Product

class ProductDetailsScreen(product :String) : Fragment() {

    private var productId :String
    init {
        this.productId = product
    }

    private lateinit var progressBar: LottieAnimationView
    private lateinit var tvproductTitle: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductDescription: TextView
    private lateinit var profileImage: ImageView
    private lateinit var tvProductStock: TextView
    private lateinit var tvProductSize: TextView
    private var imageUrl = ""

    private lateinit var ivBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        tvproductTitle = view.findViewById(R.id.tvproductTitle)
        tvProductPrice = view.findViewById(R.id.tvPrice)
        tvProductDescription = view.findViewById(R.id.tvProductDescription)
        profileImage = view.findViewById(R.id.profileImage)
        tvProductStock = view.findViewById(R.id.tvProductStock)
        tvProductSize = view.findViewById(R.id.tvProductSize)

        progressBar.visibility = View.VISIBLE
        getproductById(productId) { product ->
            product?.let {
                println("Getproduct - product found: $product")
                tvproductTitle.setText(product.productTitle)
                tvProductPrice.setText(product.price.toString())
                tvProductDescription.setText(product.productDescription)
                loadImageFromUrl(requireContext(),product.imageUrl,profileImage)
                imageUrl = product.imageUrl
                tvProductStock.setText(product.productStock.toString())
                tvProductSize.setText(product.productSize)
                progressBar.visibility = View.GONE
            } ?: run {
                println("Getproduct - product not found")
                Snackbar.make(requireView(), "Something went wrong try again later", Snackbar.LENGTH_LONG).show()
            }
        }

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    fun getproductById(productName: String, onProductRetrieved: (Product?) -> Unit) {
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
                Snackbar.make(requireView(), "Something went wrong try again later", Snackbar.LENGTH_LONG).show()
            }
    }

}