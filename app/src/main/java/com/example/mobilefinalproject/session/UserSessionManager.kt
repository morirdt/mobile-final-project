package com.example.mobilefinalproject.session

import android.content.Context
import androidx.core.content.edit
import com.example.mobilefinalproject.models.Customer
import com.example.mobilefinalproject.models.driver.Driver

object UserSessionManager {
    private const val PREFS_NAME = "user_session_prefs"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"

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
    )

    fun saveDriver(context: Context, driver: Driver) {
        saveSession(context, UserSession(UserType.DRIVER, driver.id, driver.fullName))
    }

    fun saveCustomer(context: Context, customer: Customer) {
        saveSession(context, UserSession(UserType.CUSTOMER, customer.id, customer.fullName))
    }

    fun saveSession(context: Context, session: UserSession) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_USER_TYPE, session.userType.value)
            putString(KEY_USER_ID, session.userId)
            putString(KEY_USER_NAME, session.fullName)
        }
    }

    fun getSession(context: Context): UserSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userType = UserType.fromValue(prefs.getString(KEY_USER_TYPE, null)) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        val fullName = prefs.getString(KEY_USER_NAME, null)?.takeIf { it.isNotBlank() } ?: return null
        return UserSession(userType, userId, fullName)
    }

    fun hasSession(context: Context): Boolean = getSession(context) != null

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
    }

    fun restoreDriver(context: Context): Driver? {
        val session = getSession(context) ?: return null
        return if (session.userType == UserType.DRIVER) {
            Driver(session.userId, session.fullName)
        } else {
            null
        }
    }

    fun restoreCustomer(context: Context): Customer? {
        val session = getSession(context) ?: return null
        return if (session.userType == UserType.CUSTOMER) {
            Customer(session.userId, session.fullName)
        } else {
            null
        }
    }
}


