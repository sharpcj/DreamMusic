package com.sharpcj.dreammusic.core.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
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
import com.sharpcj.dreammusic.feature.library.LocalMusicScreen
import com.sharpcj.dreammusic.feature.player.MiniPlayer
import com.sharpcj.dreammusic.feature.player.PlayerScreen
import com.sharpcj.dreammusic.feature.player.PlayerViewModel
import com.sharpcj.dreammusic.feature.recent.RecentPlaysScreen
import com.sharpcj.dreammusic.feature.search.SearchScreen
import com.sharpcj.dreammusic.feature.settings.SettingsScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DreamMusicApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
) {
    val backStack = remember { mutableStateListOf<DreamMusicNavKey>(DreamMusicNavKey.Library) }
    val currentDestination = backStack.lastOrNull() ?: DreamMusicNavKey.Library
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = topLevelDestinations.indexOfFirst { it.key::class == DreamMusicNavKey.Library::class },
        pageCount = { topLevelDestinations.size },
    )
    val coroutineScope = rememberCoroutineScope()

    fun topLevelIndexOf(destination: DreamMusicNavKey): Int =
        topLevelDestinations.indexOfFirst { it.key::class == destination::class }

    fun navigateToTopLevel(destination: DreamMusicNavKey) {
        if (currentDestination::class != destination::class) {
            backStack.clear()
            backStack.add(destination)
        }
    }

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

    val currentTopLevelIndex = topLevelIndexOf(currentDestination)

    LaunchedEffect(currentTopLevelIndex) {
        if (currentTopLevelIndex >= 0 && pagerState.currentPage != currentTopLevelIndex) {
            pagerState.animateScrollToPage(currentTopLevelIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val destination = topLevelDestinations.getOrNull(page)?.key ?: return@collect
                if (topLevelIndexOf(backStack.lastOrNull() ?: DreamMusicNavKey.Library) >= 0 &&
                    (backStack.lastOrNull() ?: DreamMusicNavKey.Library)::class != destination::class
                ) {
                    backStack.clear()
                    backStack.add(destination)
                }
            }
    }

    NotificationPermissionEffect()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentDestination !is DreamMusicNavKey.Player) {
                Column {
                    MiniPlayer(
                        uiState = playerUiState,
                        onSkipToPrevious = playerViewModel::skipToPrevious,
                        onTogglePlayPause = playerViewModel::playOrPause,
                        onSkipToNext = playerViewModel::skipToNext,
                        onOpenPlayer = openPlayer,
                    )
                    DreamMusicNavigationBar(
                        currentDestination = currentDestination,
                        onDestinationSelected = { destination ->
                            navigateToTopLevel(destination)
                            val page = topLevelIndexOf(destination)
                            if (page >= 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)

        if (currentTopLevelIndex >= 0) {
            HorizontalPager(
                state = pagerState,
                modifier = contentModifier,
            ) { page ->
                when (topLevelDestinations[page].key) {
                    is DreamMusicNavKey.Library -> LibraryScreen(
                        onOpenPlayer = openPlayer,
                        onOpenLocalMusic = {
                            if (backStack.lastOrNull() !is DreamMusicNavKey.LocalMusic) {
                                backStack.add(DreamMusicNavKey.LocalMusic)
                            }
                        },
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
                    is DreamMusicNavKey.Discover -> DiscoverScreen()
                    is DreamMusicNavKey.Search -> SearchScreen(onOpenPlayer = openPlayer)
                    is DreamMusicNavKey.Settings -> SettingsScreen()
                    else -> Unit
                }
            }
        } else {
            NavDisplay(
                backStack = backStack,
                modifier = contentModifier,
                entryProvider = entryProvider {
                    entry<DreamMusicNavKey.Library> {
                        LibraryScreen(
                            onOpenPlayer = openPlayer,
                            onOpenLocalMusic = {
                                if (backStack.lastOrNull() !is DreamMusicNavKey.LocalMusic) {
                                    backStack.add(DreamMusicNavKey.LocalMusic)
                                }
                            },
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
                    entry<DreamMusicNavKey.LocalMusic> {
                        LocalMusicScreen(
                            onBack = { backStack.removeLastOrNull() },
                            onOpenPlayer = openPlayer,
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
}
