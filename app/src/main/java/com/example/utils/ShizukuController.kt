package com.example.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.model.ShizukuState
import com.example.model.ShizukuStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuController {
    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val SHIZUKU_REQUEST_CODE = 8001

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getShizukuState(context: Context): ShizukuState {
        val installed = isShizukuInstalled(context)
        val binderAlive = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }

        if (!installed && !binderAlive) {
            return ShizukuState(
                status = ShizukuStatus.NOT_INSTALLED,
                isInstalled = false,
                isBinderAlive = false,
                isPermissionGranted = false
            )
        }

        if (!binderAlive) {
            return ShizukuState(
                status = ShizukuStatus.INSTALLED_STOPPED,
                isInstalled = true,
                isBinderAlive = false,
                isPermissionGranted = false
            )
        }

        val hasPermission = try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }

        val version = try {
            Shizuku.getVersion()
        } catch (e: Throwable) {
            0
        }

        val status = if (hasPermission) {
            ShizukuStatus.AUTHORIZED_ACTIVE
        } else {
            ShizukuStatus.PERMISSION_REQUIRED
        }

        return ShizukuState(
            status = status,
            isInstalled = installed,
            isBinderAlive = true,
            isPermissionGranted = hasPermission,
            version = version
        )
    }

    fun requestShizukuPermission(activity: Activity): Boolean {
        return try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                    true
                } else {
                    true
                }
            } else {
                false
            }
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun executeElevatedShell(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            if (!Shizuku.pingBinder()) {
                return@withContext Pair(false, "Shizuku service is not active. Start Shizuku first.")
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                return@withContext Pair(false, "Shizuku permission not granted. Tap Request Permission.")
            }

            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as java.lang.Process
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.append("ERROR: ").append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val text = output.toString().trim()
            val result = if (text.isEmpty()) "Command executed successfully (exit code $exitCode)" else text
            Pair(exitCode == 0, result)
        } catch (e: Throwable) {
            Pair(false, "Execution failed: ${e.message}")
        }
    }

    suspend fun applyFpsTuning(targetFps: Int): Pair<Boolean, String> {
        val fpsRate = targetFps.toFloat()
        // Commands to force system & global display rates across all OEMs (Samsung, Xiaomi, OnePlus, Pixel, etc.)
        val command = buildString {
            append("settings put system peak_refresh_rate $fpsRate; ")
            append("settings put system min_refresh_rate $fpsRate; ")
            append("settings put global peak_refresh_rate $fpsRate; ")
            append("settings put global min_refresh_rate $fpsRate; ")
            append("settings put system user_refresh_rate $targetFps; ")
            append("settings put system display_refresh_rate $targetFps; ")
            if (targetFps >= 90) {
                append("cmd power set-fixed-performance-mode-enabled true 2>/dev/null; ")
            } else {
                append("cmd power set-fixed-performance-mode-enabled false 2>/dev/null; ")
            }
            append("echo 'Applied $targetFps FPS / ${targetFps}Hz system display lock'")
        }
        return executeElevatedShell(command)
    }

    fun openShizukuOrStore(context: Context) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SHIZUKU_PACKAGE"))
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(storeIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$SHIZUKU_PACKAGE"))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
            }
        }
    }

    fun copyAdbCommandToClipboard(context: Context, targetFps: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cmd = "adb shell settings put system peak_refresh_rate $targetFps.0 && adb shell settings put system min_refresh_rate $targetFps.0"
        val clip = ClipData.newPlainText("FPS ADB Command", cmd)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied ADB command for $targetFps FPS to clipboard", Toast.LENGTH_SHORT).show()
    }
}
