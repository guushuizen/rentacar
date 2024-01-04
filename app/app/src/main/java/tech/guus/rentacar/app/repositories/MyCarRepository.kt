package tech.guus.rentacar.app.repositories

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tech.guus.rentacar.app.models.CarStatus
import tech.guus.rentacar.app.models.ListCarResponse
import tech.guus.rentacar.app.models.ListedCar
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


interface MyCarRepository {
    suspend fun getCars(): List<ListedCar>

    suspend fun createCar(licensePlate: String): ListedCar

    fun getCar(uuid: String): ListedCar?

    suspend fun loadCarPictures(carPictures: List<String>): List<Uri>

    suspend fun savePhotos(carUUID: UUID, photos: List<Uri>)

    suspend fun updateCar(carUUID: UUID, ratePerHour: Float, status: CarStatus)
}

class MyCarRepositoryImpl(
    private val userRepository: UserRepository,
    private val httpClient: HttpClient,
) : MyCarRepository {

    private var cars = mutableListOf<ListedCar>()

    override suspend fun getCars(): List<ListedCar> {
        val request = httpClient.get("my-cars") {
            header("Authorization", "Bearer ${userRepository.getToken()}")
        }

        val responseBody = request.body<ListCarResponse>()

        cars = responseBody.cars.toMutableList()

        return responseBody.cars
    }

    override suspend fun createCar(licensePlate: String): ListedCar {
        val request = httpClient.post("cars") {
            header("Authorization", "Bearer ${userRepository.getToken()}")

            contentType(ContentType.Application.Json)
            setBody(mapOf("licensePlate" to licensePlate))
        }

        if (request.status == HttpStatusCode.Conflict) {
            throw Exception("Er bestaat al een huurauto op het platform met dit kenteken!")
        } else if (request.status == HttpStatusCode.BadRequest) {
            throw Exception("Dit kenteken bestaat niet in de database van de RDW, weet je zeker dat je het juiste kenteken hebt ingevuld?")
        }

        val createdCar = request.body<ListedCar>()

        cars.add(createdCar)

        return createdCar
    }

    override fun getCar(uuid: String): ListedCar? {
        return cars.find { it.id.toString() == uuid }
    }

    override suspend fun loadCarPictures(carPictures: List<String>): List<Uri> {
        return carPictures.map {
            val response = this.httpClient.get(it)
            val file = File.createTempFile("rentacar", ".jpg")
            val outStream = FileOutputStream(file)
            outStream.write(response.readBytes())
            outStream.close()

            return@map file.toUri()
        }
    }

    override suspend fun savePhotos(carUUID: UUID, photos: List<Uri>) {
        val request = httpClient.submitFormWithBinaryData(
            url = "cars/$carUUID/photos",
            formData = formData {
                photos.forEach {
                    append("image[]", it.toFile().readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=${it.toFile().name}")
                    })
                }
            }
        ) {
            header("Authorization", "Bearer ${userRepository.getToken()}")
        }

        if (request.status != HttpStatusCode.OK) {
            throw Exception("Er is iets misgegaan bij het uploaden van de foto's, probeer het later opnieuw.")
        }
    }

    override suspend fun updateCar(carUUID: UUID, ratePerHour: Float, status: CarStatus) {
        val request = httpClient.patch("cars/$carUUID") {
            header("Authorization", "Bearer ${userRepository.getToken()}")

            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "ratePerHour" to ratePerHour,
                "status" to status.value
            ))
        }

        if (request.status != HttpStatusCode.OK) {
            throw Exception("Er is iets misgegaan bij het bijwerken van de listing, probeer het later opnieuw.")
        }
    }
}