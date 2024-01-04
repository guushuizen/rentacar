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
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.repositories.MyCarRepositoryImpl
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.repositories.UserRepositoryImpl
import tech.guus.rentacar.app.services.LocationService
import tech.guus.rentacar.app.services.LocationServiceImpl


const val BASE_URL = "http://10.0.2.2:8080/"  // `localhost` from the emulator's perspective.


interface AppContainerInterface {
    val userRepository: UserRepository
    val carRepository: CarRepository
    val myCarRepository: MyCarRepository
    val locationService: LocationService
}


class AppContainer(val activity: ComponentActivity) : AppContainerInterface {

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

    override val userRepository: UserRepository = UserRepositoryImpl(
        httpClient,
        this.activity.applicationContext.dataStore
    )

    override val carRepository: CarRepository = CarRepositoryImpl(httpClient, userRepository)

    override val locationService = LocationServiceImpl(
        activity = this.activity,
        httpClient = httpClient,
    )

    override val myCarRepository: MyCarRepository = MyCarRepositoryImpl(
        userRepository = userRepository,
        httpClient = httpClient,
    )
}