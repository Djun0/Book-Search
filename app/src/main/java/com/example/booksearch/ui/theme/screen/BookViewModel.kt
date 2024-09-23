package com.example.booksearch.ui.theme.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.booksearch.BookApplication
import com.example.booksearch.data.BookRepository
import com.example.booksearch.data.BookUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed interface FetchUiState {
    data class Success(val books: List<Pair<String, String?>>) : FetchUiState
    object Error : FetchUiState
    object Loading : FetchUiState
}

class BookViewModel(private val bookRepository: BookRepository) : ViewModel() {


    var fetchUiState:FetchUiState by mutableStateOf(FetchUiState.Loading)
        private set

    private val _uiState = MutableStateFlow(BookUiState("", "","", "Chưa rõ",""))
    val uiState = _uiState.asStateFlow()

    init {
        getListBook("live",10)
    }

    fun getPhotoById(id: String) {
        viewModelScope.launch {
            val infoBook = safeApiCall {
                bookRepository.getBookById(id).volumeInfo
            }
            Log.d("API_RESPONSE", "Response: $infoBook")
            infoBook?.let {
                Log.d("BOOK_INFO", "Title: ${it.title}, Authors: ${it.authors}, Description: ${it.description}")
                updateBookUiState(
                    thumbnail = it.imageLinks?.medium.toString(),
                    title = it.title,
                    authors = it.authors?.joinToString(", ") ?: "Unknown",
                    description =HtmlCompat.fromHtml(it.description.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                )


            } ?: run {
                Log.d("BOOK_INFO", "No data found")
            }
        }
    }
    private fun updateBookUiState(thumbnail: String, title:String, authors: String, description:String) {
        _uiState.update { currentUiState ->
            currentUiState.copy(thumbnail = thumbnail, title = title, authors = authors, description = description)
        }
        Log.d("BookUiState", "${uiState.value}")
    }

    fun updateIdUiState(id: String) {
        _uiState.update { currentUiState ->
            currentUiState.copy(id = id)
        }
        Log.d("BookUiState", "${uiState.value}")
    }

    fun getListBook(query: String, maxResults: Int = 40) {
        viewModelScope.launch {
            fetchUiState = FetchUiState.Loading
            val thumbnails = safeApiCall {
                bookRepository.getBooks(query, maxResults).items.map { bookInfo ->
                    Pair(bookInfo.id, bookInfo.volumeInfo.imageLinks?.thumbnail)
                }.filterNot { it.second == null }
            }
            fetchUiState = thumbnails?.let { FetchUiState.Success(it) } ?: FetchUiState.Error
        }
    }


    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): T? {
        return withContext(Dispatchers.IO) {
            try {
                apiCall()
            } catch (e: HttpException) {
                Log.e("ApiCall", "HTTP exception occurred", e)
                null
            } catch (e: IOException) {
                Log.e("ApiCall", "Network or IO exception occurred", e)
                null
            } catch (e: Exception) {
                Log.e("ApiCall", "Unexpected exception occurred", e)
                null
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookApplication)
                val bookRepository = application.container.bookRepository
                BookViewModel(bookRepository)
            }
        }
    }
}
