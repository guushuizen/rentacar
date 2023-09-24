package tech.guus.rentacarapi.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tech.guus.rentacarapi.models.User

fun Route.userRoutes() {
    route("/users") {
        get {
            call.respond(User {
                firstName = "foo"
                lastName = "bar"
            })
        }
        post {
            call.respondText("foo")
        }
    }
}