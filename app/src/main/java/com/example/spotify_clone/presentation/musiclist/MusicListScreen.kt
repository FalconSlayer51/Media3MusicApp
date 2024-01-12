package com.example.spotify_clone.presentation.musiclist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.spotify_clone.R
import com.example.spotify_clone.data.model.AudioItem

@Composable
fun MyMusicListScreen(
    viewModel: MusicListScreenViewModel,
    permissionGranted: Boolean,
    requestPermission: () -> Unit
) {
    val musicList by viewModel.musicList.collectAsState()
    val scrollState = rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState(scrollState.intValue)

    LaunchedEffect(permissionGranted) {
        viewModel.getMusicList()
    }

    LaunchedEffect(scrollState.intValue) {
        scrollState.intValue = listState.firstVisibleItemIndex
    }

    Scaffold { paddingValues ->
        if (!permissionGranted) {
            NoPermissionState {
                requestPermission.invoke()
            }
            return@Scaffold
        }
        if (musicList.isEmpty()) {
            NoMusicListState()
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            state = listState
        ) {
            items(
                key = { musicList[it].id },
                count = musicList.size,
            ) { index ->
                val item = musicList[index]
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = item.artWork)
                        .apply(block = fun ImageRequest.Builder.() {
                            transformations(
                                RoundedCornersTransformation(
                                    topLeft = 8f,
                                    topRight = 8f,
                                    bottomLeft = 8f,
                                    bottomRight = 8f
                                )
                            )
                            placeholder(R.drawable.ic_launcher_foreground)
                            error(R.drawable.ic_launcher_foreground)
                            allowHardware(true)
                            crossfade(true)
                        })
                        .allowConversionToBitmap(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                )
                MusicCard(painter, item)
            }
        }
    }
}

@Composable
private fun NoMusicListState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No music found")
    }
}

@Composable
private fun NoPermissionState(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "No Permission given")
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                onClick.invoke()
            }) {
                Text("Request Storage Permission")
            }
        }
    }
}


@Composable
private fun MusicCard(painter: Painter, item: AudioItem) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                painter = painter,
                contentDescription = "artWork",
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(.6f)
                        .height(20.dp),
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.fillMaxWidth(.6f),
                    text = item.artist!!,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(50.dp))
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play/pause"
            )
        }
    }
}
