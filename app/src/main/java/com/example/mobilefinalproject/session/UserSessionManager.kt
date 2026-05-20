package com.example.mobilefinalproject.session

import android.content.Context
import androidx.core.content.edit

object UserSessionManager {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone"
    private const val KEY_PROFILE_IMAGE_URL = "profile_image_url"

    enum class UserType(val value: String) {
        DRIVER("driver"),
        CUSTOMER("customer");

        companion object {
            fun fromValue(value: String?): UserType? = entries.firstOrNull { it.value == value }
        }
    }

    data class UserSession(
        val userType: UserType,
        val userId: String,
        val fullName: String,
        val email: String = "",
        val phone: String? = null,
        val profileImageUrl: String? = null,
    )

    fun saveSession(context: Context, session: UserSession) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_USER_TYPE, session.userType.value)
            putString(KEY_USER_ID, session.userId)
            putString(KEY_USER_NAME, session.fullName)
            putString(KEY_EMAIL, session.email)
            putString(KEY_PHONE, session.phone)
            putString(KEY_PROFILE_IMAGE_URL, session.profileImageUrl)
        }
    }

    fun getSession(context: Context): UserSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userType = UserType.fromValue(prefs.getString(KEY_USER_TYPE, null)) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        val fullName = prefs.getString(KEY_USER_NAME, null)?.takeIf { it.isNotBlank() } ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: ""
        val phone = prefs.getString(KEY_PHONE, null)
        val profileImageUrl = prefs.getString(KEY_PROFILE_IMAGE_URL, null)
        return UserSession(userType, userId, fullName, email, phone, profileImageUrl)
    }

    fun hasSession(context: Context): Boolean = getSession(context) != null

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
    }
}
