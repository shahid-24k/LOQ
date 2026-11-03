package echo.music.iad1tya.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import echo.music.iad1tya.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class LoqReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val apkUrl: String,
    val apkSize: Long,
    val sha256Url: String? = null,
    val expectedSha256: String? = null
)

sealed class UpdateCheckResult {
    data class Available(val release: LoqReleaseInfo) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String, val isRateLimited: Boolean = false, val isOffline: Boolean = false) : UpdateCheckResult()
}

object LoqUpdateManager {
    private const val TAG = "LoqUpdateManager"
    const val GITHUB_OWNER = "shahid-24k"
    const val GITHUB_REPO = "LOQ"
    const val REPO_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO"
    const val LATEST_RELEASE_API = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Checks the official public GitHub Releases API for new LOQ releases.
     * Offline and rate-limited responses are handled gracefully.
     */
    suspend fun checkForUpdates(installedVersionName: String = BuildConfig.VERSION_NAME): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(LATEST_RELEASE_API)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "LOQ-Android/$installedVersionName")
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string()

            if (code == 403 || code == 429) {
                Timber.tag(TAG).w("GitHub API rate limited: HTTP $code")
                return@withContext UpdateCheckResult.Error(
                    message = "GitHub API rate limit reached. Please try again later.",
                    isRateLimited = true
                )
            }

            if (code == 404) {
                // No releases published yet
                Timber.tag(TAG).i("No releases found on repository $GITHUB_OWNER/$GITHUB_REPO")
                return@withContext UpdateCheckResult.UpToDate
            }

            if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                Timber.tag(TAG).e("GitHub API release check failed: HTTP $code")
                return@withContext UpdateCheckResult.Error("Unable to fetch latest release (HTTP $code)")
            }

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "").trim()
            val name = json.optString("name", tagName)
            val body = json.optString("body", "No release notes provided.")
            val publishedAt = json.optString("published_at", "")
            val assets = json.optJSONArray("assets")

            if (tagName.isBlank() || assets == null || assets.length() == 0) {
                return@withContext UpdateCheckResult.UpToDate
            }

            // Find universal release APK asset
            var apkDownloadUrl: String? = null
            var apkFileSize: Long = 0L
            var sha256DownloadUrl: String? = null

            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val assetName = asset.optString("name", "")
                val assetUrl = asset.optString("browser_download_url", "")
                val size = asset.optLong("size", 0L)

                if (assetName.endsWith(".apk", ignoreCase = true)) {
                    if (apkDownloadUrl == null || assetName.contains("universal", ignoreCase = true)) {
                        apkDownloadUrl = assetUrl
                        apkFileSize = size
                    }
                } else if (assetName.endsWith(".sha256", ignoreCase = true)) {
                    sha256DownloadUrl = assetUrl
                }
            }

            if (apkDownloadUrl == null) {
                return@withContext UpdateCheckResult.UpToDate
            }

            // Check if latest tag is newer than current installed version
            val isNewer = isVersionNewer(tagName, installedVersionName)
            if (!isNewer) {
                return@withContext UpdateCheckResult.UpToDate
            }

            // Fetch SHA-256 checksum if available
            var expectedSha: String? = null
            if (!sha256DownloadUrl.isNullOrBlank()) {
                try {
                    val shaReq = Request.Builder().url(sha256DownloadUrl).build()
                    val shaResp = httpClient.newCall(shaReq).execute()
                    val shaText = shaResp.body?.string()?.trim()
                    if (!shaText.isNullOrBlank()) {
                        expectedSha = shaText.split("\\s+".toRegex()).firstOrNull()
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Failed to fetch SHA-256 asset")
                }
            }

            val releaseInfo = LoqReleaseInfo(
                tagName = tagName,
                name = name,
                body = body,
                publishedAt = publishedAt,
                apkUrl = apkDownloadUrl,
                apkSize = apkFileSize,
                sha256Url = sha256DownloadUrl,
                expectedSha256 = expectedSha
            )

            return@withContext UpdateCheckResult.Available(releaseInfo)
        } catch (e: java.net.UnknownHostException) {
            Timber.tag(TAG).d("Offline: cannot reach GitHub Releases API")
            return@withContext UpdateCheckResult.Error("No internet connection", isOffline = true)
        } catch (e: java.net.SocketTimeoutException) {
            return@withContext UpdateCheckResult.Error("Connection timed out", isOffline = true)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error checking for LOQ updates")
            return@withContext UpdateCheckResult.Error(e.message ?: "Update check failed")
        }
    }

    /**
     * Downloads the APK file to cache with progress tracking and SHA-256 verification.
     */
    suspend fun downloadApk(
        context: Context,
        release: LoqReleaseInfo,
        onProgress: (progress: Float, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val targetFile = File(updatesDir, "LOQ-${release.tagName}.apk")

            val request = Request.Builder()
                .url(release.apkUrl)
                .header("User-Agent", "LOQ-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed with HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val contentLength = body.contentLength().takeIf { it > 0 } ?: release.apkSize

            var downloadedBytes = 0L
            val buffer = ByteArray(8 * 1024)
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            val digest = MessageDigest.getInstance("SHA-256")

            inputStream.use { input ->
                outputStream.use { output ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (contentLength > 0) {
                            val progress = (downloadedBytes.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress, downloadedBytes, contentLength)
                        }
                    }
                    output.flush()
                }
            }

            // Verify SHA-256 if expected checksum is known
            if (!release.expectedSha256.isNullOrBlank()) {
                val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
                if (!computedHash.equals(release.expectedSha256.trim(), ignoreCase = true)) {
                    targetFile.delete()
                    return@withContext Result.failure(Exception("SHA-256 verification failed! Corrupted or altered download."))
                }
            }

            // Verify signing certificate matches the currently running app
            val signatureValid = verifyApkSignature(context, targetFile)
            if (!signatureValid) {
                targetFile.delete()
                return@withContext Result.failure(Exception("APK signature verification failed: Signing certificate does not match installed application."))
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error downloading update APK")
            Result.failure(e)
        }
    }

    /**
     * Verifies that the downloaded APK's signing certificate matches the installed application.
     */
    @Suppress("DEPRECATION")
    fun verifyApkSignature(context: Context, apkFile: File): Boolean {
        try {
            val pm = context.packageManager
            val installedPackage = context.packageName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val installedInfo = pm.getPackageInfo(installedPackage, PackageManager.GET_SIGNING_CERTIFICATES)
                val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES) ?: return false

                val installedSigners = installedInfo.signingInfo?.apkContentsSigners
                val archiveSigners = archiveInfo.signingInfo?.apkContentsSigners

                if (installedSigners.isNullOrEmpty() || archiveSigners.isNullOrEmpty()) {
                    return false
                }

                val installedSignatures = installedSigners.map { it.toCharsString() }.toSet()
                val archiveSignatures = archiveSigners.map { it.toCharsString() }.toSet()
                return installedSignatures.intersect(archiveSignatures).isNotEmpty()
            } else {
                val installedInfo = pm.getPackageInfo(installedPackage, PackageManager.GET_SIGNATURES)
                val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES) ?: return false

                val installedSigners = installedInfo.signatures
                val archiveSigners = archiveInfo.signatures

                if (installedSigners.isNullOrEmpty() || archiveSigners.isNullOrEmpty()) {
                    return false
                }

                val installedSignatures = installedSigners.map { it.toCharsString() }.toSet()
                val archiveSignatures = archiveSigners.map { it.toCharsString() }.toSet()
                return installedSignatures.intersect(archiveSignatures).isNotEmpty()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Signature comparison error")
            return false
        }
    }

    /**
     * Triggers the Android package installation flow via FileProvider.
     * Handles API 26+ unknown app permission if needed.
     */
    fun startInstallation(context: Context, apkFile: File): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(manageIntent)
                    return false
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.FileProvider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            return true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting APK installation")
            return false
        }
    }

    /**
     * Cleans up old cached update APK files in cacheDir/updates.
     */
    fun clearCachedApks(context: Context) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (updatesDir.exists()) {
                updatesDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to clear cached APKs")
        }
    }

    /**
     * Compares two semantic version strings (e.g. "v1.3.0" vs "1.2.6").
     */
    fun isVersionNewer(remoteVersion: String, currentVersion: String): Boolean {
        val remoteClean = remoteVersion.removePrefix("v").removePrefix("V").trim()
        val currentClean = currentVersion.removePrefix("v").removePrefix("V").trim()

        val remoteParts = remoteClean.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

        val length = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}

