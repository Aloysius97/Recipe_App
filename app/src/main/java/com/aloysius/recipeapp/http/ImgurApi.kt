package com.aloysius.recipeapp.http

import com.aloysius.recipeapp.response.UploadResponse

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImgurApi {
    @Multipart
    @POST("/3/upload")
    suspend fun uploadFile(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody? = null
    ): Response<UploadResponse>
}