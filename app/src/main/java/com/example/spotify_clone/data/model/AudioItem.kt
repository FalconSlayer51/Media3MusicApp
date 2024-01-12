package com.example.spotify_clone.data.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val path: Uri?,
    val artWork: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeLong(duration)
        parcel.writeParcelable(path, flags)
        parcel.writeString(artWork)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioItem> {
        override fun createFromParcel(parcel: Parcel): AudioItem {
            return AudioItem(parcel)
        }

        override fun newArray(size: Int): Array<AudioItem?> {
            return arrayOfNulls(size)
        }
    }
}
