package tech.guus.rentacarapi.models

import io.ktor.server.auth.*
import org.jetbrains.exposed.sql.Table


object Users: Table() {
    val uuid = varchar("uuid", 36)
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val emailAddres = varchar("email_address", 64)
    val password = varchar("password", 128)
    val streetName = varchar("street_name", 64)
    val houseNumber = varchar("house_number", 64)
    val postalCode = varchar("postal_code", 64)
    val city = varchar("city", 64)
    val country = varchar("country", 16)
    val latitude = float("latitude")
    val longitude = float("longitude")
}

data class User(
    val uuid: String,
    val firstName: String,
    val lastName: String,
    val emailAddress: String,
    val password: String,
    val streetName: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: String,
    val latitude: Float,
    val longitude: Float,
) : Principal
