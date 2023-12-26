package tech.guus.rentacar.app.models

data class Coordinates(val latitude: Double, val longitude: Double)

data class OpenStreetMapLocationInformation(
    val place_id: String,
    val lat: Double,
    val lon: Double,
    val address: OpenStreetMapLocationAddress
)

data class OpenStreetMapLocationAddress(
    val house_number: String,
    val road: String,
    val city: String,
    val country_code: String,
    val postcode: String,
)