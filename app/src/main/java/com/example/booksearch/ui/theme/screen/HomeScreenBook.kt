package com.example.booksearch.ui.theme.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksearch.R

@Composable
fun HomeScreen(
    fetchUiState: FetchUiState, retryAction: () -> Unit,
    modifier: Modifier=Modifier, onBookClick: (String) -> Unit
){
    when(fetchUiState){
        is FetchUiState.Loading -> LoadingScreen(modifier=modifier)

        is FetchUiState.Success -> PhotosGridScreen(fetchUiState.books,
            onBookClick = onBookClick,
            modifier =modifier.padding(1.dp))

        is FetchUiState.Error -> ErrorScreen(retryAction,modifier=modifier)

    }
}

@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
fun PhotosGridScreen(books: List<Pair<String, String?>>, onBookClick: (String) -> Unit, modifier: Modifier = Modifier) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        items(items = books, key = { photo -> photo }) { photo ->
            BookPhotoCard(
                onClick = {onBookClick(photo.first)},
                book = photo.second.toString(),
                modifier = modifier
                    .size(height = 193.dp, width = 128.dp)
            )
        }
    }
}

@Composable
fun BookPhotoCard(book: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(book.replace("http:", "https:"))
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = painterResource(R.drawable.baseline_broken_image_24),
            placeholder = painterResource(R.drawable.baseline_image_24),
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}



@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}
