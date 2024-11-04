package com.edu.admin.models

data class Lesson(
    val lessonId: String = "",
    val lessonTitle: String = "",
    val lessonDescription: String = "",
    val isCompleted:Boolean = false,
    val theoriesCount:Int = 0,
    val videosCount:Int = 0
)