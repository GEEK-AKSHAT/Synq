package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.PostLike
import com.example.data.model.Story
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SynqRepository(context: Context) {

    private val databaseId = context.getString(R.string.firestore_database_id)
    private val db = FirebaseFirestore.getInstance(databaseId)
    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(null)
                }
        )
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val snapshot = db.collection("users").document(userId).get().await()
        return if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
    }

    suspend fun getOrCreateUserProfile(uid: String, displayName: String, email: String, photoUrl: String?): UserProfile {
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(UserProfile::class.java) ?: UserProfile(userId = uid, displayName = displayName)
        }

        val username = displayName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }.take(15)
            .ifEmpty { "user_${uid.take(5)}" }

        val newProfile = UserProfile(
            userId = uid,
            username = username,
            displayName = displayName.ifEmpty { "SYNQ Member" },
            bio = "Exploring the next generation of social with SYNQ ✨",
            avatarUrl = photoUrl ?: "",
            interests = listOf("Tech", "AI", "Design", "Mobile", "Innovation"),
            createdAt = null
        )

        val payload = newProfile.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
        return newProfile
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>(
            "username" to profile.username,
            "displayName" to profile.displayName,
            "bio" to profile.bio,
            "avatarUrl" to profile.avatarUrl,
            "interests" to profile.interests,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(Exception(json))
        }
    }

    // --- Posts ---

    fun observePosts(): Flow<List<Post>> = flow {
        val path = "posts"
        val uid = currentUserIdOrNull()
        emitAll(
            db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Post::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun createPost(content: String, tags: List<String>, mediaUrl: String = "", mediaType: String = "text"): Result<String> {
        val uid = requireUserId()
        val user = auth.currentUser
        val postId = UUID.randomUUID().toString()
        val postRef = db.collection("posts").document(postId)

        val post = Post(
            id = postId,
            authorId = uid,
            authorName = user?.displayName ?: "SYNQ Creator",
            authorUsername = user?.displayName?.lowercase()?.replace(" ", "_")?.take(15) ?: "creator",
            authorAvatarUrl = user?.photoUrl?.toString() ?: "",
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            tags = tags,
            likesCount = 0,
            commentsCount = 0
        )

        val payload = post.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }

        return try {
            postRef.set(payload).await()
            Result.success(postId)
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.CREATE, postRef.path)
            Result.failure(Exception(json))
        }
    }

    suspend fun toggleLikePost(postId: String, currentlyLiked: Boolean): Result<Boolean> {
        val uid = requireUserId()
        val likeRef = db.collection("posts").document(postId).collection("likes").document(uid)
        val postRef = db.collection("posts").document(postId)

        return try {
            if (currentlyLiked) {
                likeRef.delete().await()
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(false)
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "postId" to postId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                likeRef.set(payload).await()
                postRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.WRITE, likeRef.path)
            Result.failure(Exception(json))
        }
    }

    fun observeUserLikes(userId: String): Flow<Set<String>> = callbackFlow {
        // Collect liked post IDs for this user
        val listener = db.collectionGroup("likes")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val postIds = snapshot?.documents?.mapNotNull { it.getString("postId") }?.toSet() ?: emptySet()
                trySend(postIds)
            }
        awaitClose { listener.remove() }
    }

    // --- Comments ---

    fun observeComments(postId: String): Flow<List<PostComment>> = flow {
        val path = "posts/$postId/comments"
        emitAll(
            db.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PostComment::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun addComment(postId: String, content: String): Result<String> {
        val uid = requireUserId()
        val user = auth.currentUser
        val commentId = UUID.randomUUID().toString()
        val commentRef = db.collection("posts").document(postId).collection("comments").document(commentId)
        val postRef = db.collection("posts").document(postId)

        val comment = PostComment(
            id = commentId,
            postId = postId,
            authorId = uid,
            authorName = user?.displayName ?: "SYNQ Member",
            authorAvatarUrl = user?.photoUrl?.toString() ?: "",
            content = content
        )

        val payload = comment.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
        }

        return try {
            commentRef.set(payload).await()
            postRef.update(
                mapOf(
                    "commentsCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(commentId)
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(Exception(json))
        }
    }

    // --- Stories ---

    fun observeStories(): Flow<List<Story>> = flow {
        val path = "stories"
        emitAll(
            db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Story::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun createStory(caption: String, bgHex: String, mediaUrl: String = ""): Result<String> {
        val uid = requireUserId()
        val user = auth.currentUser
        val storyId = UUID.randomUUID().toString()
        val storyRef = db.collection("stories").document(storyId)

        val story = Story(
            id = storyId,
            authorId = uid,
            authorName = user?.displayName ?: "SYNQ Creator",
            authorAvatarUrl = user?.photoUrl?.toString() ?: "",
            caption = caption,
            mediaUrl = mediaUrl,
            backgroundColor = bgHex
        )

        val payload = story.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
        }

        return try {
            storyRef.set(payload).await()
            Result.success(storyId)
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.CREATE, storyRef.path)
            Result.failure(Exception(json))
        }
    }

    // --- Conversations & Messages ---

    fun observeConversations(): Flow<List<Conversation>> = flow {
        val uid = requireUserId()
        val path = "conversations"
        emitAll(
            db.collection("conversations")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Conversation::class.java).sortedByDescending { it.lastUpdatedAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations").document(conversationId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Message::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun sendMessage(conversationId: String, text: String, otherParticipantId: String): Result<String> {
        val uid = requireUserId()
        val user = auth.currentUser
        val convRef = db.collection("conversations").document(conversationId)
        val messageId = UUID.randomUUID().toString()
        val messageRef = convRef.collection("messages").document(messageId)

        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = uid,
            senderName = user?.displayName ?: "Me",
            text = text
        )

        val msgPayload = message.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
        }

        return try {
            val convDoc = convRef.get().await()
            if (!convDoc.exists()) {
                val conv = Conversation(
                    id = conversationId,
                    participantIds = listOf(uid, otherParticipantId),
                    lastMessage = text,
                    lastSenderId = uid
                )
                val convPayload = conv.toMap().toMutableMap().apply {
                    put("lastUpdatedAt", FieldValue.serverTimestamp())
                }
                convRef.set(convPayload).await()
            } else {
                convRef.update(
                    mapOf(
                        "lastMessage" to text,
                        "lastSenderId" to uid,
                        "lastUpdatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            messageRef.set(msgPayload).await()
            Result.success(messageId)
        } catch (e: Exception) {
            val json = handleFirestoreError(e, OperationType.WRITE, messageRef.path)
            Result.failure(Exception(json))
        }
    }

    // --- Initial Seed Starter Content ---
    suspend fun seedCommunityContentIfEmpty() {
        try {
            val existing = db.collection("posts").limit(1).get().await()
            if (!existing.isEmpty) return

            val currentUid = auth.currentUser?.uid ?: return
            val currentName = auth.currentUser?.displayName ?: "SYNQ Creator"

            val initialPosts = listOf(
                Post(
                    id = "seed_post_1",
                    authorId = currentUid,
                    authorName = "SYNQ Community",
                    authorUsername = "synq_official",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                    content = "Welcome to SYNQ! 🚀 Experience the next era of intelligent social connection. Your feed adapts to what sparks your curiosity with real-time AI personalization.",
                    tags = listOf("SYNQ", "Community", "AI", "NextGen"),
                    likesCount = 14,
                    commentsCount = 3
                ),
                Post(
                    id = "seed_post_2",
                    authorId = currentUid,
                    authorName = "Elena Vance",
                    authorUsername = "elena_design",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    content = "Exploring generative aesthetics and micro-interactions in Jetpack Compose today. The fluid animation physics make the UI feel alive! ✨🎨",
                    tags = listOf("Design", "Tech", "Creative"),
                    likesCount = 28,
                    commentsCount = 7
                ),
                Post(
                    id = "seed_post_3",
                    authorId = currentUid,
                    authorName = "Marcus Ray",
                    authorUsername = "mray_ai",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    content = "Just deployed our local model pipeline. Blown away by how fast inference has gotten on edge hardware. Who else is building on-device AI tools? 🤖💡",
                    tags = listOf("AI", "Tech", "Innovation"),
                    likesCount = 42,
                    commentsCount = 12
                )
            )

            for (p in initialPosts) {
                val payload = p.toMap().toMutableMap().apply {
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                db.collection("posts").document(p.id).set(payload).await()
            }

            val initialStories = listOf(
                Story(
                    id = "seed_story_1",
                    authorId = currentUid,
                    authorName = "SYNQ Official",
                    caption = "Welcome to the feed! Tap to interact ✨",
                    backgroundColor = "#6C47FF"
                ),
                Story(
                    id = "seed_story_2",
                    authorId = currentUid,
                    authorName = "Elena Vance",
                    caption = "Midnight prototyping session 💻☕",
                    backgroundColor = "#FF3366"
                ),
                Story(
                    id = "seed_story_3",
                    authorId = currentUid,
                    authorName = "Marcus Ray",
                    caption = "New AI benchmarks look wild ⚡",
                    backgroundColor = "#00E5FF"
                )
            )

            for (s in initialStories) {
                val payload = s.toMap().toMutableMap().apply {
                    put("createdAt", FieldValue.serverTimestamp())
                }
                db.collection("stories").document(s.id).set(payload).await()
            }

            // Seed initial note if none exists
            val initialNote = UserNote(
                id = "seed_note_1",
                userId = currentUid,
                userName = "SYNQ Official",
                userAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                text = "Liquid glass update is live! ✨🎧",
                moodEmoji = "🔥",
                musicTrack = "Midnight City - M83"
            )
            val notePayload = initialNote.toMap().toMutableMap().apply {
                put("createdAt", FieldValue.serverTimestamp())
            }
            db.collection("notes").document(initialNote.id).set(notePayload).await()
        } catch (e: Exception) {
            Log.w("SynqRepository", "Could not seed starter data: ${e.message}")
        }
    }

    // --- Instagram-Style Notes ---

    fun observeNotes(): Flow<List<UserNote>> = callbackFlow {
        val path = "notes"
        val subscription = db.collection("notes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { it.toObject(UserNote::class.java) } ?: emptyList()
                trySend(notes)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createOrUpdateNote(text: String, emoji: String = "💭", musicTrack: String = "") {
        val uid = requireUserId()
        val profile = getUserProfile(uid)
        val noteId = "note_$uid"
        val payload = mutableMapOf<String, Any>(
            "id" to noteId,
            "userId" to uid,
            "userName" to (profile?.displayName ?: "Me"),
            "userAvatarUrl" to (profile?.avatarUrl ?: ""),
            "text" to text.take(60),
            "moodEmoji" to emoji,
            "musicTrack" to musicTrack.take(60),
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("notes").document(noteId).set(payload).await()
    }

    suspend fun deleteNote() {
        val uid = requireUserId()
        db.collection("notes").document("note_$uid").delete().await()
    }

    // --- Profile Customization ---

    suspend fun updateCustomProfile(
        displayName: String,
        username: String,
        bio: String,
        pronouns: String,
        website: String,
        bannerGradient: String,
        avatarUrl: String? = null
    ) {
        val uid = requireUserId()
        val updates = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "username" to username,
            "bio" to bio,
            "pronouns" to pronouns,
            "website" to website,
            "bannerGradient" to bannerGradient,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (avatarUrl != null && avatarUrl.isNotBlank()) {
            updates["avatarUrl"] = avatarUrl
        }
        db.collection("users").document(uid).update(updates).await()
    }

    // --- Rich Image Post Creation ---

    suspend fun createImagePost(
        content: String,
        imageUrl: String,
        tags: List<String>
    ) {
        val uid = requireUserId()
        val profile = getUserProfile(uid)
        val postId = "post_${UUID.randomUUID().toString().take(12)}"
        val post = Post(
            id = postId,
            authorId = uid,
            authorName = profile?.displayName ?: "SYNQ Creator",
            authorUsername = profile?.username ?: "creator",
            authorAvatarUrl = profile?.avatarUrl ?: "",
            content = content,
            mediaUrl = imageUrl,
            mediaType = "image",
            tags = tags,
            likesCount = 0,
            commentsCount = 0
        )
        val payload = post.toMap().toMutableMap().apply {
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        db.collection("posts").document(postId).set(payload).await()
    }
}
