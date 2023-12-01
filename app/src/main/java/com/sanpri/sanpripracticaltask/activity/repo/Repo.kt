package com.evince.sanpripracticaltask.activity.repo

import com.evince.sanpripracticaltask.activity.model.UploadedMediaRes
import com.evince.sanpripracticaltask.network.IApiService
import com.evince.sanpripracticaltask.utils.BaseDataSource
import com.evince.sanpripracticaltask.utils.CommanModel
import com.evince.sanpripracticaltask.utils.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class Repo(private var apiService : IApiService) : BaseDataSource() {

    suspend fun getUploadedMediaRes(
        fileName: String,
        fileType: String,
        authToken: String,
        userName: String,
        fileChunk: String
    ) : kotlinx.coroutines.flow.Flow<Resources<CommanModel<UploadedMediaRes>>>{
        return flow {
            val result = safeApiCall {
                apiService.getMediaUploadRes(
                    fileName,
                    fileType,
                    fileChunk,
                    authToken,
                    userName
                )
            }


            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun checkFileSize(
        fileName: String,
        fileType: String,
        authToken: String,
        userName: String
    ) : kotlinx.coroutines.flow.Flow<Resources<CommanModel<UploadedMediaRes>>>{
        return flow {
            val myresult = safeApiCall { apiService.checkFile(fileName,fileType,authToken,userName) }
            emit(myresult)
        }.flowOn(Dispatchers.IO)
    }

}