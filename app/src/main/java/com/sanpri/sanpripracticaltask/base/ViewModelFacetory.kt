package com.evince.sanpripracticaltask.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.evince.sanpripracticaltask.activity.repo.Repo
import com.evince.sanpripracticaltask.activity.vm.MediaUploadVM
import com.evince.sanpripracticaltask.network.IApiService

class ViewModelFactory(private val apiService: IApiService) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaUploadVM::class.java)){
            return MediaUploadVM(Repo(apiService)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}