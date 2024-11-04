package com.edu.admin.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.edu.admin.R

class TestScreen : Fragment() {

    private lateinit var btnStore: Button
    private lateinit var btnStoreSlider: Button
    private lateinit var btnGetAll: Button
    private lateinit var btnGet: Button
    private var count: Int = 0
    private var sliderCount: Int = 0

//    val productListener = listenToProducts { products ->
//        // Update your UI with the new product list
//        items.value?.clear()
//        for (product in products) {
//            println(" Product Listener - Product: ${product.productTitle}, Price: ${product.price}")
//            addItem(product)
//        }
//    }
//    private val _items = MutableLiveData<MutableSet<Product>>().apply { value = mutableSetOf() }
//    val items: LiveData<MutableSet<Product>> = _items


//    val sliderListener = listenToProducts { sliders ->
//        // Update your UI with the new product list
//        items.value?.clear()
//        for (product in sliders) {
//            println(" Slider Listener - Product: ${product.productTitle}, Price: ${product.price}")
//            addItem(product)
//        }
//    }
//    private val _sliderItems = MutableLiveData<MutableSet<HomeSlider>>().apply { value = mutableSetOf() }
//    val sliderItems: LiveData<MutableSet<HomeSlider>> = _sliderItems




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Observe changes to the list
//        items.observe(viewLifecycleOwner, Observer { list ->
//            Log.e("Test","List Changed - $list")
//            // Update UI with the new list
//        })

        btnStore = view.findViewById(R.id.btnStore)
        btnStore.setOnClickListener {
//            count++
//            storeProduct(Product(
//                id = "$count",
//                productTitle = "Product$count",
//                price = 1000.0,
//                productDescription = "This is the product description",
//                imageUrl = "www.imageUrl.com"
//            ))
        }

        btnStoreSlider = view.findViewById(R.id.btnStoreSlider)
        btnStoreSlider.setOnClickListener {
            sliderCount++
//            storeHomeSlider(HomeSlider(
//                id = "$sliderCount",
//                imageUrl = "www.imageUrl.com"
//            ))
        }

        btnGetAll = view.findViewById(R.id.btnGetAll)
        btnGetAll.setOnClickListener {
//            getAllProducts { products ->
//                for (product in products) {
//                    println("GetAll Products - Product: ${product.name}, Price: ${product.price}")
//                }
//            }
        }
        btnGet = view.findViewById(R.id.btnGet)
        btnGet.setOnClickListener {
//            searchProducts("Product1") { products ->
//                products.forEach {
//                    Log.d("Product", it.productTitle)
//                }
//            }

//            getProductById("10") { product ->
//                product?.let {
//                    println("GetProductById - Product found: ${it.name}, Price: ${it.price}")
//                } ?: run {
//                    println("GetProductById - Product not found")
//                }
//            }
        }

    }

    fun sendNotificationToUser(userToken: String, title: String, message: String) {
        val notificationData = mapOf(
            "to" to userToken,
            "notification" to mapOf(
                "title" to title,
                "body" to message
            )
        )
        // Send HTTP request to FCM API using OkHttp or another HTTP client
    }

//    fun SendNotification() {
//        val fcmToken = "USER_FCM_TOKEN"  // Get the user's token from Firestore
//        val url = "https://fcm.googleapis.com/fcm/send"
//
//        val json = """{
//        "to": "$fcmToken",
//        "notification": {
//            "title": "New Notification",
//            "body": "Hello, this is a message from the Admin!"
//        },
//        "data": {
//            "extraData": "This is some extra data"
//        }
//    }"""
//
//        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
//
//        val request = Request.Builder()
//            .url(url)
//            .addHeader("Authorization", "key=YOUR_SERVER_KEY")  // Replace with your Firebase server key
//            .post(requestBody)
//            .build()
//
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle failure
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Handle success
//            }
//        })
//    }

//    fun searchProducts(searchWord: String, onResult: (List<Product>) -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//        val productsRef = db.collection("products")
//
//        // Simple search based on exact match
//        productsRef.whereGreaterThanOrEqualTo("productTitle", searchWord)
//            .whereLessThanOrEqualTo("productTitle", searchWord + '\uf8ff')
//            .get()
//            .addOnSuccessListener { documents ->
//                val products = mutableListOf<Product>()
//                for (document in documents) {
//                    val product = document.toObject(Product::class.java)
//                    products.add(product)
//                }
//                onResult(products)
//            }
//            .addOnFailureListener { exception ->
//                // Handle the error
//                Log.e("Firestore", "Error getting documents: ", exception)
//                onResult(emptyList())
//            }
//    }

