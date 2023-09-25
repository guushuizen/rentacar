package tech.guus.rentacarapi.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.float
import org.ktorm.schema.long
import org.ktorm.schema.varchar


val Database.users get() = this.sequenceOf(Users)


object Users : Table<User>("users") {
    val uuid = varchar("uuid").primaryKey().bindTo { it.uuid }
    val firstName = varchar("first_name").bindTo { it.firstName }
    val lastName = varchar("last_name").bindTo { it.lastName }
    val emailAddres = varchar("email_address").bindTo { it.emailAddress }
    val password = varchar("password").bindTo { it.password }
    val streetName = varchar("street_name").bindTo { it.streetName }
    val houseNumber = varchar("house_number").bindTo { it.houseNumber }
    val postalCode = varchar("postal_code").bindTo { it.postalCode }
    val city = varchar("city").bindTo { it.city }
    val country = varchar("country").bindTo { it.country }
    val latitude = float("latitude").bindTo { it.latitude }
    val longitude = float("longitude").bindTo { it.longitude }
}

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var uuid: String
    var firstName: String
    var lastName: String
    var emailAddress: String
    var password: String
    var streetName: String
    var houseNumber: String
    var postalCode: String
    var city: String
    var country: String
    var latitude: Float
    var longitude: Float
}


data class CreateUserRequest(
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
    val longitude: Float
)
