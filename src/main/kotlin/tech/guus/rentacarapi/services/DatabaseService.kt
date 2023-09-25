package tech.guus.rentacarapi.services

import org.ktorm.database.Database
import io.ktor.server.application.*
import tech.guus.rentacarapi.models.User

class DatabaseService(environment: ApplicationEnvironment) {
    var database: Database = Database.connect(
        url =  environment.config.propertyOrNull("ktor.database.url")!!.getString(),
        driver = environment.config.propertyOrNull("ktor.database.driver")!!.getString(),
        user = environment.config.propertyOrNull("ktor.database.username")!!.getString(),
        password = environment.config.propertyOrNull("ktor.database.password")!!.getString(),
    )

}