//    private fun addItem(item: Product) {
//        _items.value?.apply {
//            add(item)
//            _items.value = this
//        }
//    }

//    private fun removeItem(item: Product) {
//        _items.value?.apply {
//            remove(item)
//            _items.value = this
//        }
//    }

//    fun storeProduct(product: Product) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("products")
//            .document(product.id)
//            .set(product)
//            .addOnSuccessListener {
//                // Product successfully written!
//                println("Product added successfully")
//            }
//            .addOnFailureListener { e ->
//                // Handle the error
//                println("Error adding product: $e")
//            }
//    }

//    fun storeHomeSlider(homeSlider: HomeSlider) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("homeSlider")
//            .document(homeSlider.id)
//            .set(homeSlider)
//            .addOnSuccessListener {
//                // Product successfully written!
//                println("HomeSlider added successfully")
//            }
//            .addOnFailureListener { e ->
//                // Handle the error
//                println("Error adding homeSlider: $e")
//            }
//    }

//    fun listenToHomeSliders(onSlidersChanged: (List<HomeSlider>) -> Unit): ListenerRegistration {
//        val db = FirebaseFirestore.getInstance()
//        return db.collection("homeSliders")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    // Handle the error
//                    println("Listen failed: $e")
//                    return@addSnapshotListener
//                }
//
//                if (snapshots != null && !snapshots.isEmpty) {
//                    val sliderList = mutableListOf<HomeSlider>()
//                    for (document in snapshots.documents) {
//                        val homeSlider = document.toObject(HomeSlider::class.java)
//                        homeSlider?.let { sliderList.add(it) }
//                    }
//                    onSlidersChanged(sliderList)
//                } else {
//                    println("No products found")
//                }
//            }
//    }

//    fun listenToProducts(onProductsChanged: (List<Product>) -> Unit): ListenerRegistration {
//        val db = FirebaseFirestore.getInstance()
//        return db.collection("products")
//            .addSnapshotListener { snapshots, e ->
//                if (e != null) {
//                    // Handle the error
//                    println("Listen failed: $e")
//                    return@addSnapshotListener
//                }
//
//                if (snapshots != null && !snapshots.isEmpty) {
//                    val productList = mutableListOf<Product>()
//                    for (document in snapshots.documents) {
//                        val product = document.toObject(Product::class.java)
//                        product?.let { productList.add(it) }
//                    }
//                    onProductsChanged(productList)
//                } else {
//                    println("No products found")
//                }
//            }
//    }


//    fun getAllProducts(onProductsRetrieved: (List<Product>) -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("products")
//            .get()
//            .addOnSuccessListener { result ->
//                val productList = mutableListOf<Product>()
//                for (document in result) {
//                    val product = document.toObject(Product::class.java)
//                    productList.add(product)
//                }
//                onProductsRetrieved(productList)
//            }
//            .addOnFailureListener { e ->
//                // Handle the error
//                println("Error getting products: $e")
//            }
//    }

//    fun getProductById(productId: String, onProductRetrieved: (Product?) -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("products")
//            .document(productId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val product = document.toObject(Product::class.java)
//                    onProductRetrieved(product)
//                } else {
//                    onProductRetrieved(null)
//                }
//            }
//            .addOnFailureListener { e ->
//                // Handle the error
//                println("Error getting product: $e")
//            }
//    }

//    fun listenToProduct(productId: String, onProductChanged: (Product?) -> Unit): ListenerRegistration {
//        val db = FirebaseFirestore.getInstance()
//        return db.collection("products")
//            .document(productId)
//            .addSnapshotListener { documentSnapshot, e ->
//                if (e != null) {
//                    // Handle the error
//                    println("Listen failed: $e")
//                    return@addSnapshotListener
//                }
//
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    val product = documentSnapshot.toObject(Product::class.java)
//                    onProductChanged(product)
//                } else {
//                    onProductChanged(null)
//                    println("Product not found")
//                }
//            }
//    }


    override fun onDestroy() {
        super.onDestroy()
        //productListener.remove() // Detach the listener when the activity is destroyed
    }


}