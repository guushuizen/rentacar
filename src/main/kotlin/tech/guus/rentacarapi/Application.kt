package tech.guus.rentacarapi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.routes.carPhotoRoutes
import tech.guus.rentacarapi.routes.carRoutes
import tech.guus.rentacarapi.routes.userRoutes
import tech.guus.rentacarapi.services.Database
import java.io.File


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() {
    val config = this.environment.config
    Database.init(config)

    install(Authentication) {
        basic {
            realm = "RentACar"
            validate { credentials ->
                return@validate transaction {
                    User.find {
                        Users.emailAddress eq credentials.name
                        Users.password eq credentials.password
                    }
                        .singleOrNull()
                }
            }
        }
    }

    configureRouting(config)
    configureSerialization()
}
fun Application.configureRouting(config: ApplicationConfig) {
    routing {
        userRoutes()
        carRoutes()

        staticFiles("/uploads", File(config.property("ktor.upload_dir").getString()))
    }
}

fun Application.configureSerialization() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
    install(RequestValidation) {
        validate<CreateCarRequest> { request ->
            if (request.licensePlate.count() != 6) {
                ValidationResult.Invalid("De kentekenplaat is altijd 6 tekens lang.")
            } else {
                ValidationResult.Valid
            }
        }
    }
    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            registerModule(JavaTimeModule())
        }
    }
}