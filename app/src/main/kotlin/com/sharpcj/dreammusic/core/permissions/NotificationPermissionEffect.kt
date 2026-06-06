package com.sharpcj.dreammusic.core.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android 13+ requires POST_NOTIFICATIONS before the media playback foreground
 * notification can be shown reliably. Keep this as an app-shell side effect so
 * playback slices do not need to duplicate notification permission handling.
 */
@Composable
fun NotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* Playback still works if the user denies notification visibility. */ }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
