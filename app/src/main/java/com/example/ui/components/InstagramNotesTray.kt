package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserNote
import com.example.data.model.UserProfile
import com.example.ui.theme.GlassEffects
import com.example.ui.theme.SynqPrimary
import com.example.ui.theme.SynqSecondary
import com.example.ui.theme.liquidGlass

/**
 * Instagram-Style Notes Tray displaying avatars with floating thought bubbles.
 */
@Composable
fun InstagramNotesTray(
    currentUser: UserProfile?,
    notes: List<UserNote>,
    onAddOrEditNote: (text: String, emoji: String, song: String) -> Unit,
    onDeleteNote: () -> Unit,
    onReplyToNote: (note: UserNote, replyText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNoteComposer by remember { mutableStateOf(false) }
    var selectedNoteForReply by remember { mutableStateOf<UserNote?>(null) }

    val myNote = notes.firstOrNull { it.userId == currentUser?.userId }
    val friendNotes = notes.filter { it.userId != currentUser?.userId }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Current User Note / Add Note Item
            item(key = "my_note") {
                MyNoteItem(
                    currentUser = currentUser,
                    currentNote = myNote,
                    onClick = { showNoteComposer = true }
                )
            }

            // Friend Notes
            items(friendNotes, key = { it.id }) { note ->
                FriendNoteItem(
                    note = note,
                    onClick = { selectedNoteForReply = note }
                )
            }
        }
    }

    // Composer Dialog
    if (showNoteComposer) {
        NoteComposerDialog(
            existingNote = myNote,
            onDismiss = { showNoteComposer = false },
            onSave = { text, emoji, song ->
                onAddOrEditNote(text, emoji, song)
                showNoteComposer = false
            },
            onDelete = {
                onDeleteNote()
                showNoteComposer = false
            }
        )
    }

    // Quick Reply to Note Dialog
    selectedNoteForReply?.let { note ->
        NoteReplyDialog(
            note = note,
            onDismiss = { selectedNoteForReply = null },
            onSendReply = { reply ->
                onReplyToNote(note, reply)
                selectedNoteForReply = null
            }
        )
    }
}

@Composable
private fun MyNoteItem(
    currentUser: UserProfile?,
    currentNote: UserNote?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clickable { onClick() }
            .testTag("my_note_item")
    ) {
        // Thought bubble or prompt
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth()
        ) {
            if (currentNote != null && currentNote.text.isNotBlank()) {
                ThoughtBubble(
                    text = currentNote.text,
                    emoji = currentNote.moodEmoji,
                    isMine = true
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Share thought…",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar with optional '+' badge
        Box(contentAlignment = Alignment.BottomEnd) {
            SynqAvatar(
                name = currentUser?.displayName ?: "Me",
                avatarUrl = currentUser?.avatarUrl,
                sizeDp = 56
            )
            if (currentNote == null || currentNote.text.isBlank()) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(SynqPrimary)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your note",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FriendNoteItem(
    note: UserNote,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(82.dp)
            .clickable { onClick() }
            .testTag("friend_note_${note.id}")
    ) {
        // Floating Thought Bubble
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth()
        ) {
            ThoughtBubble(
                text = note.text,
                emoji = note.moodEmoji,
                isMine = false
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar
        SynqAvatar(
            name = note.userName,
            avatarUrl = note.userAvatarUrl,
            sizeDp = 56
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = note.userName.split(" ").firstOrNull() ?: note.userName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThoughtBubble(
    text: String,
    emoji: String,
    isMine: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    brush = if (isMine) GlassEffects.LiquidNeonGradient else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (emoji.isNotBlank()) {
                    Text(text = emoji, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = text,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tiny circles for thought bubble tail
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun NoteComposerDialog(
    existingNote: UserNote?,
    onDismiss: () -> Unit,
    onSave: (text: String, emoji: String, song: String) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember { mutableStateOf(existingNote?.text ?: "") }
    var selectedEmoji by remember { mutableStateOf(existingNote?.moodEmoji ?: "💭") }
    var musicTrack by remember { mutableStateOf(existingNote?.musicTrack ?: "") }

    val emojis = listOf("💭", "🎧", "🔥", "⚡", "☕", "💡", "🚀", "✨", "🎨", "💻")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(24.dp))
                .testTag("note_composer_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingNote != null) "Edit Note" else "New Note",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (existingNote != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Share a 60-character thought. Visible at the top of your network's feed & messages.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji Mood Selector
                Text(
                    text = "Mood:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) SynqPrimary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SynqPrimary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedEmoji = emoji }
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 60) text = it },
                    label = { Text("What's on your mind?") },
                    placeholder = { Text("Listening to synthwave... 🎧") },
                    supportingText = { Text("${text.length}/60") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_text_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = musicTrack,
                    onValueChange = { if (it.length <= 50) musicTrack = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Music",
                            tint = SynqSecondary
                        )
                    },
                    label = { Text("Song (Optional)") },
                    placeholder = { Text("Track title & artist") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSave(text.trim(), selectedEmoji, musicTrack.trim())
                            }
                        },
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SynqPrimary),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Share Note")
                    }
                }
            }
        }
    }
}

@Composable
fun NoteReplyDialog(
    note: UserNote,
    onDismiss: () -> Unit,
    onSendReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SynqAvatar(name = note.userName, avatarUrl = note.userAvatarUrl, sizeDp = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = note.userName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = note.moodEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = note.text,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Send reply to ${note.userName}...") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendReply(replyText.trim())
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SynqPrimary)
                    ) {
                        Text("Send in DM")
                    }
                }
            }
        }
    }
}
