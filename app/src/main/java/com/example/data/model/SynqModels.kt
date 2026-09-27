package com.example.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val interests: List<String> = emptyList(),
    val pronouns: String = "",
    val website: String = "",
    val bannerGradient: String = "linear-gradient(135deg, #6C5CE7, #FD79A8)",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "username" to username,
            "displayName" to displayName,
            "bio" to bio,
            "avatarUrl" to avatarUrl,
            "interests" to interests,
            "pronouns" to pronouns,
            "website" to website,
            "bannerGradient" to bannerGradient
        )
        createdAt?.let { map["createdAt"] = it }
        updatedAt?.let { map["updatedAt"] = it }
        return map
    }
}

data class UserNote(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val text: String = "",
    val moodEmoji: String = "💭",
    val musicTrack: String = "",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "userAvatarUrl" to userAvatarUrl,
            "text" to text,
            "moodEmoji" to moodEmoji,
            "musicTrack" to musicTrack
        )
        createdAt?.let { map["createdAt"] = it }
        return map
    }
}

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "text",
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    // UI-only transient fields:
    val isLikedByCurrentUser: Boolean = false,
    val isSavedByCurrentUser: Boolean = false,
    val aiRecommendationReason: String? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "authorId" to authorId,
            "authorName" to authorName,
            "authorUsername" to authorUsername,
            "authorAvatarUrl" to authorAvatarUrl,
            "content" to content,
            "mediaUrl" to mediaUrl,
            "mediaType" to mediaType,
            "tags" to tags,
            "likesCount" to likesCount,
            "commentsCount" to commentsCount
        )
        createdAt?.let { map["createdAt"] = it }
        updatedAt?.let { map["updatedAt"] = it }
        return map
    }
}

data class PostLike(
    val userId: String = "",
    val postId: String = "",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "postId" to postId
        )
        createdAt?.let { map["createdAt"] = it }
        return map
    }
}

data class PostComment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "postId" to postId,
            "authorId" to authorId,
            "authorName" to authorName,
            "authorAvatarUrl" to authorAvatarUrl,
            "content" to content
        )
        createdAt?.let { map["createdAt"] = it }
        return map
    }
}

data class Story(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val caption: String = "",
    val mediaUrl: String = "",
    val backgroundColor: String = "#6C47FF",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "authorId" to authorId,
            "authorName" to authorName,
            "authorAvatarUrl" to authorAvatarUrl,
            "caption" to caption,
            "mediaUrl" to mediaUrl,
            "backgroundColor" to backgroundColor
        )
        createdAt?.let { map["createdAt"] = it }
        return map
    }
}

data class Conversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val lastUpdatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "participantIds" to participantIds,
            "lastMessage" to lastMessage,
            "lastSenderId" to lastSenderId
        )
        lastUpdatedAt?.let { map["lastUpdatedAt"] = it }
        return map
    }
}

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text
        )
        createdAt?.let { map["createdAt"] = it }
        return map
    }
}

data class AiRecommendationInsight(
    val summary: String = "",
    val recommendedTopics: List<String> = emptyList(),
    val postReasons: Map<String, String> = emptyMap()
)
