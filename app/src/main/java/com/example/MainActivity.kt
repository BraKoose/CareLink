package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CareLinkViewModel
import com.example.ui.CareLinkViewModelFactory
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as CareLinkApp).repository
        val viewModel = ViewModelProvider(this, CareLinkViewModelFactory(repository))[CareLinkViewModel::class.java]

        setContent {
            val profile by viewModel.anonymizedProfile.collectAsState()
            val themeType = profile?.selectedTheme ?: "CLASSIC"
            val isDisguised = profile?.isDisguised ?: false

            MyApplicationTheme(themeType = themeType) {
                var currentTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CareLinkTopAppBar(isDisguised = isDisguised)
                    },
                    bottomBar = {
                        CareLinkBottomNavigation(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it },
                            isDisguised = isDisguised
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { tabIndex ->
                                    currentTab = tabIndex
                                }
                            )
                            1 -> ChatScreen(viewModel = viewModel)
                            2 -> QuizScreen(
                                viewModel = viewModel,
                                onNavigateToStore = {
                                    currentTab = 3 // Switch to Store tab
                                }
                            )
                            3 -> StoreScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareLinkTopAppBar(isDisguised: Boolean) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isDisguised) Icons.Default.Favorite else Icons.Default.HealthAndSafety,
                    contentDescription = if (isDisguised) "FlowTrack logo" else "CareLink logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (isDisguised) "FlowTrack" else "CareLink",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            // Anonymized secure indicator badge
            Row(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "End-to-End Encrypted Secure",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "SECURE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun CareLinkBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDisguised: Boolean
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text(if (isDisguised) "My Flow" else "Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = if (isDisguised) Icons.Default.DateRange else Icons.Default.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.testTag("tab_dashboard")
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text(if (isDisguised) "Symptom Coach" else "AI Educator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = if (isDisguised) Icons.Default.Email else Icons.Default.Chat,
                    contentDescription = "AI Educator"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.testTag("tab_educator")
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text(if (isDisguised) "Wellness Quiz" else "Risk Quiz", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = "Risk Quiz"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.testTag("tab_quiz")
        )

        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text(if (isDisguised) "Essentials" else "Store", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            icon = {
                Icon(
                    imageVector = if (isDisguised) Icons.Default.ShoppingCart else Icons.Default.LocalPharmacy,
                    contentDescription = "Storefront"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.testTag("tab_store")
        )
    }
}
