package com.example.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean("app_lock_enabled", false)
        set(value) = prefs.edit().putBoolean("app_lock_enabled", value).apply()

    var isCloudSyncEnabled: Boolean
        get() = prefs.getBoolean("cloud_sync_enabled", true)
        set(value) = prefs.edit().putBoolean("cloud_sync_enabled", value).apply()
}
