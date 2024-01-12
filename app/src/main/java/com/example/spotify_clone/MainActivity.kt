@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.spotify_clone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spotify_clone.player.service.AudioService
import com.example.spotify_clone.presentation.home.HomeScreen
import com.example.spotify_clone.presentation.musiclist.MusicListScreenViewModel
import com.example.spotify_clone.presentation.musiclist.MyMusicScreen
import com.example.spotify_clone.presentation.musiclist.UIEvents
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isServiceRunning: Boolean = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            val systemUiController = rememberSystemUiController()
            val permissionState =
                rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
            val lifeCycleOwner = LocalLifecycleOwner.current

            DisposableEffect(key1 = lifeCycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        permissionState.launchPermissionRequest()
                    }
                }
                lifeCycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifeCycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (isSystemInDarkTheme()) {
                systemUiController.setStatusBarColor(Color.Black)
            } else {
                systemUiController.setStatusBarColor(Color.White)
            }
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    @Composable
    private fun NavGraph(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "/home") {
            composable("/home") {
                HomeScreen {
                    navController.navigate("/mymusic")
                }
            }
            composable("/mymusic") {
                val context = LocalContext.current
                var permissionGranted by remember { mutableStateOf(hasStoragePermission(context)) }
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    permissionGranted = isGranted
                }
                val musicListScreenViewModel by viewModels<MusicListScreenViewModel>()
                LaunchedEffect(Unit) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                MyMusicScreen(
                    progress = musicListScreenViewModel.progress,
                    onProgress = { musicListScreenViewModel.onUIEvents(UIEvents.SeekTo(it)) },
                    currentPlayingAudio = musicListScreenViewModel.currentSelectedAudio,
                    isAudioPlaying = musicListScreenViewModel.isPlaying,
                    audioList = musicListScreenViewModel.audioList,
                    onStart = { musicListScreenViewModel.onUIEvents(UIEvents.PlayPause) },
                    onItemClick = {
                        musicListScreenViewModel.onUIEvents(UIEvents.SelectedAudioChanged(it))
                        startService()
                    },
                    onNext = {
                        musicListScreenViewModel.onUIEvents(UIEvents.SeekToNext)
                    },
                    isChecked = musicListScreenViewModel.isSelfLoop,
                    onCheckedChange = {
                        musicListScreenViewModel.isSelfLoop = it
                        musicListScreenViewModel.onUIEvents(UIEvents.EnableSelfLoop(it))
                    },
                )
            }
        }
    }


    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, AudioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }

    private fun hasStoragePermission(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }
}


