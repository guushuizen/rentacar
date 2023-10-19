package tech.guus.rentacarapi

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.koin
import tech.guus.rentacarapi.repositories.UserRepository
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.routes.carRoutes
import tech.guus.rentacarapi.routes.userRoutes
import tech.guus.rentacarapi.services.DatabaseService
import tech.guus.rentacarapi.services.LicensePlateService


fun main() {
    embeddedServer(Netty, port = 8080) {
        init(HoconApplicationConfig(ConfigFactory.load("application.conf")))
    }.start(wait = true)
}


fun Application.init(config: HoconApplicationConfig) {
    val userRepository = UserRepository()
    DatabaseService.init(config)
    koin {
        val dependencyContainer = org.koin.dsl.module {
            single { userRepository }
            single { LicensePlateService() }
        }

        modules(dependencyContainer)
    }

    install(Authentication) {
        basic {
            realm = "Access to the application"
            validate { credentials ->
                userRepository.attemptLogin(credentials.name, credentials.password)
            }
        }
    }

    configureRouting()
    configureSerialization()
}
fun Application.configureRouting() {
    routing {
        userRoutes()
        carRoutes()
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
        }
    }
}