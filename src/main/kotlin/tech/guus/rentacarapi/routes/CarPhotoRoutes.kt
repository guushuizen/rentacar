package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import java.io.File
import java.util.*

fun Route.carPhotoRoutes() {
    authenticate {
        post("/cars/{carUuid}/photos") {
            val user = call.principal<User>()!!
            val carUuid = call.parameters["carUuid"]

            lateinit var oldPhotos: List<CarPhoto>

            val car: Car = transaction {
                val car = Car.find { (Cars.ownerUuid eq user.id) and (Cars.id eq UUID.fromString(carUuid)) }
                    .singleOrNull()

                if (car != null)
                    oldPhotos = car.load(Car::photos).photos.toList()

                return@transaction car
            } ?: return@post call.respond(HttpStatusCode.Forbidden)

            val requestData = call.receiveMultipart()

            val newPhotos = mutableListOf<Pair<Int, String>>()

            val uploadPath = this@carPhotoRoutes.environment!!.config.property("ktor.upload_dir").getString()

            var index = 0
            requestData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()
                    val path = "${UUID.randomUUID()}-$fileName"
                    val file = File("$uploadPath/$path")
                    file.parentFile.mkdirs()
                    file.writeBytes(fileBytes)
                    newPhotos.add(Pair(index, path))
                    index++
                }
            }

            transaction {
                car.photos.forEach { it.delete() }

                newPhotos.forEach { CarPhoto.new(UUID.randomUUID()) {
                    this.car = car
                    this.index = it.first
                    this.path = it.second
                } }

                oldPhotos.forEach { File(it.path).delete() }
            }

            return@post call.respond(HttpStatusCode.OK)
        }
    }
}