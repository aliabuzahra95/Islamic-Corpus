package com.example.islamiccorpus.ui.shell

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islamiccorpus.ui.screens.bookmarks.BookmarksScreen
import com.example.islamiccorpus.ui.screens.home.HomeScreen
import com.example.islamiccorpus.ui.screens.library.LibraryScreen
import com.example.islamiccorpus.ui.screens.quran.QuranScreen

@Composable
fun MainShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
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
                        icon = {},
                        label = { Text(text = destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShellDestination.Home.route
        ) {
            composable(route = ShellDestination.Home.route) {
                HomeScreen(contentPadding = innerPadding)
            }
            composable(route = ShellDestination.Library.route) {
                LibraryScreen(contentPadding = innerPadding)
            }
            composable(route = ShellDestination.Bookmarks.route) {
                BookmarksScreen(contentPadding = innerPadding)
            }
            composable(route = ShellDestination.Quran.route) {
                QuranScreen(contentPadding = innerPadding)
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
