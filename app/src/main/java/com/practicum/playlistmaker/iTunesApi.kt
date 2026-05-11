package com.practicum.playlistmaker

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface iTunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") term: String): Call<SearchResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://itunes.apple.com"

    val api: iTunesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(iTunesApi::class.java)
    }
}