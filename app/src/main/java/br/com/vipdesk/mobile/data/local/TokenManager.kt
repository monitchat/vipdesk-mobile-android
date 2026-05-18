package br.com.vipdesk.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vipdesk_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val COMPANY_ID_KEY = intPreferencesKey("company_id")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    }

    suspend fun saveUserInfo(userId: Int, name: String, email: String, companyId: Int?) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = name
            prefs[USER_EMAIL_KEY] = email
            companyId?.let { prefs[COMPANY_ID_KEY] = it }
        }
    }

    suspend fun getUserId(): Int? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.first()[USER_NAME_KEY]
    }

    suspend fun getUserEmail(): String? {
        return context.dataStore.data.first()[USER_EMAIL_KEY]
    }

    fun getUserIdFlow(): Flow<Int?> {
        return context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }
    }

    suspend fun getCompanyId(): Int? {
        return context.dataStore.data.first()[COMPANY_ID_KEY]
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
