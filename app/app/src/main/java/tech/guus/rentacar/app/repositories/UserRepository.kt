package tech.guus.rentacar.app.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import tech.guus.rentacar.app.models.AppPreferences
import tech.guus.rentacar.app.models.AppPreferencesKeys
import tech.guus.rentacar.app.models.LoginRequest
import tech.guus.rentacar.app.models.LoginResponse
import tech.guus.rentacar.app.models.UserDTO

abstract class UserRepository {

    abstract var loggedInUser: UserDTO?

    /**
     * Attempts to login with the given credentials and returns
     * the generated JWT token for future authentication.
     *
     * @return The JWT token
     */
    abstract suspend fun login(email: String, password: String): String?

    /**
     * Persists the authentication token on the device's local storage.
     */
    abstract suspend fun storeToken(token: String)

    /**
     * Attempts to login with the stored authentication token.
     */
    abstract suspend fun attemptCachedLogin()
}

class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>
) : UserRepository() {
    override var loggedInUser: UserDTO? = null

    override suspend fun login(email: String, password: String): String? {
        val response = httpClient.post("login") {
            setBody(LoginRequest(email, password))
        }

        if (response.status != HttpStatusCode.OK)
            return null

        val body = response.body<LoginResponse>()

        this.loggedInUser = body.user

        return body.token
    }

    override suspend fun storeToken(token: String) {
        this.dataStore.edit {
            it[AppPreferencesKeys.token] = token
        }
    }

    private suspend fun getStoredToken(): String? {
        val preferences: Flow<AppPreferences> = this.dataStore.data
            .map {
                AppPreferences(token = it[AppPreferencesKeys.token])
            }

        return preferences.first().token
    }

    override suspend fun attemptCachedLogin() {
        if (this.loggedInUser != null)
            return

        val token = getStoredToken() ?: return

        val response = this.httpClient.get("users") {
            header("Authorization", "Bearer $token")
        }

        if (response.status != HttpStatusCode.OK)
            return

        this.loggedInUser = response.body<UserDTO>()
    }
}