package com.aloysius.recipeapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aloysius.recipeapp.Utils
import com.aloysius.recipeapp.http.ImgurApi
import com.aloysius.recipeapp.response.UploadResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecipeViewModel : ViewModel() {

    val uploadImageLiveData = MutableLiveData<UploadResponse>()
    val errorLiveData = MutableLiveData<Throwable>()

    var imageUrl: String? = null

    fun uploadImage(file: File) {
        CoroutineScope(Dispatchers.Default).launch {
            val filePart =
                MultipartBody.Part.createFormData("image", file.name, file.asRequestBody())

            val response =
                Utils.getRetrofitInstance().create(ImgurApi::class.java)
                    .uploadFile(filePart, file.name.toRequestBody())

            if (response.isSuccessful) {
                response.body()?.run{
                    imageUrl = this.upload.link
                    uploadImageLiveData.postValue(this)
                }
            } else {
                //Can handle various network exception to return more detail error msg in future.
                errorLiveData.postValue(Exception("Unknown network Exception."))
            }
        }
    }

}