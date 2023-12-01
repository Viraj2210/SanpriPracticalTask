package com.evince.sanpripracticaltask.activity.model

data class UploadMediaReq(
    var file_name : String,
    var file_type : String,
    var authentication_token : String,
    var user_name : String,
    var file_chunk : String
)
