package tech.guus.rentacar.app

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.fasterxml.jackson.databind.DeserializationFeature
import com.google.android.gms.location.LocationServices
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import tech.guus.rentacar.app.activities.dataStore
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.CarRepositoryImpl
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.repositories.UserRepositoryImpl
import tech.guus.rentacar.app.services.LocationService


const val BASE_URL = "http://10.0.2.2:8080/"  // `localhost` from the emulator's perspective.


class AppContainer(val activity: ComponentActivity) {

    private val httpClient = HttpClient(Android) {
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    val userRepository: UserRepository = UserRepositoryImpl(
        httpClient,
        this.activity.applicationContext.dataStore
    )

    val carRepository: CarRepository = CarRepositoryImpl(httpClient, userRepository)

    val locationService = LocationService(
        activity = this.activity,
        httpClient = httpClient,
    )
}