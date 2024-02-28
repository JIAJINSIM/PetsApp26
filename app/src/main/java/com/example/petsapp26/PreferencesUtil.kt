package com.example.petsapp26

import android.content.Context

object PreferencesUtil {
    private const val PREFS_NAME = "AppPrefs"


    fun getCurrentUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString("userId", null)
    }
}