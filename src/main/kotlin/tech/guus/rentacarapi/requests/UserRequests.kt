package tech.guus.rentacarapi.requests

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
