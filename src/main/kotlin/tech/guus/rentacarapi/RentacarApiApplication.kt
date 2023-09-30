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
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.koin
import tech.guus.rentacarapi.controllers.userRoutes
import tech.guus.rentacarapi.repositories.UserRepository
import tech.guus.rentacarapi.repositories.UserRepositoryImpl
import tech.guus.rentacarapi.services.DatabaseService


fun main() {
    embeddedServer(Netty, port = 8080) {
        init(HoconApplicationConfig(ConfigFactory.load("application.conf")))
    }.start(wait = true)
}


fun Application.init(config: HoconApplicationConfig) {
    koin {
        val databaseService = DatabaseService(config)
        val dependencyContainer = org.koin.dsl.module {
            single<DatabaseService> { databaseService }
            single<UserRepository> { UserRepositoryImpl(databaseService) }
        }

        modules(dependencyContainer)
    }

    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the application"
            validate { credentials ->
                val user = this@init.getKoin().get<UserRepository>().attemptLogin(credentials.name, credentials.password)
                user
            }
        }
    }

    configureRouting()
    configureSerialization()
}
fun Application.configureRouting() {
    routing {
        userRoutes()
    }
}

fun Application.configureSerialization() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
    install(RequestValidation)
    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
        }
    }
}