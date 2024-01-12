package com.example.spotify_clone.presentation.musiclist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.math.floor
import kotlin.random.Random


@Composable
fun MyMusicScreen(
    progress: Float,
    onProgress: (Float) -> Unit,
    currentPlayingAudio: AudioItem,
    isAudioPlaying: Boolean,
    audioList: List<AudioItem>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomBarPlayer(
                progress = progress,
                onProgress = onProgress,
                audio = currentPlayingAudio,
                isAudioPlaying = isAudioPlaying,
                onStart = onStart,
                onNext = onNext
            )
        }
    ) {
        LazyColumn(contentPadding = it) {
            itemsIndexed(audioList) { index: Int, item: AudioItem ->
                AudioItemComp(item = item) {
                    onItemClick(index)
                }
            }
        }
    }

}

private fun timeStampToDuration(position: Long): String {
    val totalSeconds = floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)
    return if (position < 0) "--:--"
    else "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun AudioItemComp(
    item: AudioItem,
    onItemClick: () -> Unit
) {
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
    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .height(60.dp)
            .clickable {
                onItemClick()
            }
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
            Text(
                text = timeStampToDuration(item.duration),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
private fun BottomBarPlayer(
    progress: Float,
    onProgress: (Float) -> Unit,
    audio: AudioItem,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtistInfo(modifier = Modifier.weight(1f), audio = audio)
                MediaPlayerController(
                    isAudioPlaying = isAudioPlaying,
                    onStart = onStart,
                    onNext = onNext
                )
                Slider(
                    value = progress,
                    onValueChange = {
                        onProgress(it)
                    },
                    valueRange = 0f..100f
                )
            }
        }
    }
}

@Composable
fun MediaPlayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        PlayerIconItem(
            icon = if (isAudioPlaying) Icons.Default.Pause
            else Icons.Default.PlayArrow
        ) {
            onStart()
        }
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            modifier = Modifier.clickable {
                onNext()
            },
            contentDescription = null
        )
    }
}

@Composable
private fun ArtistInfo(
    modifier: Modifier,
    audio: AudioItem
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIconItem(
            icon = Icons.Default.Home,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        ) {}
        Spacer(modifier = Modifier.size(4.dp))
        Column {
            Text(
                modifier = Modifier.weight(1f),
                text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = audio.artist!!,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}

@Composable
fun PlayerIconItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}

