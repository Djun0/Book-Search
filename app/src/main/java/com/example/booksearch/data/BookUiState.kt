package com.example.booksearch.data

import kotlinx.serialization.Serializable

@Serializable
data class BookUiState(
    val id:String,
    var thumbnail:String,
    val title:String,
    val authors:String,
    val description:String
)
