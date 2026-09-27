package com.example

import com.example.data.model.Post
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SynqModelTest {

    @Test
    fun testUserProfileDefaultsAndToMap() {
        val profile = UserProfile(
            userId = "user_123",
            username = "alex_synq",
            displayName = "Alex",
            bio = "Building the future of social apps",
            interests = listOf("Tech", "AI", "Design")
        )

        assertEquals("user_123", profile.userId)
        assertEquals("alex_synq", profile.username)
        assertEquals(3, profile.interests.size)

        val map = profile.toMap()
        assertEquals("user_123", map["userId"])
        assertEquals("alex_synq", map["username"])
        assertEquals("Alex", map["displayName"])
    }

    @Test
    fun testPostDefaultsAndToMap() {
        val post = Post(
            id = "post_abc",
            authorId = "user_123",
            authorName = "Alex",
            content = "Excited to share SYNQ!",
            tags = listOf("AI", "Innovation")
        )

        assertEquals("post_abc", post.id)
        assertEquals(0, post.likesCount)
        assertEquals(0, post.commentsCount)

        val map = post.toMap()
        assertEquals("post_abc", map["id"])
        assertEquals("Alex", map["authorName"])
        assertTrue((map["tags"] as List<*>).contains("AI"))
    }
}
