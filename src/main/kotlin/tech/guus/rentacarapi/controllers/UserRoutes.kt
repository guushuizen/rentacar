package tech.guus.rentacarapi.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.repositories.UserRepository
import tech.guus.rentacarapi.requests.LoginRequest
import java.sql.SQLIntegrityConstraintViolationException
import java.util.UUID


fun Route.userRoutes() {
    val userRepository by inject<UserRepository>()

    route("/users") {
        authenticate("auth-basic") {
            get {
                val user = call.principal<User>()!!
                call.respond(user)
            }
        }

        post {
            val requestBody: CreateUserRequest = call.receive<CreateUserRequest>()

            val user = User(
                uuid = UUID.randomUUID().toString(),
                firstName = requestBody.firstName,
                lastName = requestBody.lastName,
                emailAddress = requestBody.emailAddress,
                password = requestBody.password,
                streetName = requestBody.streetName,
                houseNumber = requestBody.houseNumber,
                postalCode = requestBody.postalCode,
                city = requestBody.city,
                country = requestBody.country,
                longitude = requestBody.longitude,
                latitude = requestBody.latitude
            )

            try {
                userRepository.insert(user)
            } catch (exc: SQLIntegrityConstraintViolationException) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    "Someone has already registered with this e-mail address"
                )
            }

            call.respond(HttpStatusCode.Created)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = userRepository.attemptLogin(request.emailAddress, request.password)

            if (user == null) {
                return@post call.respond(HttpStatusCode.BadRequest)
            } else {
                return@post call.respond(HttpStatusCode.OK)
            }
        }
    }
}