package com.example.islamiccorpus.ui.shell

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islamiccorpus.ui.screens.bookmarks.BookmarksScreen
import com.example.islamiccorpus.ui.screens.home.HomeScreen
import com.example.islamiccorpus.ui.screens.library.LibraryScreen
import com.example.islamiccorpus.ui.screens.quran.ContinueReadingState
import com.example.islamiccorpus.ui.screens.quran.QuranScreen

@Composable
fun MainShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var continueReadingJumpState by remember { mutableStateOf<ContinueReadingState?>(null) }
    val destinations = listOf(
        ShellDestination.Home,
        ShellDestination.Library,
        ShellDestination.Bookmarks,
        ShellDestination.Quran
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (destination) {
                                    ShellDestination.Home -> Icons.Filled.Home
                                    ShellDestination.Library -> Icons.Filled.Folder
                                    ShellDestination.Bookmarks -> Icons.Filled.Bookmark
                                    ShellDestination.Quran -> Icons.Filled.AutoStories
                                },
                                contentDescription = destination.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(text = destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShellDestination.Home.route,
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing))
            }
        ) {
            composable(route = ShellDestination.Home.route) {
                HomeScreen(
                    contentPadding = innerPadding,
                    onContinueReadingClick = { state ->
                        continueReadingJumpState = state
                        navController.navigate(ShellDestination.Quran.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(route = ShellDestination.Library.route) {
                LibraryScreen(contentPadding = innerPadding)
            }
            composable(route = ShellDestination.Bookmarks.route) {
                BookmarksScreen(contentPadding = innerPadding)
            }
            composable(route = ShellDestination.Quran.route) {
                QuranScreen(
                    contentPadding = innerPadding,
                    continueReadingOverride = continueReadingJumpState,
                    onContinueReadingConsumed = { continueReadingJumpState = null }
                )
            }
        }
    }
}

private sealed class ShellDestination(
    val route: String,
    val label: String
) {
    data object Home : ShellDestination(route = "home", label = "Home")
    data object Library : ShellDestination(route = "library", label = "Library")
    data object Bookmarks : ShellDestination(route = "bookmarks", label = "Bookmarks")
    data object Quran : ShellDestination(route = "quran", label = "Quran")
}
