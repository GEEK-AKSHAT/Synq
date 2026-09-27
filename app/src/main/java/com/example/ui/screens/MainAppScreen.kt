package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.SynqPrimary
import com.example.ui.viewmodel.SynqViewModel

enum class SynqTab(val label: String, val icon: ImageVector) {
    FEED("Feed", Icons.Default.DynamicFeed),
    EXPLORE("Discover", Icons.Default.AutoAwesome),
    MESSAGES("Messages", Icons.Default.ChatBubbleOutline),
    PROFILE("Profile", Icons.Default.PersonOutline)
}

@Composable
fun MainAppScreen(
    onSignOutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SynqViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(SynqTab.FEED) }

    // If on a secondary tab, Back button navigates back to Feed tab
    if (selectedTab != SynqTab.FEED) {
        BackHandler {
            selectedTab = SynqTab.FEED
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("synq_bottom_navigation")
            ) {
                SynqTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SynqPrimary,
                            selectedTextColor = SynqPrimary,
                            indicatorColor = SynqPrimary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("main_app_screen")
    ) { paddingValues ->
        val screenModifier = Modifier.padding(paddingValues)
        when (selectedTab) {
            SynqTab.FEED -> FeedScreen(
                viewModel = viewModel,
                onNavigateToMessages = { selectedTab = SynqTab.MESSAGES },
                modifier = screenModifier
            )
            SynqTab.EXPLORE -> ExploreScreen(viewModel = viewModel, modifier = screenModifier)
            SynqTab.MESSAGES -> ChatScreen(viewModel = viewModel, modifier = screenModifier)
            SynqTab.PROFILE -> ProfileScreen(
                viewModel = viewModel,
                onSignOutSuccess = onSignOutSuccess,
                modifier = screenModifier
            )
        }
    }
}
