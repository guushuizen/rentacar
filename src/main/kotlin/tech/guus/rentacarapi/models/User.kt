package tech.guus.rentacarapi.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.server.auth.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID


object Users: IdTable<UUID>() {
    override val id: Column<EntityID<UUID>> = uuid("uuid").entityId()

    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val emailAddress = varchar("email_address", 64)
    val password = varchar("password", 128)
    val streetName = varchar("street_name", 64)
    val houseNumber = varchar("house_number", 64)
    val postalCode = varchar("postal_code", 64)
    val city = varchar("city", 64)
    val country = varchar("country", 16)
    val latitude = float("latitude")
    val longitude = float("longitude")
}

@JsonIgnoreProperties(value = ["Users", "table", "columns", "klass"])
class User(id: EntityID<UUID>): Entity<UUID>(id), Principal {
    companion object : EntityClass<UUID, User>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var emailAddress by Users.emailAddress
    var password by Users.password
    var streetName by Users.streetName
    var houseNumber by Users.houseNumber
    var postalCode by Users.postalCode
    var city by Users.city
    var country by Users.country
    var latitude by Users.latitude
    var longitude by Users.longitude
}

data class UserDTO(
    val uuid: UUID,
    val firstName: String,
    val lastName: String,
    val emailAddress: String,
    val streetName: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: String,
    val latitude: Float,
    val longitude: Float,
) {
    constructor(user: User): this(
        uuid = user.id.value,
        firstName = user.firstName,
        lastName = user.lastName,
        emailAddress = user.emailAddress,
        streetName = user.streetName,
        houseNumber = user.houseNumber,
        postalCode = user.postalCode,
        city = user.city,
        country = user.country,
        latitude = user.latitude,
        longitude = user.longitude
    )
}
