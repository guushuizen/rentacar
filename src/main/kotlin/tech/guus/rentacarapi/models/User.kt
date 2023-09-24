package tech.guus.rentacarapi.models

import org.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    val uuid: String
    var firstName: String
    var lastName: String
    var emailAddress: String
    var password: String
    var streetName: String
    var houseNumber: String
    var postalCode: String
    var city: String
    var country: String
    var latitude: String
    var longitude: String
}
