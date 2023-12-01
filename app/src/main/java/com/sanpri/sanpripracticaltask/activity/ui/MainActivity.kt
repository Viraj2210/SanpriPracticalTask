package com.evince. evincepracticaltask.activity.ui

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.d2k.losapp.ui.connectivityobserver.ConnectivityObserver
import com.d2k.losapp.ui.connectivityobserver.NetworkConnectivityObserver
import com.d2k.losapp.ui.connectivityobserver.NoInternetConnectivity
import com.d2k.shg.networking.ApiClient
import com.evince.sanpripracticaltask.*
import com.evince.sanpripracticaltask.activity.model.UploadMediaReq
import com.evince.sanpripracticaltask.activity.vm.MediaUploadVM
import com.evince.sanpripracticaltask.base.ViewModelFactory
import com.evince.sanpripracticaltask.databinding.ActivityMainBinding
import com.evince.sanpripracticaltask.extension.launchActivity
import com.evince.sanpripracticaltask.utils.Status
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.android.synthetic.main.activity_main.videoView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding: ActivityMainBinding
    lateinit var mediaUploadVM: MediaUploadVM
    lateinit var layoutManager : LinearLayoutManager
    lateinit var connectivityObserver: ConnectivityObserver
    var uploadMediaReq : UploadMediaReq?=null
    private val pickImageVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the selected media URI
                val selectedMediaUri = result.data?.data

                val (fileName, fileExtension) = getFileNameAndExtension(selectedMediaUri!!)

                if (fileExtension?.contains("jpg") == true ||
                    fileExtension!!.contains("png") == true||
                    fileExtension.contains("jpeg") == true){

                    activityMainBinding.imageView.visibility = View.VISIBLE
                    activityMainBinding.videoView.visibility = View.GONE
                    Glide.with(this).load(selectedMediaUri).into(activityMainBinding.imageView)
                }else{
                    activityMainBinding.imageView.visibility = View.GONE
                    activityMainBinding.videoView.visibility = View.VISIBLE
                    activityMainBinding.videoView.setVideoURI(selectedMediaUri)
                }

                activityMainBinding.btnSave.visibility = View.VISIBLE
                val mediaController = MediaController(this)
                mediaController.setAnchorView(videoView)
                videoView.setMediaController(mediaController)

                videoView.start()
                Log.v("MediaUrl",""+selectedMediaUri)
                selectedMediaUri?.let {

                    GlobalScope.launch(Dispatchers.Main) {
                        val base64String = withContext(Dispatchers.IO) {
                            parseVideoToBase64ChunksInBackground(it)
                        }

                        Log.v("Base64",""+base64String)

                        val uploadMediaReq1 = UploadMediaReq(fileName,fileExtension,
                            "94cdffda0d6376c73f5d83c263f05653","viraj_pawar", base64String!!
                        )
                        uploadMediaReq = uploadMediaReq1
                    }
                }
            }
        }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        init()
    }

    fun init(){
        mediaUploadVM = ViewModelProvider(this,ViewModelFactory(ApiClient.aPIService)).get(MediaUploadVM::class.java)
        connectivityObserver = NetworkConnectivityObserver(this)
        val dialog = Dialog(this)

        activityMainBinding.imageView.visibility = View.GONE
        activityMainBinding.videoView.visibility = View.GONE
        activityMainBinding.btnSave.visibility = View.GONE
        lifecycleScope.launchWhenCreated {
            connectivityObserver.observe().collect {
                if (it.equals(ConnectivityObserver.Status.Lost)){
                    launchActivity<NoInternetConnectivity>()
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            mediaUploadVM.mediaUploadRes.collect{
                when(it.status){
                    Status.SUCCESS->{
                        showProgress(1,dialog)
                        Toast.makeText(this@MainActivity,""+it.data?.message,Toast.LENGTH_SHORT).show()
                        if (it.data?.message.equals("Data Saved Successfully ",true)){

                            Log.v("RES","On Success")
                            mediaUploadVM.checkSize(
                                uploadMediaReq!!.file_name,
                                uploadMediaReq!!.file_type,
                                uploadMediaReq!!.authentication_token,
                                uploadMediaReq!!.user_name,
                                )


                        }else{
                            Log.v("RES","Else part")
                        }
                    }
                    Status.ERROR->{
                        showProgress(1,dialog)
                    }
                    Status.LOADING->{
                        showProgress(1,dialog)
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            mediaUploadVM.getSize.collect{
                when(it.status){
                    Status.SUCCESS->{
                        Toast.makeText(this@MainActivity,"Size of your uploaded file is"+ (it.data?.data?.data?.get(0)?.size ?: ""),Toast.LENGTH_SHORT).show()
                    }
                    Status.LOADING->{

                    }
                    Status.ERROR->{

                    }
                }
            }
        }
        activityMainBinding.buttonChooseMedia.setOnClickListener {
            requestStoragePermission()
        }

        activityMainBinding.btnSave.setOnClickListener {
            showProgress(0,dialog)
            uploadImageToServer(uploadMediaReq!!)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private suspend fun parseVideoToBase64ChunksInBackground(videoUri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(videoUri)
            if (inputStream != null) {
                val bufferedInputStream = BufferedInputStream(inputStream)

                val chunkSize = 1024 * 1024
                val byteArray = ByteArray(chunkSize)
                val stringBuilder = StringBuilder()

                var bytesRead: Int
                while (bufferedInputStream.read(byteArray).also { bytesRead = it } != -1) {
                    val chunk = byteArray.copyOf(bytesRead)
                    val base64Chunk = Base64.encodeToString(chunk, Base64.DEFAULT)
                    stringBuilder.append(base64Chunk)
                }

                bufferedInputStream.close()
                inputStream.close()

                stringBuilder.toString()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

/*
    private suspend fun convertVideoToBase64(filePath: String): String? {
        return try {
            val file = File(filePath)
            val fileInputStream = FileInputStream(file)
            val buffer = ByteArray(8192) // 8 KB buffer size (adjust as needed)
            val outputStream = StringBuilder()

            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                val chunk = buffer.copyOf(bytesRead)
                val base64Chunk = Base64.encodeToString(chunk, Base64.DEFAULT)
                outputStream.append(base64Chunk)
            }

            fileInputStream.close()

            outputStream.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
*/

    private fun openGalleryForImageOrVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/* video/*"

        pickImageVideo.launch(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGalleryForImageOrVideo()
            }
        }


    private fun requestStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGalleryForImageOrVideo()
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

/*
    private fun convertMediaToBase64(uri: android.net.Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        inputStream?.let {
            val buffer = ByteArray(1024)
            var bytesRead: Int
            val output = ByteArrayOutputStream()

            try {
                while (it.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                val data = output.toByteArray()
                return Base64.encodeToString(data, Base64.DEFAULT)
            } finally {
                it.close()
                output.close()
            }
        }
        return ""
    }
*/


    fun getFileNameAndExtension(uri: Uri): Pair<String, String?> {
        val contentResolver: ContentResolver = contentResolver

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
            DocumentsContract.isDocumentUri(this, uri)
        ) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val mimeType = contentResolver.getType(uri)
            val extensionFromMimeType = if (mimeType != null) {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } else {
                null
            }

            val name = if (split.size > 1) {
                split[1]
            } else {
                null
            }

            Pair(name ?: "", extensionFromMimeType)
        } else {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(uri, filePathColumn, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(filePathColumn[0])
                    val pathIndex = it.getColumnIndex(filePathColumn[1])

                    val name = it.getString(nameIndex)
                    val path = it.getString(pathIndex)

                    val dotIndex = name.lastIndexOf(".")
                    val extension = if (dotIndex != -1 && dotIndex < name.length - 1) {
                        name.substring(dotIndex + 1)
                    } else {
                        null
                    }

                    Toast.makeText(this,"Name : "+name+"\nExtension : "+extension,Toast.LENGTH_SHORT).show()
                    Pair(name, extension)
                } else {
                    Pair("", null)
                }
            } ?: Pair("", null)
        }
    }

    fun uploadImageToServer(uploadMediaReq: UploadMediaReq) {
        this.uploadMediaReq = uploadMediaReq
        mediaUploadVM.mediaUpload(uploadMediaReq.file_name,
            uploadMediaReq.file_type,
            uploadMediaReq.authentication_token,
            uploadMediaReq.user_name,
            uploadMediaReq.file_chunk)
    }

    fun showProgress(value: Int = 0, dialog : Dialog) {
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_progress)

        val progress: CircularProgressIndicator = dialog.findViewById(R.id.progress)

        if (value == 0) {
            progress.show()
            dialog.show()
        } else {
            progress.hide()
            dialog.dismiss()
        }
    }


}