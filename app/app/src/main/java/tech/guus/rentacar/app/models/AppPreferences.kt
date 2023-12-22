package tech.guus.rentacar.app.models

import androidx.datastore.preferences.core.stringPreferencesKey

data class AppPreferences(val token: String?)

object AppPreferencesKeys {
    val token = stringPreferencesKey("token")
}
