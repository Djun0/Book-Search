package com.example.booksearch.network

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val items: List<BookInfo>
)

@Serializable
data class BookInfo(
    val id: String,
    val volumeInfo: VolumeInfo
)

@Serializable
data class VolumeInfo(
    val imageLinks: ImageLinks? = null,
    val title: String,
    val authors: List<String>? = null,
    val description: String? = null
)

@Serializable
data class ImageLinks(
    val thumbnail: String,
    val medium: String?=null
)
