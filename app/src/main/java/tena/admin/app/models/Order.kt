package tena.admin.app.models

import tena.health.care.models.CartItem

data class Order(
    val orderId: String = "",
    val cardItems: List<CartItem?> = emptyList(),
    val selectedPayment: String = "",
    val orderPlacedDate: String = "",
    val subTotal: String = "",
    val deliveryCharge: String = "",
    val tax: String = "",
    val total: String = "",
    val currency: String = "INR",
    val shippingAddress: String = "",
    val userId: String = "",
    val orderStatus: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
)