package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.UserDTO
import tech.guus.rentacarapi.requests.CreateUserRequest
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*


fun Route.userRoutes() {
    route("/users") {
        authenticate {
            get {
                val user = call.principal<User>()!!
                call.respond(UserDTO(user))
            }
        }

        post {
            val requestBody = call.receive<CreateUserRequest>()

            try {
                val user = transaction {
                    return@transaction User.new(UUID.randomUUID()) {
                        firstName = requestBody.firstName
                        lastName = requestBody.lastName
                        emailAddress = requestBody.emailAddress
                        password = requestBody.password
                        streetName = requestBody.streetName
                        houseNumber = requestBody.houseNumber
                        postalCode = requestBody.postalCode
                        city = requestBody.city
                        country = requestBody.country
                        latitude = requestBody.latitude
                        longitude = requestBody.longitude
                    }
                }

                call.respond(HttpStatusCode.Created, UserDTO(user))
            } catch (e: Exception) {
                val original = (e as? ExposedSQLException)?.cause
                when (original) {
                    is SQLIntegrityConstraintViolationException ->
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            "Someone has already registered with this e-mail address"
                        )
                    is BatchUpdateException ->
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            "Someone has already registered with this e-mail address"
                        )
                    else ->
                        return@post call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}