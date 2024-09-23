package com.example.booksearch.ui.theme.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.booksearch.BookApplication
import com.example.booksearch.data.BookRepository
import com.example.booksearch.data.BookUiState
import com.example.booksearch.ui.theme.BookScreenMap
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
    data object Error : FetchUiState
    data object Loading : FetchUiState
}
sealed interface FetchPhotoBook {
    data object  Success:FetchPhotoBook
    data object Normal : FetchPhotoBook
    data object Loading : FetchPhotoBook
}

class BookViewModel(private val bookRepository: BookRepository) : ViewModel() {

    var shouldNavigate by mutableStateOf(false)
    var fetchUiState:FetchUiState by mutableStateOf(FetchUiState.Loading)
        private set
    var fetchPhotoBook:FetchPhotoBook by mutableStateOf(FetchPhotoBook.Normal)
        private set
    private val _uiState = MutableStateFlow(BookUiState("", "","", "Chưa rõ",""))
    val uiState = _uiState.asStateFlow()

    init {
        getListBook("live",10)
    }
    @OptIn(ExperimentalCoilApi::class)
    private fun isImageInCache(imageUrl: String, imageLoader: ImageLoader, context: Context): Boolean {
        // Tạo ImageRequest
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()

        // Lấy cache key
        val cacheKey = request.memoryCacheKey

        // Kiểm tra cache bộ nhớ (RAM)
        val cachedImageInMemory = cacheKey?.let { imageLoader.memoryCache?.get(it) }

        // Nếu ảnh đã có trong bộ nhớ RAM, trả về true
        if (cachedImageInMemory != null) {
            Log.d("Image Cache", "Image found in memory cache")
            return true
        }

        // Nếu không có trong bộ nhớ RAM, kiểm tra cache ổ đĩa
        val diskCacheKey = request.diskCacheKey
        val snapshot = diskCacheKey?.let { imageLoader.diskCache?.openSnapshot(it) }

        // Trả về true nếu ảnh có trong cache ổ đĩa
        if (snapshot != null) {
            Log.d("Image Cache", "Image found in disk cache")
            snapshot.close() // Đảm bảo đóng snapshot để tránh rò rỉ tài nguyên
            return true
        }

        // Ảnh không có trong cache
        Log.d("Image Cache", "Image not found in cache")
        return false
    }

    fun getPhotoById(id: String,imageLoader: ImageLoader,
                     context: Context) {
        viewModelScope.launch {
            val infoBook = safeApiCall {
                bookRepository.getBookById(id).volumeInfo
            }
            Log.d("API_RESPONSE", "Response: $infoBook")
            infoBook?.let {
                updateBookUiState(
                    thumbnail =it.imageLinks?.medium.toString().replace("http:", "https:"),
                    title = it.title,
                    authors = it.authors?.joinToString(", ") ?: "Unknown",
                    description =HtmlCompat.fromHtml(it.description.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                )
                val cachedImage = isImageInCache(it.imageLinks?.medium.toString().replace("http:", "https:"), imageLoader,context)
                Log.d("Cached Image","$cachedImage")
                if (cachedImage) {
                    shouldNavigate=true

                } else {

                    val request = ImageRequest.Builder(context)
                        .data(it.imageLinks?.medium.toString().replace("http:", "https:"))

                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .decoderFactory { _, _, _ ->
                            Decoder { DecodeResult(ColorDrawable(Color.BLACK), false) }
                        }

                        .listener(
                            onStart={ _ ->
                                fetchPhotoBook=FetchPhotoBook.Loading
                                Log.d("Request Start","Start")
                            }
                            ,
                            onSuccess = { _, _ ->
                                fetchPhotoBook=FetchPhotoBook.Success

                                shouldNavigate=true
                                Log.d("Request Load", "Success")
                            }
                            ,

                            onError={
                                    _, _ ->
                                fetchPhotoBook=FetchPhotoBook.Normal

                                Log.d("Request Load2","Success")
                            }

                        )
                        .build()

                    imageLoader.enqueue(request)

                }


                Log.d("BOOK_INFO", "Title: ${it.title}, Authors: ${it.authors}, Description: ${it.description}")


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
