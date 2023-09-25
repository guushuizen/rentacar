package tech.guus.rentacarapi.controllers

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.models.CreateUserRequest
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.repositories.UserRepository
import java.util.UUID


fun Route.userRoutes() {
    val userService by inject<UserRepository>()

    route("/users") {
        get {
            call.respond(userService.getAll())
        }
        get("/{uuid?}") {
            val uuid = call.parameters["uuid"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )

            val user = userService.findOne(uuid) ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(user)
        }
        post {
            val requestBody: CreateUserRequest = call.receive<CreateUserRequest>()

            val user = User {
                uuid = UUID.randomUUID().toString()
                firstName = requestBody.firstName
                lastName = requestBody.lastName
                emailAddress = requestBody.emailAddress
                password = requestBody.password
                streetName = requestBody.streetName
                houseNumber = requestBody.houseNumber
                postalCode = requestBody.postalCode
                city = requestBody.city
                country = requestBody.country
                longitude = requestBody.longitude
                latitude = requestBody.latitude
            }

            try {
                userService.insert(user)
            } catch (exc: MySQLIntegrityConstraintViolationException) {
                return@post call.respond(HttpStatusCode.Conflict, "Someone has already registered with this e-mail address")
            }

            call.respond(user)
        }
    }
}