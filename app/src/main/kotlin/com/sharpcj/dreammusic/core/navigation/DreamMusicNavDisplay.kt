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
import com.sharpcj.dreammusic.core.permissions.NotificationPermissionEffect
import com.sharpcj.dreammusic.feature.discover.DiscoverScreen
import com.sharpcj.dreammusic.feature.favorite.FavoriteSongsScreen
import com.sharpcj.dreammusic.feature.library.LibraryGroupDetailScreen
import com.sharpcj.dreammusic.feature.library.LibraryGroupMode
import com.sharpcj.dreammusic.feature.library.LibraryScreen
import com.sharpcj.dreammusic.feature.library.LibraryViewModel
import com.sharpcj.dreammusic.feature.player.MiniPlayer
import com.sharpcj.dreammusic.feature.player.PlayerScreen
import com.sharpcj.dreammusic.feature.player.PlayerViewModel
import com.sharpcj.dreammusic.feature.recent.RecentPlaysScreen
import com.sharpcj.dreammusic.feature.search.SearchScreen
import com.sharpcj.dreammusic.feature.settings.SettingsScreen

@Composable
fun DreamMusicApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
) {
    val backStack = remember { mutableStateListOf<DreamMusicNavKey>(DreamMusicNavKey.Library) }
    val currentDestination = backStack.lastOrNull() ?: DreamMusicNavKey.Library
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val openPlayer = {
        if (backStack.lastOrNull() !is DreamMusicNavKey.Player) {
            backStack.add(DreamMusicNavKey.Player)
        }
    }
    val openRecentPlays = {
        if (backStack.lastOrNull() !is DreamMusicNavKey.RecentPlays) {
            backStack.add(DreamMusicNavKey.RecentPlays)
        }
    }
    val openFavoriteSongs = {
        if (backStack.lastOrNull() !is DreamMusicNavKey.FavoriteSongs) {
            backStack.add(DreamMusicNavKey.FavoriteSongs)
        }
    }

    NotificationPermissionEffect()

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
                entry<DreamMusicNavKey.Library> {
                    LibraryScreen(
                        onOpenPlayer = openPlayer,
                        onOpenRecentPlays = openRecentPlays,
                        onOpenFavoriteSongs = openFavoriteSongs,
                        onOpenGroup = { groupMode, groupTitle ->
                            backStack.add(
                                DreamMusicNavKey.LibraryGroupDetail(
                                    groupModeName = groupMode.name,
                                    groupTitle = groupTitle,
                                ),
                            )
                        },
                        viewModel = libraryViewModel,
                    )
                }
                entry<DreamMusicNavKey.Discover> { DiscoverScreen() }
                entry<DreamMusicNavKey.Search> { SearchScreen(onOpenPlayer = openPlayer) }
                entry<DreamMusicNavKey.Settings> { SettingsScreen() }
                entry<DreamMusicNavKey.RecentPlays> {
                    RecentPlaysScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onOpenPlayer = openPlayer,
                    )
                }
                entry<DreamMusicNavKey.FavoriteSongs> {
                    FavoriteSongsScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onOpenPlayer = openPlayer,
                    )
                }
                entry<DreamMusicNavKey.LibraryGroupDetail> { key ->
                    LibraryGroupDetailScreen(
                        groupMode = LibraryGroupMode.valueOf(key.groupModeName),
                        groupTitle = key.groupTitle,
                        onBack = { backStack.removeLastOrNull() },
                        onOpenPlayer = openPlayer,
                        viewModel = libraryViewModel,
                    )
                }
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
