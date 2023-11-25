package com.example.market.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val documentId: String = "",
    val userId: String = "",
    val userName: String = "",  // 이메일 주소
    val message: String = "",
    val timestamp: Date = Date(),
) {
    @get: Exclude
    @set: Exclude
    var dateVisibility = false
}
