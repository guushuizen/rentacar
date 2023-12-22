package tech.guus.rentacar.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.CarRepositoryImpl
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.repositories.UserRepositoryImpl


const val BASE_URL = "http://10.0.2.2:8080/"  // `localhost` from the emulator's perspective.


class AppContainer(private val dataStore: DataStore<Preferences>) {

    private val httpClient = HttpClient(Android) {
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            jackson()
        }
    }

    val carRepository: CarRepository = CarRepositoryImpl(httpClient)

    val userRepository: UserRepository = UserRepositoryImpl(httpClient, this.dataStore)
}