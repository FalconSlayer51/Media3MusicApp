package com.example.spotify_clone.presentation.musiclist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.spotify_clone.data.model.AudioItem
import com.example.spotify_clone.data.repository.MusicRepository
import com.example.spotify_clone.player.service.AudioServiceHandler
import com.example.spotify_clone.player.service.AudioState
import com.example.spotify_clone.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private val audioDummy = AudioItem(
    0L,
    "",
    "",
    "",
    0L,
    "".toUri(),
    ""
)

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class MusicListScreenViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val audioServiceHandler: AudioServiceHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var duration by savedStateHandle.saveable { mutableLongStateOf(1L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(audioDummy) }
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<AudioItem>()) }
    var isSelfLoop by savedStateHandle.saveable { mutableStateOf(false) }

    private val _uiState: MutableState<UIState> = mutableStateOf(UIState.Initial)
    val uiState: State<UIState> = _uiState
    private val _musicList: MutableStateFlow<List<AudioItem>> =
        MutableStateFlow(listOf<AudioItem>())
    val musicList: StateFlow<List<AudioItem>> = _musicList


    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.Initial
                    is AudioState.Buffering -> calculateProgress(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                    is AudioState.Progress -> calculateProgress(mediaState.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }

                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }

                    is AudioState.SelfLoop -> {
                        isSelfLoop = mediaState.enabled
                    }
                }
            }
        }
    }

    init {
        loadAudioData()
    }

    fun onUIEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when (uiEvents) {
            UIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            is UIEvents.PlayPause -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }

            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }

            is UIEvents.SelectedAudioChanged -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChanged,
                    selectedAudioIndex = uiEvents.index
                )
            }

            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(uiEvents.newProgress)
                )
                progress = uiEvents.newProgress
            }

            is UIEvents.EnableSelfLoop -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelfLoop(isSelfLoop)
                )

            }
        }
    }

    private fun calculateProgress(currentProgress: Long) {
        progress = if (currentProgress > 0) {
            (currentProgress.toFloat() / duration.toFloat()) * 100f
        } else {
            0f
        }
        progressString = formatString(currentProgress)
    }

    private fun formatString(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val second = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
        return String.format("%02d:%02d", minute, second)
    }

    fun loadAudioData() = viewModelScope.launch {
        val audio = musicRepository.getAudio()
        audioList = audio
        _musicList.value = audioList
        setMediaItems()
    }

    private fun setMediaItems() {
        audioList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.album)
                        .build()
                ).build()
        }.also {
            audioServiceHandler.setMediaItemList(it)
        }
    }

    fun getMusicList() {
        _musicList.value = musicRepository.getAudio()
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }
}

sealed class UIEvents {
    object PlayPause : UIEvents()
    data class SelectedAudioChanged(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    object SeekToNext : UIEvents()
    object Backward : UIEvents()
    object Forward : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()

    data class EnableSelfLoop(val enabled: Boolean) : UIEvents()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()

    object IsSelfLoop : UIState()
}