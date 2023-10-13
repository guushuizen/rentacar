package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.UserDTO
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
                call.respond(UserDTO(user))
            }
        }

        post {
            val requestBody: CreateUserRequest = call.receive<CreateUserRequest>()

            try {
                val user = userRepository.insert(requestBody)

                call.respond(HttpStatusCode.Created, UserDTO(user))
            } catch (exc: SQLIntegrityConstraintViolationException) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    "Someone has already registered with this e-mail address"
                )
            }
        }
    }
}