package com.example.market.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ChatRoom(
    @DocumentId val documentId: String = "",
    val productId: String = "",
    val participants: List<String> = listOf(),
    val names: HashMap<String, String> = hashMapOf(),

    val lastMessageUserId: String = "",
    val lastMessageUserName: String = "",
    val lastMessageContent: String = "",
    val lastMessageTimestamp: Date = Date(),
) : Parcelable