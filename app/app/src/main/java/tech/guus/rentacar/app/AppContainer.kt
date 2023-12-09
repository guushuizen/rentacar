package tech.guus.rentacar.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.CarRepositoryImpl
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.repositories.UserRepositoryImpl

class AppContainer {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    val carRepository: CarRepository = CarRepositoryImpl(httpClient)

    val userRepository: UserRepository = UserRepositoryImpl()
}