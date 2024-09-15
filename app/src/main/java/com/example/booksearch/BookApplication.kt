package com.example.booksearch

import android.app.Application
import com.example.booksearch.data.AppContainer
import com.example.booksearch.data.DefaultAppContainer

class BookApplication :Application(){
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}