package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiRecommendationBanner
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.CreatePostDialog
import com.example.ui.components.CreateStoryDialog
import com.example.ui.components.InstagramNotesTray
import com.example.ui.components.InstagramPostCard
import com.example.ui.components.StoryCarousel
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.SynqBrandHeader
import com.example.ui.theme.SynqPrimary
import com.example.ui.theme.SynqSecondary
import com.example.ui.theme.liquidGlass
import com.example.ui.viewmodel.SynqViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: SynqViewModel,
    onNavigateToMessages: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val aiInsight by viewModel.aiInsight.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()
    val selectedTopic by viewModel.selectedTopicFilter.collectAsState()
    val activeCommentsPost by viewModel.activeCommentsPost.collectAsState()
    val activeComments by viewModel.activePostComments.collectAsState()
    val activeStory by viewModel.activeViewingStory.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SynqBrandHeader(
                        logoSize = 30.dp,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showCreateStoryDialog = true },
                        modifier = Modifier.testTag("open_create_story_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MotionPhotosOn,
                            contentDescription = "New Story",
                            tint = SynqSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToMessages,
                        modifier = Modifier.testTag("open_messages_action")
                    ) {
                        if (conversations.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = SynqSecondary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${conversations.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Direct Messages",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Direct Messages",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                modifier = Modifier.liquidGlass(shape = RoundedCornerShape(0.dp), elevation = 2.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = SynqPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Create Post"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize().testTag("feed_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Instagram-Style Notes Tray
            item(key = "notes_tray") {
                InstagramNotesTray(
                    currentUser = userProfile,
                    notes = notes,
                    onAddOrEditNote = { text, emoji, song ->
                        viewModel.postUserNote(text, emoji, song) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Note updated! ✨")
                            }
                        }
                    },
                    onDeleteNote = {
                        viewModel.removeUserNote {
                            scope.launch {
                                snackbarHostState.showSnackbar("Note deleted.")
                            }
                        }
                    },
                    onReplyToNote = { note, replyText ->
                        viewModel.startNewDirectChat(note.userName, note.userId)
                        viewModel.sendMessage(replyText)
                        scope.launch {
                            snackbarHostState.showSnackbar("Reply sent to ${note.userName} in DM!")
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Stories Row (with Instagram-style gradient rings)
            item(key = "stories_carousel") {
                StoryCarousel(
                    currentUserProfile = userProfile,
                    stories = stories,
                    onAddStoryClick = { showCreateStoryDialog = true },
                    onStoryClick = { story -> viewModel.openStory(story) }
                )
            }

            // AI Recommendation Banner
            item(key = "ai_banner") {
                AiRecommendationBanner(
                    aiInsight = aiInsight,
                    isGenerating = isGeneratingAi,
                    selectedTopic = selectedTopic,
                    onTopicSelected = { topic -> viewModel.setTopicFilter(topic) },
                    onRefreshAi = { viewModel.generateAiFeedRecommendations() }
                )
            }

            // Feed Posts in Instagram Card Style
            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = SynqPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Curating your personalized SYNQ feed...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    InstagramPostCard(
                        post = post,
                        onLikeClicked = { viewModel.toggleLike(post) },
                        onCommentsClicked = { viewModel.openComments(post) },
                        onShareClicked = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out this post on SYNQ: ${post.content}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share SYNQ Post"))
                        },
                        onDirectMessageAuthor = { authorName, authorId ->
                            if (authorId != userProfile?.userId) {
                                viewModel.startNewDirectChat(authorName, authorId)
                                onNavigateToMessages()
                            }
                        },
                        onTopicClicked = { tag -> viewModel.setTopicFilter(tag) },
                        onSaveClicked = {
                            viewModel.toggleSavePost(post.id)
                            val isNowSaved = !savedPostIds.contains(post.id)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (isNowSaved) "Post saved to your collection" else "Post removed from saved")
                            }
                        },
                        isSaved = savedPostIds.contains(post.id),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Active Comments Sheet
        activeCommentsPost?.let { post ->
            CommentsBottomSheet(
                post = post,
                comments = activeComments,
                onAddComment = { text -> viewModel.addComment(text) },
                onDismiss = { viewModel.closeComments() }
            )
        }

        // Fullscreen Story Viewer
        activeStory?.let { story ->
            StoryViewerDialog(
                story = story,
                onDismiss = { viewModel.closeStory() }
            )
        }

        // Create Post Dialog with Image Posting Service
        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onSubmit = { content, imageUrl, tags ->
                    if (!imageUrl.isNullOrBlank()) {
                        viewModel.createImagePost(content, imageUrl, tags) { success ->
                            showCreatePostDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Photo post published to SYNQ!" else "Could not publish post."
                                )
                            }
                        }
                    } else {
                        viewModel.createPost(content, tags) { success ->
                            showCreatePostDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Post published to SYNQ!" else "Could not publish post."
                                )
                            }
                        }
                    }
                }
            )
        }

        // Create Story Dialog
        if (showCreateStoryDialog) {
            CreateStoryDialog(
                onDismiss = { showCreateStoryDialog = false },
                onSubmit = { caption, bgHex ->
                    viewModel.createStory(caption, bgHex) { success ->
                        showCreateStoryDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Story shared!" else "Could not share story."
                            )
                        }
                    }
                }
            )
        }
    }
}

