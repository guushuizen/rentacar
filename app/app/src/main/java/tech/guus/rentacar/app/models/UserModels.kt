package tech.guus.rentacar.app.models

import java.util.UUID

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
)


data class LoginRequest(
    val emailAddress: String,
    val password: String
)

data class LoginResponse(
    val token: String
)