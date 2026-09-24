package echo.music.iad1tya.echomusic.component

import androidx.compose.runtime.Composable
import echo.music.iad1tya.echomusic.updater.ChangelogSection

/**
 * UpdateAvailableDialog is completely disabled for LOQ.
 */
@Composable
fun UpdateAvailableDialog(
    version: String,
    changelog: List<ChangelogSection>,
    description: String?,
    onDismiss: () -> Unit
) {
    // No-op for LOQ: update dialog rendering removed
}
