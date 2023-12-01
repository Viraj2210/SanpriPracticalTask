package com.evince.sanpripracticaltask.network

import com.evince.sanpripracticaltask.activity.model.UploadedMediaRes
import com.evince.sanpripracticaltask.utils.CommanModel
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface IApiService {

    /**
     * Authenticate call.
     *
     * @param loginRequest the login request Ex:- <String,String>
     * @return the call
     */
    /*IAuthenticate this API Call Interface Is use to Login*/


    @Multipart
    @POST("upload_chunk_file.php")
    suspend fun getMediaUploadRes(
        @Part("file_name") fileName : String,
        @Part("file_type") fileType : String,
        @Part("file_chunk") fileChunk: String,
        @Part("authentication_token") auth : String,
        @Part("user_name") userName : String) :
            Response<CommanModel<UploadedMediaRes>>

    @Multipart
    @POST("check_file_size.php")
    suspend fun checkFile(
        @Part("file_name") fileName : String,
        @Part("file_type") fileType : String,
        @Part("authentication_token") auth : String,
        @Part("user_name") userName : String) :
            Response<CommanModel<UploadedMediaRes>>
}