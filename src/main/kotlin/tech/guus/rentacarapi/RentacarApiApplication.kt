package tech.guus.rentacarapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import org.ktorm.jackson.KtormModule
import tech.guus.rentacarapi.routes.userRoutes

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSerialization()
}

fun Application.configureRouting() {
    routing {
        userRoutes()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            registerModule(KtormModule())
        }
    }
}