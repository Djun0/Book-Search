package com.example.booksearch.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booksearch.R
import com.example.booksearch.ui.theme.screen.BookViewModel
import com.example.booksearch.ui.theme.screen.DetailsScreen
import com.example.booksearch.ui.theme.screen.HomeScreen



enum class BookScreenMap {
    Start,
    Detail
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookHome(onBookClick: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bookViewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AmphibianTopAppBar(scrollBehavior = scrollBehavior)
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = { newQuery ->
                        query = newQuery
                    },
                    onSearch = {
                        if (query.isNotBlank()) {
                            bookViewModel.getListBook(query)
                        }
                    },
                    active = false,
                    onActiveChange = { },
                    modifier = Modifier.fillMaxWidth()
                ) {}

                Spacer(modifier = Modifier.height(16.dp))

                HomeScreen(
                    onBookClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    fetchUiState = bookViewModel.fetchUiState,
                    retryAction = {
                        if (query.isNotBlank()) {
                            bookViewModel.getListBook(query)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmphibianTopAppBar(modifier: Modifier = Modifier, scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier
    )
}

@Composable
fun NavigateMap(navController: NavHostController = rememberNavController(),
                viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)) {

    NavHost(navController = navController, startDestination = BookScreenMap.Start.name) {
        composable(route = BookScreenMap.Start.name) {
            BookHome(onBookClick = { id ->
                viewModel.getPhotoById(id)
                navController.navigate(BookScreenMap.Detail.name)
            })
        }
        composable(route = BookScreenMap.Detail.name) {

            val uiState by viewModel.uiState.collectAsState()
            DetailsScreen(uiState.thumbnail, title = uiState.title,
                authors = uiState.authors,
                description = uiState.description)
        }
    }
}
