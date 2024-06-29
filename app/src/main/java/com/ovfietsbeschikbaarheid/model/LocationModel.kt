package com.ovfietsbeschikbaarheid.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationOverviewModel(
    val title: String,
    val uri: String,
    val rentalBikesAvailable: Int?,
    val open: Boolean,
) : Parcelable
