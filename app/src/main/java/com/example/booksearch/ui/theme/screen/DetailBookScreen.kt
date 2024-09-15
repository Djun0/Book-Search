package com.example.booksearch.ui.theme.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.booksearch.R


@Composable
fun DetailsScreen(imgSrc: String, title: String, description: String, authors: String, modifier: Modifier = Modifier) {
Card(modifier = Modifier
    .fillMaxSize()
    .verticalScroll(rememberScrollState())) {
    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(imgSrc.replace("http:", "https:"))
                .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = painterResource(R.drawable.baseline_broken_image_24),
            placeholder = painterResource(R.drawable.white_placeholder),
            modifier = Modifier
                .fillMaxWidth().aspectRatio(0.7f)
                ,
            contentScale = ContentScale.FillBounds
        )

            Column(modifier = Modifier.padding(5.dp)) {
                Text(
                    text = title,
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = authors,
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Giới thiệu về sách này",
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = description,
                    modifier = Modifier.padding(5.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


