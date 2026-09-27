package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.ui.components.InstagramNotesTray
import com.example.ui.components.SynqAvatar
import com.example.ui.theme.SynqPrimary
import com.example.ui.theme.SynqSecondary
import com.example.ui.viewmodel.SynqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SynqViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val selectedConv by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }

    // When inside a conversation, hardware Back button returns to the conversations list
    if (selectedConv != null) {
        BackHandler {
            viewModel.closeConversation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedConv != null) {
                        val otherId = selectedConv!!.participantIds.firstOrNull { it != userProfile?.userId } ?: "Friend"
                        val title = selectedConv!!.participantNames[otherId] ?: "Chat with $otherId"
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Direct Messages",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    if (selectedConv != null) {
                        IconButton(onClick = { viewModel.closeConversation() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedConv == null) {
                FloatingActionButton(
                    onClick = { showNewChatDialog = true },
                    containerColor = SynqPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("start_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "New Message"
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("chat_screen")
    ) { paddingValues ->
        if (selectedConv == null) {
            // Conversations List View with Instagram-style Notes tray
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item(key = "dm_notes_tray") {
                    InstagramNotesTray(
                        currentUser = userProfile,
                        notes = notes,
                        onAddOrEditNote = { text, emoji, song ->
                            viewModel.postUserNote(text, emoji, song) {}
                        },
                        onDeleteNote = {
                            viewModel.removeUserNote {}
                        },
                        onReplyToNote = { note, replyText ->
                            viewModel.startNewDirectChat(note.userName, note.userId)
                            viewModel.sendMessage(replyText)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (conversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No direct messages yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap on an author in the feed or the + button to start a chat!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            currentUserId = userProfile?.userId ?: "",
                            onClick = { viewModel.openConversation(conv) }
                        )
                    }
                }
            }
        } else {
            // Direct Messages Conversation View
            DirectConversationView(
                messages = messages,
                currentUserId = userProfile?.userId ?: "",
                onSendMessage = { text -> viewModel.sendMessage(text) },
                modifier = Modifier.padding(paddingValues)
            )
        }

        // New Chat Dialog
        if (showNewChatDialog) {
            NewChatDialog(
                onDismiss = { showNewChatDialog = false },
                onStartChat = { name, id ->
                    showNewChatDialog = false
                    viewModel.startNewDirectChat(name, id)
                }
            )
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherId = conversation.participantIds.firstOrNull { it != currentUserId } ?: "Community"
    val displayName = conversation.participantNames[otherId] ?: otherId

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_${conversation.id}")
    ) {
        SynqAvatar(
            name = displayName,
            avatarUrl = null,
            sizeDp = 48
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessage.ifEmpty { "Tap to say hello..." },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DirectConversationView(
    messages: List<Message>,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUserId
                MessageBubble(message = msg, isMe = isMe)
            }
        }

        // Bottom Input Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Write a message...", fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SynqPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(SynqPrimary, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isMe) SynqPrimary else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    onStartChat: (name: String, id: String) -> Unit
) {
    var recipientName by remember { mutableStateOf("") }
    var recipientId by remember { mutableStateOf("") }

    val suggestedPeers = listOf(
        Pair("SYNQ Community Bot", "synq_official"),
        Pair("Elena Vance", "elena_design"),
        Pair("Marcus Ray", "mray_ai")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "New Direct Message",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Suggested Connections:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                suggestedPeers.forEach { (name, id) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartChat(name, id) }
                            .padding(vertical = 8.dp)
                    ) {
                        SynqAvatar(name = name, avatarUrl = null, sizeDp = 34)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
