package tech.guus.rentacar.app.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tech.guus.rentacar.app.BASE_URL
import tech.guus.rentacar.app.models.ChosenFilterValues
import tech.guus.rentacar.app.models.ListCarResponse
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.models.ReserveCarRequest
import tech.guus.rentacar.app.models.ReserveCarResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


abstract class CarRepository {

    /**
     * Finds a list of cars, optionally filtered by the given filter values.
     */
    abstract suspend fun getAllCars(filterValues: ChosenFilterValues? = null): List<ListedCar>

    /**
     * Finds a single car by its UUID.
     */
    abstract fun getCar(carUuid: String): ListedCar?

    /**
     * Reserves a car for the currently logged in user from the given start DateTime to the given
     * end DateTime.
     *
     * Returns an error message if the reservation failed, or null if the reservation succeeded.
     */
    abstract suspend fun reserveCar(carUuid: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): String?
}

class CarRepositoryImpl(
    private val httpClient: HttpClient,
    private val userRepository: UserRepository
) : CarRepository() {

    private var cars: List<ListedCar> = emptyList()
    override suspend fun getAllCars(filterValues: ChosenFilterValues?): List<ListedCar> {
        val listResponse: HttpResponse = httpClient.get("cars") {
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

        val parsedCars = listResponse.body<ListCarResponse>().cars

        if (filterValues?.hasNoFilters() != false)
            this.cars = parsedCars

        return parsedCars
    }

    override fun getCar(carUuid: String): ListedCar? {
        return cars.firstOrNull { it.id.toString() == carUuid }
    }

    override suspend fun reserveCar(
        carUuid: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): String? {
        val response = this.httpClient.post("cars/${carUuid}/reserve") {
            setBody(ReserveCarRequest(
                startDateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT),
                endDateTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT)
            ))
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${userRepository.getToken()}")
        }

        return if (response.status.value >= 400) {
            response.bodyAsText()
        } else if (response.status.value >= 500) {
            "Er is iets misgegaan bij het reserveren van de auto. Probeer het later opnieuw."
        } else {
            val parsedResponse = response.body<ReserveCarResponse>()

            this.cars = cars.map {
                if (it.id == parsedResponse.reservedCar.id) parsedResponse.reservedCar else it
            }

            null
        }
    }
}