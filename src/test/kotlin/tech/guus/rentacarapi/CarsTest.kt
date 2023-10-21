package tech.guus.rentacarapi

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.requests.ListCarResponse
import tech.guus.rentacarapi.requests.UpdateCarRequest
import tech.guus.rentacarapi.services.DatabaseService
import java.io.File
import kotlin.test.*


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

        val uploadFolder = File("test_uploads/")
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

        assertNull(uploadFolder.listFiles())
    }

    @Test
    fun testMarkCarAsActive() = setupTestApplicationWithUser {
        val car = createDummyCar()
        val authenticatedClient = createAuthenticatedClient()
        val photoBytes = object {}.javaClass.classLoader.getResourceAsStream("car.jpg")!!.readAllBytes()

        var listResponse = authenticatedClient.get("/cars").body<ListCarResponse>()
        assertEquals(0, listResponse.cars.count())

        authenticatedClient.post("/cars/${car.id}/photos") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "image[]",
                            photoBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"car.jpeg\"")
                            }
                        )
                    },
                )
            )
        }

        assertEquals(
            HttpStatusCode.BadRequest,
            authenticatedClient.patch("/cars/${car.id}") {
                contentType(ContentType.Application.Json)
                setBody(UpdateCarRequest(status = CarStatus.ACTIVE))
            }.status
        )

        assertEquals(
            HttpStatusCode.OK,
            authenticatedClient.patch("/cars/${car.id}") {
                contentType(ContentType.Application.Json)
                setBody(
                    UpdateCarRequest(
                        status = CarStatus.ACTIVE,
                        ratePerHour = 10
                    )
                )
            }.status
        )

        transaction {
            assertNotNull(
                Car.find {
                    (Cars.id eq car.id) and (Cars.ratePerHour eq 10) and (Cars.status eq CarStatus.ACTIVE)
                }.firstOrNull()
            )
        }

        val unauthenticatedClient = createUnauthenticatedClient()
        listResponse = unauthenticatedClient.get("/cars").body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())
        val listedCar = listResponse.cars[0]
        assertEquals(10, listedCar.ratePerHour)
        assertEquals(1, listedCar.photos.count())

        val photoResponse = unauthenticatedClient.get(listedCar.photos[0])
        assertEquals(HttpStatusCode.OK, photoResponse.status)
        assertTrue(ContentType.Image.JPEG.match(photoResponse.headers["Content-Type"]!!))

        File("test_uploads/").deleteRecursively()
    }

    @Test
    fun testListCarsFilterByBrand() = setupTestApplicationWithUser {
        createDummyCar {
            this.status = CarStatus.ACTIVE
        }
        val authenticatedClient = createAuthenticatedClient()

        var listResponse: ListCarResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "VOLKSWAGEN")
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())

        listResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "Volkswagen")
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())

        listResponse = authenticatedClient.get("/cars") {
            parameter("modelName", "Scirocco")
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())


        listResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "Volkswagen")
            parameter("modelName", "Scirocco")
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())

        listResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "Ford")
        }.body<ListCarResponse>()
        assertEquals(0, listResponse.cars.count())
    }

    @Test
    fun testListCarsByCoordinatesAndRadius() = setupTestApplicationWithUser {
        createDummyCar {
            this.status = CarStatus.ACTIVE
        }
        val authenticatedClient = createAuthenticatedClient()

        var listResponse: ListCarResponse = authenticatedClient.get("/cars") {
            parameter("currentLongitude", 51.9851) // Arnhem
            parameter("currentLatitude", 5.8987) // Arnhem
            parameter("filterRadius", 25)
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())

        listResponse = authenticatedClient.get("/cars") {
            parameter("currentLongitude", 51.9851)
            parameter("currentLatitude", 5.8987)
            parameter("filterRadius", 10)
        }.body<ListCarResponse>()
        assertEquals(0, listResponse.cars.count())


        listResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "Volkswagen")
            parameter("modelName", "Scirocco")
            parameter("currentLongitude", 51.5866)  // Breda
            parameter("currentLatitude", 4.7756)
            parameter("filterRadius", 200)
        }.body<ListCarResponse>()
        assertEquals(1, listResponse.cars.count())

        listResponse = authenticatedClient.get("/cars") {
            parameter("brandName", "Volkswagen")
            parameter("modelName", "Scirocco")
            parameter("currentLongitude", 51.5866)  // Breda
            parameter("currentLatitude", 4.7756)
            parameter("filterRadius", 20)
        }.body<ListCarResponse>()
        assertEquals(0, listResponse.cars.count())
    }
}