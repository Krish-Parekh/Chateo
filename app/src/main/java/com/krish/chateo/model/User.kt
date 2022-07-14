package com.krish.chateo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val phoneNumber: String = "",
    val imageUrl: String = "",
    val uid: String = "",
    val status: String = "",
    val onlineStatus: Boolean = false,
    val lastActive: String = ""
):Parcelable