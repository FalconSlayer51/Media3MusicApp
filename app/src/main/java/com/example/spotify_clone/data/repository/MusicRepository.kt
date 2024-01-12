package com.example.spotify_clone.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.example.spotify_clone.data.model.AudioItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @WorkerThread
    fun getAudio(): List<AudioItem> {
        return getAllAudioFiles()
    }

    private fun getAllAudioFiles(): List<AudioItem> {
        val audioFiles = mutableListOf<AudioItem>()
        val uri: Uri = Media.EXTERNAL_CONTENT_URI
        val selection = Media.IS_MUSIC + "!= 0"
        val sortOrder = "${Media.DATE_ADDED} DESC"
        val projection = arrayOf(
            Media._ID,
            Media.TITLE,
            Media.ARTIST,
            Media.DATA, // Path to the audio file
            Media.DURATION, // Duration of the audio file
            Media.ALBUM_ID
        )

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor: Cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(Media.ARTIST)
            val duration = cursor.getColumnIndexOrThrow(Media.DURATION)
            val path = cursor.getColumnIndexOrThrow(Media.DATA)
            val albumId = cursor.getColumnIndexOrThrow(Media.ALBUM_ID)
            cursor.apply {
                if (count == 0) {
                    Log.i("Cursor", "getCursorData: ")
                } else {
                    try {
                        cursor.moveToFirst()
                        while (cursor.moveToNext()) {
                            val id = cursor.getLong(idColumn)
                            val title = cursor.getString(titleColumn)
                            val artist = cursor.getString(artistColumn)
                            val duration = cursor.getLong(duration)
                            val path = cursor.getString(path)
                            val albumId = cursor.getLong(albumId)
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            )

                            val artworkUri =
                                Uri.parse("content://media/external/audio/albumart/$albumId")
                            val audioItem = AudioItem(
                                id,
                                title,
                                artist,
                                "no ab",
                                duration,
                                uri,
                                artWork = artworkUri.toString()
                            )
                            audioFiles.add(audioItem)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    } finally {
                        cursor.close()
                    }
                }
            }
        }
        return audioFiles
    }
}