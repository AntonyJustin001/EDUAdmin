package com.edu.admin.models

data class Student(
    val userId: String = "",
    val name: String = "",
    val emailId: String = "",
    val mobileNo: String = "",
    val pointsEarned: Int = 0,
    val noOfAttempts: Int = 0,
    val completedLevel: String = "",
)