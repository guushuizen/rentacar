package tech.guus.rentacarapi

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.services.DatabaseService
import java.io.File
import java.lang.NullPointerException
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail


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
            assertEquals("VOLKSWAGEN", foundCar.brandName)
            assertEquals("SCIROCCO", foundCar.modelName)
            assertEquals("GRIJS", foundCar.color)
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

    @Test
    fun testUploadPictureForCar() = setupTestApplicationWithUser {
        val car = createDummyCar()

        val boundary = "WebAppBoundary"
        var response = createAuthenticatedClient().post("/cars/${car.id}/photos") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image[]",
                            object {}.javaClass.classLoader.getResourceAsStream("car.jpg")!!.readAllBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"car.jpeg\"")
                            }
                        )

                        append(
                            "image[]",
                            object {}.javaClass.classLoader.getResourceAsStream("car.jpg")!!.readAllBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"car.jpeg\"")
                            }
                        )

                        append(
                            "image[]",
                            object {}.javaClass.classLoader.getResourceAsStream("car.jpg")!!.readAllBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"car.jpeg\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        var savedPhotos = transaction {
            CarPhoto.find {
                CarPhotos.carUuid eq car.id
            }.toList()
        }

        assertEquals(3, savedPhotos.count())

        val uploadFolder = File("uploads/")
        assertEquals(3, uploadFolder.listFiles()!!.count())

        response = createAuthenticatedClient().post("/cars/${car.id}/photos") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image[]",
                            object {}.javaClass.classLoader.getResourceAsStream("car.jpg")!!.readAllBytes(),
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"car.jpeg\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        savedPhotos = transaction {
            CarPhoto.find {
                CarPhotos.carUuid eq car.id
            }.toList()
        }

        assertEquals(1, savedPhotos.count())

        assertEquals(1, uploadFolder.listFiles()!!.count())

        uploadFolder.deleteRecursively()
    }
}