package tech.guus.rentacarapi.requests

import tech.guus.rentacarapi.models.CarDTO
import tech.guus.rentacarapi.models.CarStatus


data class ListCarResponse(
    val cars: List<CarDTO>
)


data class CreateCarRequest(
    val licensePlate: String
)

data class UpdateCarRequest(
    val status: CarStatus,
    val ratePerHour: Int?
)
