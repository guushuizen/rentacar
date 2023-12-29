package tech.guus.rentacar.app.models

import java.util.UUID


data class ListCarResponse(
    val cars: List<ListedCar>
)

data class ListedCar(
    val id: UUID,
    val ownerName: String,
    val brandName : String,
    val modelName: String,
    val licensePlate: String,
    val color: String,
    val fuelType: String,
    val ratePerHour: Float?,
    val status: String,
    val locationLatitude: Float,
    val locationLongitude: Float,
    val photos: List<String>,
    val reservedDates: List<Pair<String, String>>  // Should become datetimes at some point.
)


data class AvailableFilterValues(
    val availableBrandNames: List<String>,
    val availableModelNames: List<String>,
)

data class ChosenFilterValues(
    val chosenBrandName: String?,
    val chosenModelName: String?,
    val chosenCoordinates: Coordinates?,
    val chosenRadius: Int?
)