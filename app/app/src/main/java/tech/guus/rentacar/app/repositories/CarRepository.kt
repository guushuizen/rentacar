package tech.guus.rentacar.app.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import tech.guus.rentacar.app.BASE_URL
import tech.guus.rentacar.app.models.ChosenFilterValues
import tech.guus.rentacar.app.models.ListCarResponse
import tech.guus.rentacar.app.models.ListedCar


abstract class CarRepository {

    abstract suspend fun getAllCars(filterValues: ChosenFilterValues? = null): List<ListedCar>

}

class CarRepositoryImpl(private val httpClient: HttpClient) : CarRepository() {
    override suspend fun getAllCars(filterValues: ChosenFilterValues?): List<ListedCar> {
        val listResponse: HttpResponse = httpClient.get("${BASE_URL}/cars") {
            if (filterValues != null) {
                if (filterValues.chosenBrandName != null)
                    parameter("brandName", filterValues.chosenBrandName)

                if (filterValues.chosenModelName != null)
                    parameter("modelName", filterValues.chosenModelName)

                if (filterValues.chosenCoordinates != null && filterValues.chosenRadius != null) {
                    parameter("currentLatitude", filterValues.chosenCoordinates.latitude)
                    parameter("currentLongitude", filterValues.chosenCoordinates.longitude)
                    parameter("filterRadius", filterValues.chosenRadius)
                }
            }
        }

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