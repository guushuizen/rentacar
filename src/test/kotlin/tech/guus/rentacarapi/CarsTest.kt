package tech.guus.rentacarapi

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.Car
import tech.guus.rentacarapi.models.CarDTO
import tech.guus.rentacarapi.models.CarStatus
import tech.guus.rentacarapi.models.Cars
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.services.DatabaseService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class CarsTest {

    @Test
    fun testCreateCar() = setupTestApplicationWithUser {
        val response = createAuthenticatedClient().post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCarRequest(
                    licensePlate = "L369JR"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        transaction(DatabaseService.database) {
            val foundCar = Car.find { Cars.licensePlate eq "L369JR" }.first()
            assertNotNull(foundCar)
            assertEquals(CarStatus.DRAFT, foundCar.status)
        }

        val responseBody = response.body() as CarDTO
        assertEquals("Guus Huizen", responseBody.ownerName)
    }

    @Test
    fun testCreateDuplicateCarByLicensePlate() = setupTestApplicationWithUser {
        createAuthenticatedClient().post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCarRequest(
                    licensePlate = "L369JR"
                )
            )
        }

        transaction(DatabaseService.database) {
            assertNotNull(Car.find { Cars.licensePlate eq "L369JR" }.firstOrNull())
        }

        var response = createAuthenticatedClient().post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCarRequest(
                    licensePlate = "L369JR"
                )
            )
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun testLicensePlateMustBe6CharactersLong() = setupTestApplicationWithUser {
        assertEquals(HttpStatusCode.BadRequest, createAuthenticatedClient().post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCarRequest(
                    licensePlate = "L369J"
                )
            )
        }.status)

        assertEquals(HttpStatusCode.BadRequest, createAuthenticatedClient().post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCarRequest(
                    licensePlate = "L369JRR"
                )
            )
        }.status)
    }

}