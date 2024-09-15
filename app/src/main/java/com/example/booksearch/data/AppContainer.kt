package com.example.booksearch.data

import com.example.booksearch.network.BookApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val bookRepository: BookRepository
}
class DefaultAppContainer : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val baseUrl="https://www.googleapis.com/books/v1/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    private  val retrofitService: BookApiService by lazy {
        retrofit.create(BookApiService::class.java)
    }
    override val bookRepository: BookRepository by lazy {
        NetWorkBookRepository(retrofitService)
    }

}