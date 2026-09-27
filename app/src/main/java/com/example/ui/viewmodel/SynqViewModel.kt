package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiRecommendationService
import com.example.data.model.AiRecommendationInsight
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.Story
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.data.repository.SynqRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class SynqViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SynqRepository(application)
    private val geminiService = GeminiRecommendationService()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _likedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostIds: StateFlow<Set<String>> = _likedPostIds.asStateFlow()

    private val _aiInsight = MutableStateFlow(AiRecommendationInsight())
    val aiInsight: StateFlow<AiRecommendationInsight> = _aiInsight.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _selectedTopicFilter = MutableStateFlow<String?>(null)
    val selectedTopicFilter: StateFlow<String?> = _selectedTopicFilter.asStateFlow()

    private val _activeCommentsPost = MutableStateFlow<Post?>(null)
    val activeCommentsPost: StateFlow<Post?> = _activeCommentsPost.asStateFlow()

    private val _activeViewingStory = MutableStateFlow<Story?>(null)
    val activeViewingStory: StateFlow<Story?> = _activeViewingStory.asStateFlow()

    private val _selectedConversation = MutableStateFlow<Conversation?>(null)
    val selectedConversation: StateFlow<Conversation?> = _selectedConversation.asStateFlow()

    private val rawPosts = repository.observePosts()
    val stories: StateFlow<List<Story>> = repository.observeStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<Conversation>> = repository.observeConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    val notes: StateFlow<List<UserNote>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Posts decorated with Likes, Saves & AI Recommendation badges
    val posts: StateFlow<List<Post>> = combine(
        rawPosts,
        _likedPostIds,
        _savedPostIds,
        _aiInsight,
        _selectedTopicFilter
    ) { postsList, likedIds, savedIds, aiInfo, filter ->
        var list = postsList.map { post ->
            val reason = aiInfo.postReasons[post.id]
            post.copy(
                isLikedByCurrentUser = likedIds.contains(post.id),
                isSavedByCurrentUser = savedIds.contains(post.id),
                aiRecommendationReason = reason
            )
        }
        if (!filter.isNullOrBlank()) {
            val cleanFilter = filter.removePrefix("#").lowercase()
            list = list.filter { post ->
                post.tags.any { it.lowercase() == cleanFilter } ||
                post.content.contains(cleanFilter, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePostComments = MutableStateFlow<List<PostComment>>(emptyList())
    val activePostComments: StateFlow<List<PostComment>> = _activePostComments.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<Message>>(emptyList())
    val activeMessages: StateFlow<List<Message>> = _activeMessages.asStateFlow()

    init {
        initializeUserData()
    }

    fun initializeUserData() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            val profile = repository.getOrCreateUserProfile(
                uid = user.uid,
                displayName = user.displayName ?: "SYNQ Creator",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString()
            )
            _currentUserProfile.value = profile

            repository.seedCommunityContentIfEmpty()

            // Observe user's likes
            repository.observeUserLikes(user.uid).collect { ids ->
                _likedPostIds.value = ids
            }
        }

        // Trigger initial AI feed personalization
        viewModelScope.launch {
            rawPosts.collect { currentPosts ->
                if (currentPosts.isNotEmpty() && _aiInsight.value.postReasons.isEmpty()) {
                    generateAiFeedRecommendations(currentPosts)
                }
            }
        }
    }

    fun setTopicFilter(topic: String?) {
        _selectedTopicFilter.value = if (_selectedTopicFilter.value == topic) null else topic
    }

    fun generateAiFeedRecommendations(currentPosts: List<Post> = posts.value, goalPrompt: String? = null) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            val interests = _currentUserProfile.value?.interests ?: listOf("Tech", "AI", "Design")
            val insight = geminiService.getPersonalizedRecommendations(
                userInterests = interests,
                availablePosts = currentPosts,
                customGoalPrompt = goalPrompt
            )
            _aiInsight.value = insight
            _isGeneratingAi.value = false
        }
    }

    fun createPost(content: String, tags: List<String>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.createPost(content = content, tags = tags)
            onComplete(result.isSuccess)
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val currentlyLiked = _likedPostIds.value.contains(post.id)
            val newLiked = !currentlyLiked
            // Optimistic update
            _likedPostIds.value = if (newLiked) _likedPostIds.value + post.id else _likedPostIds.value - post.id
            repository.toggleLikePost(post.id, currentlyLiked)
        }
    }

    fun openComments(post: Post) {
        _activeCommentsPost.value = post
        viewModelScope.launch {
            repository.observeComments(post.id).collect { comments ->
                _activePostComments.value = comments
            }
        }
    }

    fun closeComments() {
        _activeCommentsPost.value = null
        _activePostComments.value = emptyList()
    }

    fun addComment(text: String) {
        val post = _activeCommentsPost.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(post.id, text.trim())
        }
    }

    fun createStory(caption: String, bgHex: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.createStory(caption = caption, bgHex = bgHex)
            onComplete(result.isSuccess)
        }
    }

    fun openStory(story: Story) {
        _activeViewingStory.value = story
    }

    fun closeStory() {
        _activeViewingStory.value = null
    }

    fun updateInterests(newInterests: List<String>) {
        val current = _currentUserProfile.value ?: return
        val updated = current.copy(interests = newInterests)
        _currentUserProfile.value = updated
        viewModelScope.launch {
            repository.updateUserProfile(updated)
            generateAiFeedRecommendations(posts.value)
        }
    }

    fun updateBio(newBio: String, newDisplayName: String) {
        val current = _currentUserProfile.value ?: return
        val updated = current.copy(bio = newBio, displayName = newDisplayName)
        _currentUserProfile.value = updated
        viewModelScope.launch {
            repository.updateUserProfile(updated)
        }
    }

    fun openConversation(conversation: Conversation) {
        _selectedConversation.value = conversation
        viewModelScope.launch {
            repository.observeMessages(conversation.id).collect { msgs ->
                _activeMessages.value = msgs
            }
        }
    }

    fun closeConversation() {
        _selectedConversation.value = null
        _activeMessages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val conv = _selectedConversation.value ?: return
        val currentUid = repository.currentUserIdOrNull() ?: return
        val otherParticipant = conv.participantIds.firstOrNull { it != currentUid } ?: "community_bot"
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(conv.id, text.trim(), otherParticipant)
        }
    }

    fun startNewDirectChat(recipientName: String, recipientId: String) {
        val currentUid = repository.currentUserIdOrNull() ?: return
        val convId = listOf(currentUid, recipientId).sorted().joinToString("_")
        val conv = Conversation(
            id = convId,
            participantIds = listOf(currentUid, recipientId),
            lastMessage = "Started conversation with $recipientName",
            lastSenderId = currentUid
        )
        openConversation(conv)
    }

    fun toggleSavePost(postId: String) {
        val current = _savedPostIds.value
        _savedPostIds.value = if (current.contains(postId)) current - postId else current + postId
    }

    fun postUserNote(text: String, emoji: String, song: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.createOrUpdateNote(text, emoji, song)
            onComplete()
        }
    }

    fun removeUserNote(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteNote()
            onComplete()
        }
    }

    fun updateExtendedProfile(
        displayName: String,
        username: String,
        bio: String,
        pronouns: String,
        website: String,
        bannerGradient: String,
        avatarUrl: String? = null,
        onComplete: () -> Unit
    ) {
        val current = _currentUserProfile.value ?: return
        val updated = current.copy(
            displayName = displayName,
            username = username,
            bio = bio,
            pronouns = pronouns,
            website = website,
            bannerGradient = bannerGradient,
            avatarUrl = if (!avatarUrl.isNullOrBlank()) avatarUrl else current.avatarUrl
        )
        _currentUserProfile.value = updated
        viewModelScope.launch {
            repository.updateCustomProfile(displayName, username, bio, pronouns, website, bannerGradient, avatarUrl)
            onComplete()
        }
    }

    fun createImagePost(content: String, imageUrl: String, tags: List<String>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.createImagePost(content, imageUrl, tags)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
