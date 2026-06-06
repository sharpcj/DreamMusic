package com.sharpcj.dreammusic.core.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DreamMusicNavigationBar(
    currentDestination: DreamMusicNavKey,
    onDestinationSelected: (DreamMusicNavKey) -> Unit,
) {
    NavigationBar {
        topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination::class == destination.key::class,
                onClick = { onDestinationSelected(destination.key) },
                icon = { Text(destination.label.take(1)) },
                label = { Text(destination.label) },
            )
        }
    }
}
