package com.example.speedycontrol.domain.model

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeviceModel(
    val name: String?,
    val address: String,
) : Parcelable