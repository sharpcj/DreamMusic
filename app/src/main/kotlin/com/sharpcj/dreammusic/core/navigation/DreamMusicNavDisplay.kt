package com.sharpcj.dreammusic.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sharpcj.dreammusic.feature.discover.DiscoverScreen
import com.sharpcj.dreammusic.feature.library.LibraryScreen
import com.sharpcj.dreammusic.feature.player.MiniPlayer
import com.sharpcj.dreammusic.feature.player.PlayerScreen
import com.sharpcj.dreammusic.feature.player.PlayerViewModel
import com.sharpcj.dreammusic.feature.search.SearchScreen
import com.sharpcj.dreammusic.feature.settings.SettingsScreen

@Composable
fun DreamMusicApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val backStack = remember { mutableStateListOf<DreamMusicNavKey>(DreamMusicNavKey.Library) }
    val currentDestination = backStack.lastOrNull() ?: DreamMusicNavKey.Library
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val openPlayer = {
        if (backStack.lastOrNull() !is DreamMusicNavKey.Player) {
            backStack.add(DreamMusicNavKey.Player)
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                if (currentDestination !is DreamMusicNavKey.Player) {
                    MiniPlayer(
                        uiState = playerUiState,
                        onSkipToPrevious = playerViewModel::skipToPrevious,
                        onTogglePlayPause = playerViewModel::playOrPause,
                        onSkipToNext = playerViewModel::skipToNext,
                        onOpenPlayer = openPlayer,
                    )
                }
                DreamMusicNavigationBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        if (currentDestination != destination) {
                            backStack.clear()
                            backStack.add(destination)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<DreamMusicNavKey.Library> { LibraryScreen(onOpenPlayer = openPlayer) }
                entry<DreamMusicNavKey.Discover> { DiscoverScreen() }
                entry<DreamMusicNavKey.Search> { SearchScreen() }
                entry<DreamMusicNavKey.Settings> { SettingsScreen() }
                entry<DreamMusicNavKey.Player> {
                    PlayerScreen(
                        onBack = { backStack.removeLastOrNull() },
                        viewModel = playerViewModel,
                    )
                }
            },
        )
    }
}
