package echo.music.iad1tya.performance

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * Performance profile determining visual complexity, animation density,
 * and caching strategies.
 */
enum class PerformanceProfile {
    LITE,
    BALANCED,
    FULL
}

/**
 * User-configurable performance mode options for Settings.
 */
enum class PerformanceModePreference(val value: String) {
    AUTO("auto"),
    LITE("lite"),
    BALANCED("balanced"),
    FULL("full");

    companion object {
        fun fromValue(value: String): PerformanceModePreference =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: AUTO
    }
}

object PerformanceManager {
    private var cachedDetectedProfile: PerformanceProfile? = null

    /**
     * Resolves the active performance profile given user preference and device metrics.
     */
    fun resolveProfile(context: Context, preferenceValue: String): PerformanceProfile {
        val pref = PerformanceModePreference.fromValue(preferenceValue)
        return when (pref) {
            PerformanceModePreference.LITE -> PerformanceProfile.LITE
            PerformanceModePreference.BALANCED -> PerformanceProfile.BALANCED
            PerformanceModePreference.FULL -> PerformanceProfile.FULL
            PerformanceModePreference.AUTO -> detectDeviceProfile(context)
        }
    }

    /**
     * Automatically detect the optimal performance profile using reliable Android signals:
     * - ActivityManager.isLowRamDevice
     * - Memory class (heap limit)
     * - Total physical RAM via MemoryInfo
     * - Available processor cores
     */
    fun detectDeviceProfile(context: Context): PerformanceProfile {
        cachedDetectedProfile?.let { return it }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val profile = if (activityManager != null) {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val isLowRam = activityManager.isLowRamDevice
            val memoryClass = activityManager.memoryClass
            val cores = Runtime.getRuntime().availableProcessors()

            when {
                // Low-RAM device or <= 2.5 GB RAM or heap <= 128MB
                isLowRam || totalRamGb <= 2.5 || memoryClass <= 128 -> PerformanceProfile.LITE
                // Entry-level: <= 4.0 GB RAM or <= 4 cores or heap <= 256MB
                totalRamGb <= 4.2 || cores <= 4 || memoryClass <= 256 -> PerformanceProfile.BALANCED
                // Capable device
                else -> PerformanceProfile.FULL
            }
        } else {
            PerformanceProfile.BALANCED
        }

        cachedDetectedProfile = profile
        return profile
    }

    fun isRiveEnabled(profile: PerformanceProfile): Boolean {
        return profile != PerformanceProfile.LITE
    }

    fun allowHighRefreshRate(profile: PerformanceProfile): Boolean {
        return profile == PerformanceProfile.FULL
    }

    fun thumbnailTargetSizeDp(profile: PerformanceProfile): Int {
        return when (profile) {
            PerformanceProfile.LITE -> 120
            PerformanceProfile.BALANCED -> 200
            PerformanceProfile.FULL -> 300
        }
    }
}

val LocalPerformanceProfile = compositionLocalOf { PerformanceProfile.BALANCED }

@Composable
@ReadOnlyComposable
fun currentPerformanceProfile(): PerformanceProfile = LocalPerformanceProfile.current

