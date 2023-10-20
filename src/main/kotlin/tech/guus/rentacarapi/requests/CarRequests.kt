package tech.guus.rentacarapi.requests

import tech.guus.rentacarapi.models.CarStatus


data class CreateCarRequest(
    var licensePlate: String
)

data class UpdateCarRequest(
    var status: CarStatus,
    var ratePerHour: Int?
)
