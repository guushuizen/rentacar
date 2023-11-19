package tech.guus.rentacar.app.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import tech.guus.rentacar.app.models.ListCarResponse
import tech.guus.rentacar.app.models.ListedCar


abstract class CarRepository {

    abstract suspend fun getAllCars(): List<ListedCar>

}

class CarRepositoryImpl(val httpClient: HttpClient) : CarRepository() {
    private val baseUrl: String = "http://10.0.2.2:8080"  // `localhost` from the emulator's perspective.

    override suspend fun getAllCars(): List<ListedCar> {
        val listResponse: HttpResponse = httpClient.get("${baseUrl}/cars")

        return listResponse.body<ListCarResponse>().cars.let { list ->
            return@let list.map { car ->
                return@map car.copy(
                    photos = car.photos.map {photoPath ->
                        "${baseUrl}/${photoPath}"
                    }
                )
            }
        }
    }
}