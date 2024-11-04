package com.edu.admin.models

data class Customer(
    val userId: String = "",
    val name: String = "",
    val emailId: String = "",
    val mobileNo: String = "",
    val whatsapp: String = "",
    val DOB: String = "",
    val address: String = "",
    val instaId: String = "",
    val pointsEarned: String = "",
    val offersAvailable: String = "",
    val status: String = "Active"
)