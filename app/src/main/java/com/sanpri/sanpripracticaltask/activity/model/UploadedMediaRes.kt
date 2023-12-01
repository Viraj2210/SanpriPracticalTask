package com.evince.sanpripracticaltask.activity.model

data class UploadedMediaRes(
    val `data`: List<Data>,
    val file_path: String,
    val message: String,
    val success: Int
)