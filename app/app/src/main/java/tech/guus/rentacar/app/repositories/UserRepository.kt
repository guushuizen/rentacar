package tech.guus.rentacar.app.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
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

        return response.body<LoginResponse>().token
    }

    override suspend fun storeToken(token: String) {
        this.dataStore.edit {
            it[AppPreferencesKeys.token] = token
        }
    }
}