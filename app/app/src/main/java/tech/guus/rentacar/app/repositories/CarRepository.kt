package tech.guus.rentacar.app.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import tech.guus.rentacar.app.BASE_URL
import tech.guus.rentacar.app.models.ListCarResponse
import tech.guus.rentacar.app.models.ListedCar


abstract class CarRepository {

    abstract suspend fun getAllCars(): List<ListedCar>

}

class CarRepositoryImpl(private val httpClient: HttpClient) : CarRepository() {
    override suspend fun getAllCars(): List<ListedCar> {
        val listResponse: HttpResponse = httpClient.get("${BASE_URL}/cars")

        return listResponse.body<ListCarResponse>().cars.let { list ->
            return@let list.map { car ->
                return@map car.copy(
                    photos = car.photos.map {photoPath ->
                        "${BASE_URL}/${photoPath}"
                    }
                )
            }
        }
    }
}