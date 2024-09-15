package com.example.booksearch.data


import com.example.booksearch.network.BookApiService
import com.example.booksearch.network.BookInfo
import com.example.booksearch.network.QueryResult


interface BookRepository {
    suspend fun getBooks(query: String,maxResults: Int = 10): QueryResult
    suspend fun getBookById(id:String):BookInfo
}
class NetWorkBookRepository(private val bookApiService: BookApiService): BookRepository{
    override suspend fun getBooks(query: String, maxResults: Int): QueryResult {
        return bookApiService.getBooks(query,maxResults)
    }

    override suspend fun getBookById(id: String): BookInfo {
        return bookApiService.getBookById(id)
    }
}