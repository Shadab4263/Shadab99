package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object GameLauncherHelper {
    const val PACKAGE_BGMI = "com.pubg.imobile"
    const val PACKAGE_PUBG_GLOBAL = "com.tencent.ig"
    const val PACKAGE_PUBG_KR = "com.pubg.krmobile"
    const val PACKAGE_PUBG_VN = "com.vng.pubgmobile"

    data class InstalledGameInfo(
        val packageName: String,
        val displayName: String,
        val isInstalled: Boolean
    )

    fun getInstalledGames(context: Context): List<InstalledGameInfo> {
        val pm = context.packageManager
        val list = listOf(
            InstalledGameInfo(PACKAGE_BGMI, "Battlegrounds Mobile India (BGMI)", isPackageInstalled(pm, PACKAGE_BGMI)),
            InstalledGameInfo(PACKAGE_PUBG_GLOBAL, "PUBG Mobile (Global)", isPackageInstalled(pm, PACKAGE_PUBG_GLOBAL)),
            InstalledGameInfo(PACKAGE_PUBG_KR, "PUBG Mobile (Korea/Japan)", isPackageInstalled(pm, PACKAGE_PUBG_KR)),
            InstalledGameInfo(PACKAGE_PUBG_VN, "PUBG Mobile (Vietnam)", isPackageInstalled(pm, PACKAGE_PUBG_VN))
        )
        return list
    }

    private fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchGameOrStore(context: Context, packageName: String = PACKAGE_BGMI) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            // Open Play Store
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(storeIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
            }
        }
    }

    fun openDisplaySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open display settings directly", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDeveloperSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
