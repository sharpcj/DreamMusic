package com.sharpcj.dreammusic.feature.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.FavoriteSongsRepository
import com.sharpcj.dreammusic.core.data.LocalMusicRepository
import com.sharpcj.dreammusic.core.data.PlaybackHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val localMusicRepository: LocalMusicRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val favoriteSongsRepository: FavoriteSongsRepository,
) : ViewModel() {
    private val operationState = MutableStateFlow(SettingsOperationState())
    private val appVersion = application.versionInfo()

    val uiState = combine(
        localMusicRepository.observeLocalSongs(),
        playbackHistoryRepository.observeRecentPlayedSongs(),
        favoriteSongsRepository.observeFavoriteSongs(),
        operationState,
    ) { songs, recentPlays, favoriteSongs, operation ->
        SettingsUiState(
            songCount = songs.size,
            recentPlayCount = recentPlays.size,
            favoriteCount = favoriteSongs.size,
            isScanning = operation.isScanning,
            lastScanCount = operation.lastScanCount,
            statusMessage = operation.statusMessage,
            appVersionName = appVersion.name,
            appVersionCode = appVersion.code,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            appVersionName = appVersion.name,
            appVersionCode = appVersion.code,
        ),
    )

    val mediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasMediaPermission(): Boolean = ContextCompat.checkSelfPermission(
        application,
        mediaPermission,
    ) == PackageManager.PERMISSION_GRANTED

    fun scanLocalMusic() {
        viewModelScope.launch {
            if (!hasMediaPermission()) {
                operationState.value = SettingsOperationState(statusMessage = "需要授权读取音频文件后才能扫描本地音乐")
                return@launch
            }

            operationState.value = SettingsOperationState(isScanning = true)
            runCatching { localMusicRepository.refreshLocalSongs() }
                .onSuccess { count ->
                    operationState.value = SettingsOperationState(
                        lastScanCount = count,
                        statusMessage = "扫描完成：$count 首歌曲",
                    )
                }
                .onFailure { throwable ->
                    operationState.value = SettingsOperationState(
                        statusMessage = throwable.message ?: "扫描本地音乐失败",
                    )
                }
        }
    }

    fun clearRecentPlays() {
        viewModelScope.launch {
            playbackHistoryRepository.clear()
            operationState.value = operationState.value.copy(statusMessage = "已清空最近播放记录")
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteSongsRepository.clear()
            operationState.value = operationState.value.copy(statusMessage = "已清空喜欢的音乐")
        }
    }

    private data class SettingsOperationState(
        val isScanning: Boolean = false,
        val lastScanCount: Int? = null,
        val statusMessage: String? = null,
    )

    private data class AppVersion(
        val name: String,
        val code: Long,
    )

    private fun Application.versionInfo(): AppVersion {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        return AppVersion(
            name = packageInfo.versionName.orEmpty().ifBlank { "未知版本" },
            code = code,
        )
    }
}
