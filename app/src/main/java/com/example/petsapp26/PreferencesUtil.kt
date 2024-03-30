package com.example.petsapp26

import android.content.Context
import android.util.Log

object PreferencesUtil {
    private const val PREFS_NAME = "AppPrefs"

    fun storeUserIdInPreferences(userId: String, context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userId", userId)
            apply()
        }
    }
    fun clearUserId(context: Context) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("userId")
            apply()
        }
    }

    fun getCurrentUserId(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString("userId", null)
    }

    fun storeUIDInPreferences(UID: String, context: Context) {
        Log.d("PreferencesUtil", "Storing UID: $UID")
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("UID", UID)
            apply()
        }
    }

    fun getUID(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val UID = sharedPref.getString("UID", null)
        Log.d("PreferencesUtil", "Retrieved UID: $UID")
        return UID
    }


}