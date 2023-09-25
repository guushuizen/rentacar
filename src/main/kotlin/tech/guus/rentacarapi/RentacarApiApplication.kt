package tech.guus.rentacarapi

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.ktorm.jackson.KtormModule
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.koin
import tech.guus.rentacarapi.controllers.userRoutes
import tech.guus.rentacarapi.repositories.UserRepository
import tech.guus.rentacarapi.repositories.UserRepositoryImpl
import tech.guus.rentacarapi.services.DatabaseService

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSerialization()

    koin {
        val databaseService = DatabaseService(environment)
        val dependencyContainer = org.koin.dsl.module {
            single<DatabaseService> { databaseService }
            single<UserRepository> { UserRepositoryImpl(databaseService) }
        }

        modules(dependencyContainer)
    }
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
            registerModule(KtormModule())
        }
    }
}