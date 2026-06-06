package com.sharpcj.dreammusic.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sharpcj.dreammusic.feature.discover.DiscoverScreen
import com.sharpcj.dreammusic.feature.library.LibraryScreen
import com.sharpcj.dreammusic.feature.player.PlayerScreen
import com.sharpcj.dreammusic.feature.search.SearchScreen
import com.sharpcj.dreammusic.feature.settings.SettingsScreen
import androidx.compose.material3.Scaffold

@Composable
fun DreamMusicApp() {
    val backStack = remember { mutableStateListOf<DreamMusicNavKey>(DreamMusicNavKey.Library) }
    val currentDestination = backStack.lastOrNull() ?: DreamMusicNavKey.Library

    Scaffold(
        bottomBar = {
            DreamMusicNavigationBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    if (currentDestination != destination) {
                        backStack.clear()
                        backStack.add(destination)
                    }
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<DreamMusicNavKey.Library> { LibraryScreen(onOpenPlayer = { backStack.add(DreamMusicNavKey.Player) }) }
                entry<DreamMusicNavKey.Discover> { DiscoverScreen() }
                entry<DreamMusicNavKey.Search> { SearchScreen() }
                entry<DreamMusicNavKey.Settings> { SettingsScreen() }
                entry<DreamMusicNavKey.Player> { PlayerScreen(onBack = { backStack.removeLastOrNull() }) }
            },
        )
    }
}
