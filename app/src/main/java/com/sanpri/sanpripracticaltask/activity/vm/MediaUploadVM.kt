package com.evince.sanpripracticaltask.activity.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evince.sanpripracticaltask.activity.model.UploadedMediaRes
import com.evince.sanpripracticaltask.activity.repo.Repo
import com.evince.sanpripracticaltask.utils.CommanModel
import com.evince.sanpripracticaltask.utils.Resources
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MediaUploadVM(val repo: Repo) : ViewModel() {
    private val _mediaUploadRes = Channel<Resources<CommanModel<UploadedMediaRes>>>(Channel.BUFFERED)
    public val mediaUploadRes = _mediaUploadRes.receiveAsFlow()


    private val _getSize = Channel<Resources<CommanModel<UploadedMediaRes>>>(Channel.BUFFERED)
    public val getSize = _getSize.receiveAsFlow()


    fun mediaUpload(fileName: String,
                    fileType: String,
                    authToken: String,
                    userName: String,
                    fileChunk: String){
        viewModelScope.launch {
            repo.getUploadedMediaRes(
                fileName,
                fileType,
                authToken,
                userName,
                fileChunk).catch {
                _mediaUploadRes.send(Resources.error(null, message = it.message?:"Error Occurred"))
            }.collect{
                _mediaUploadRes.send(it)
            }
        }
    }

    fun checkSize(fileName: String,
                  fileType: String,
                  authToken: String,
                  userName: String, ){
        viewModelScope.launch {
            repo.checkFileSize(
                fileName,
                fileType,
                authToken,
                userName).catch {
                _getSize.send(Resources.error(null, message = it.message?:"Error Occurred"))
            }.collect{
                _getSize.send(it)
            }
        }

    }

}