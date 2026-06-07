package com.sharpcj.dreammusic.feature.settings

data class SettingsUiState(
    val songCount: Int = 0,
    val recentPlayCount: Int = 0,
    val favoriteCount: Int = 0,
    val isScanning: Boolean = false,
    val lastScanCount: Int? = null,
    val statusMessage: String? = null,
    val appVersionName: String = "",
    val appVersionCode: Long = 0L,
)
