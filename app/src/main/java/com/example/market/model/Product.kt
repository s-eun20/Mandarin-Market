package com.example.market.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Date


@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class Product(
    @DocumentId val documentId: String = "",
    val userId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("price") val price: String = "",
    @SerializedName("image") val imageUrl: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("sell") val sell: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("timestamp") val timestamp: Date = Date(),
) : Parcelable