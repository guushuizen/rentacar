package tech.guus.rentacar.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import tech.guus.rentacar.app.models.ChosenFilterValues
import tech.guus.rentacar.app.models.Coordinates
import tech.guus.rentacar.app.repositories.CarRepositoryImpl
import kotlin.test.assertEquals

class CarRepositoryTest {

    @Test
    fun testGetUnfilteredListOfCars() {
        val mockEngine = MockEngine {request ->
            respond(
                content = """{ "cars": [] }""",
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                jackson()
            }

            defaultRequest {
                url(BASE_URL)
            }
        }

        val carRepository = CarRepositoryImpl(httpClient = httpClient, mock())

        runBlocking { carRepository.getAllCars() }

        assertEquals(1, mockEngine.requestHistory.size)
        var request = mockEngine.requestHistory[0]
        assertEquals(EmptyContent, request.body)
        assertEquals("http://10.0.2.2:8080/cars", request.url.toString())

        runBlocking {
            carRepository.getAllCars(ChosenFilterValues(
                chosenBrandName = "Volkswagen",
                chosenModelName = "Golf",
                chosenCoordinates = Coordinates(51.0, 5.0),
                chosenRadius = 10
            ))
        }

        assertEquals(2, mockEngine.requestHistory.size)
        request = mockEngine.requestHistory[1]
        assertEquals(EmptyContent, request.body)
        assertEquals(
            "http://10.0.2.2:8080/cars?brandName=Volkswagen&modelName=Golf&currentLatitude=51.0&currentLongitude=5.0&filterRadius=10",
            request.url.toString()
        )
    }
}