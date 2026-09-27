package com.example.ui.screens

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.auth.GoogleSignInButton
import com.example.ui.components.SynqLogoMark
import com.example.ui.components.SynqWordmark
import com.example.ui.theme.SynqDarkBackground
import com.example.ui.theme.SynqPrimary
import com.example.ui.theme.SynqSecondary
import com.example.ui.theme.SynqTertiary
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.launch

data class FeatureSlide(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val tagline: String
)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            SynqDarkBackground,
            Color(0xFF131122),
            Color(0xFF1C1733)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Brand Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(listOf(SynqPrimary, SynqSecondary, SynqTertiary))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SynqLogoMark(size = 50.dp, tint = Color.White, isDark = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                SynqWordmark(fontSize = 42.sp, color = Color.White)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Connect. Share. Intelligently Curated.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            // Features Sliding Carousel (One by One)
            val features = listOf(
                FeatureSlide(
                    icon = Icons.Default.DynamicFeed,
                    iconTint = SynqPrimary,
                    title = "Liquid Social Feed",
                    tagline = "Dynamic media posts, double-tap hearts, and frosted glass aesthetics."
                ),
                FeatureSlide(
                    icon = Icons.Default.ChatBubbleOutline,
                    iconTint = SynqSecondary,
                    title = "Instagram-Style Notes",
                    tagline = "Share 60-character thoughts and music moods floating above your inbox."
                ),
                FeatureSlide(
                    icon = Icons.Default.MotionPhotosOn,
                    iconTint = SynqTertiary,
                    title = "24-Hour Stories",
                    tagline = "Share ephemeral moments with vibrant gradients and timed progress playback."
                ),
                FeatureSlide(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFFFD700),
                    title = "Gemini AI Discovery",
                    tagline = "Intelligent topic clustering and personalized recommendation insights."
                ),
                FeatureSlide(
                    icon = Icons.Default.Send,
                    iconTint = Color(0xFF00CEC9),
                    title = "Instant Direct DMs",
                    tagline = "Fast real-time private conversations with friends and creators."
                )
            )

            var currentSlideIndex by remember { mutableStateOf(0) }

            // Auto-advance sliding timer
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(3500)
                    currentSlideIndex = (currentSlideIndex + 1) % features.size
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(24.dp))
                    .testTag("feature_slider_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val slide = features[currentSlideIndex]

                    androidx.compose.animation.AnimatedContent(
                        targetState = slide,
                        transitionSpec = {
                            androidx.compose.animation.slideInHorizontally { width -> width } + androidx.compose.animation.fadeIn() togetherWith
                                    androidx.compose.animation.slideOutHorizontally { width -> -width } + androidx.compose.animation.fadeOut()
                        },
                        label = "FeatureSlideTransition"
                    ) { currentFeature ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(currentFeature.iconTint.copy(alpha = 0.18f))
                                    .border(1.dp, currentFeature.iconTint.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentFeature.icon,
                                    contentDescription = currentFeature.title,
                                    tint = currentFeature.iconTint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentFeature.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = currentFeature.tagline,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        features.indices.forEach { index ->
                            val isSelected = index == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(if (isSelected) 20.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) SynqPrimary else Color.White.copy(alpha = 0.25f)
                                    )
                                    .clickable { currentSlideIndex = index }
                            )
                        }
                    }
                }
            }

            // Google Sign-In Call To Action
            val context = androidx.compose.ui.platform.LocalContext.current
            var currentClientId by remember { mutableStateOf(AuthManager.getEffectiveClientId(context)) }
            var showClientIdInput by remember { mutableStateOf(currentClientId.isBlank()) }
            var inputClientId by remember { mutableStateOf(currentClientId) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showClientIdInput) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("client_id_config_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Firebase Web Client ID",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To enable Google Sign-In, enter your Firebase Web Client ID (from Firebase Console > Authentication > Sign-in method > Google > Web SDK config):",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedTextField(
                                value = inputClientId,
                                onValueChange = { inputClientId = it },
                                placeholder = { Text("e.g. 52451249939-your_oauth_client_id", fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_id_text_field")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        if (inputClientId.isNotBlank()) {
                                            AuthManager.setCustomClientId(context, inputClientId)
                                            currentClientId = inputClientId.trim()
                                            showClientIdInput = false
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Web Client ID configured successfully!")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = SynqPrimary
                                    ),
                                    modifier = Modifier.testTag("save_client_id_button")
                                ) {
                                    Text("Save Web Client ID", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                GoogleSignInButton(
                    onAuthSuccess = onAuthSuccess,
                    onAuthError = { errorMsg ->
                        if (currentClientId.isBlank()) {
                            showClientIdInput = true
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    }
                )

                if (!showClientIdInput) {
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { showClientIdInput = true }
                    ) {
                        Text(
                            text = "Configure Web Client ID",
                            fontSize = 11.sp,
                            color = SynqTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By signing in, you connect securely via Firebase Authentication.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 15.sp
            )
        }
    }
}
