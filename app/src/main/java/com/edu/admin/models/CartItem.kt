package com.health.care.models

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val productImageUrl: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
