package com.edu.admin.models

data class Product(
    val id: String = "",
    val productTitle: String = "",
    val productDescription: String = "",
    val price: Double = 0.0,
    val currency: String = "INR",
    val imageUrl: String = "",
    val productStock: Int = 0,
    val productSize: String = "",
    )