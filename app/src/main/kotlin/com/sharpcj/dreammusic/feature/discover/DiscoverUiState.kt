package com.sharpcj.dreammusic.feature.discover

import com.sharpcj.dreammusic.core.network.dto.BillboardSong
import com.sharpcj.dreammusic.core.network.dto.BillboardType

data class DiscoverUiState(
    val selectedBillboardType: BillboardType = BillboardType.Hot,
    val billboardSongs: List<BillboardSong> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && errorMessage == null && billboardSongs.isEmpty()
}
