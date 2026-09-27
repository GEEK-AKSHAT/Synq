package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.AiAccent
import com.example.ui.theme.GlassEffects
import com.example.ui.theme.SynqPrimary
import com.example.ui.theme.SynqSecondary
import com.example.ui.theme.SynqTertiary
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Instagram-style Post Card with liquid glass aesthetic, media display,
 * double-tap to like with bursting heart animation, and complete interaction bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstagramPostCard(
    post: Post,
    onLikeClicked: () -> Unit,
    onCommentsClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onDirectMessageAuthor: (authorName: String, authorId: String) -> Unit,
    onTopicClicked: (String) -> Unit,
    onSaveClicked: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showBurstingHeart by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
            .testTag("insta_post_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // AI Insight Header Banner (if applicable)
            if (!post.aiRecommendationReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(AiAccent.copy(alpha = 0.22f), SynqSecondary.copy(alpha = 0.12f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Reason",
                            tint = AiAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.aiRecommendationReason,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Author Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with colorful Story Ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(2.dp, GlassEffects.InstaStoryGradient, CircleShape)
                        .padding(2.5.dp)
                        .clickable { onDirectMessageAuthor(post.authorName, post.authorId) }
                ) {
                    SynqAvatar(
                        name = post.authorName,
                        avatarUrl = post.authorAvatarUrl,
                        sizeDp = 40
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDirectMessageAuthor(post.authorName, post.authorId) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername.ifEmpty { post.authorName.lowercase().replace(" ", "_") },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Following",
                            fontSize = 11.sp,
                            color = SynqPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = post.authorName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3-dots Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Post Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Direct Message Author") },
                            onClick = {
                                showMenu = false
                                onDirectMessageAuthor(post.authorName, post.authorId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Post") },
                            onClick = {
                                showMenu = false
                                onShareClicked()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isSaved) "Remove from Saved" else "Save Post") },
                            onClick = {
                                showMenu = false
                                onSaveClicked()
                            }
                        )
                    }
                }
            }

            // Post Media (Image) or Rich Text Body
            if (post.mediaType == "image" && post.mediaUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Instagram square aspect ratio
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (!post.isLikedByCurrentUser) {
                                        onLikeClicked()
                                    }
                                    coroutineScope.launch {
                                        showBurstingHeart = true
                                        delay(850)
                                        showBurstingHeart = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = "Post Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Bursting Animated Heart on Double Tap
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showBurstingHeart,
                        enter = scaleIn(spring(dampingRatio = 0.4f, stiffness = 400f)) + fadeIn(),
                        exit = scaleOut(tween(300)) + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Liked",
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(8.dp, CircleShape)
                        )
                    }
                }
            } else {
                // Text-based post styled with liquid card body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(14.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (!post.isLikedByCurrentUser) {
                                        onLikeClicked()
                                    }
                                }
                            )
                        }
                ) {
                    Text(
                        text = post.content,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Action Buttons Bar (Instagram layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Like Button
                IconButton(
                    onClick = onLikeClicked,
                    modifier = Modifier.testTag("like_post_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByCurrentUser) Color(0xFFFF2D55) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Comment Bubble
                IconButton(
                    onClick = onCommentsClicked,
                    modifier = Modifier.testTag("comment_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Direct Message / Paper Plane Share
                IconButton(
                    onClick = { onDirectMessageAuthor(post.authorName, post.authorId) },
                    modifier = Modifier.testTag("dm_author_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Direct Message",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bookmark / Save Post
                IconButton(
                    onClick = onSaveClicked,
                    modifier = Modifier.testTag("save_post_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Post",
                        tint = if (isSaved) SynqPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Social Proof: Likes Count
            val displayLikes = post.likesCount + if (post.isLikedByCurrentUser) 1 else 0
            if (displayLikes > 0) {
                Text(
                    text = "$displayLikes likes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Caption Section (Username + Content)
            if (post.mediaType == "image" && post.content.isNotBlank()) {
                val annotatedString = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append(post.authorUsername.ifEmpty { post.authorName.lowercase().replace(" ", "_") })
                        append(" ")
                    }
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                    ) {
                        append(post.content)
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Hashtags Flow
            if (post.tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.tags.forEach { tag ->
                        val formattedTag = if (tag.startsWith("#")) tag else "#$tag"
                        Text(
                            text = formattedTag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SynqTertiary,
                            modifier = Modifier.clickable { onTopicClicked(tag) }
                        )
                    }
                }
            }

            // Comments Count Button
            if (post.commentsCount > 0) {
                Text(
                    text = "View all ${post.commentsCount} comments",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { onCommentsClicked() }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = "Add a comment…",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { onCommentsClicked() }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
