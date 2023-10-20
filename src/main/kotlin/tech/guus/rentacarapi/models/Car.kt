package tech.guus.rentacarapi.models

import io.ktor.server.auth.*
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import tech.guus.rentacarapi.models.User.Companion.referrersOn
import java.util.*

enum class FuelType(val internalName: String) {
    ICE("internal_combustion_engine"),
    BEV("battery_electric_vehicle"),
    FCEV("fuel_cell_electric_vehicle");

    companion object {
        fun fromRdwDescription(description: String): FuelType {
            return when (description) {
                "Elektriciteit" -> BEV
                "Waterstof" -> FCEV
                else -> ICE
            }
        }
    }
}

enum class CarStatus(val internalName: String) {
    ACTIVE("active"),
    DRAFT("draft")
}


class Car(id: EntityID<UUID>): Entity<UUID>(id) {
    companion object : EntityClass<UUID, Car>(Cars)

    var owner by User referencedOn Cars.ownerUuid
    var brandName by Cars.brandName
    var modelName by Cars.modelName
    var licensePlate by Cars.licensePlate
    var color by Cars.color
    var fuelType by Cars.fuelType
    var ratePerHour by Cars.ratePerHour
    var status by Cars.status

    val photos by CarPhoto referrersOn CarPhotos.carUuid
}


object Cars: UUIDTable() {
    val ownerUuid = reference("owner_uuid", Users)
    val brandName = varchar("brand_name", 64)
    val modelName = varchar("model_name", 64)
    val licensePlate = varchar("license_plate", 6).uniqueIndex()
    val color = varchar("color", 32)
    val fuelType = enumerationByName<FuelType>("fuel_type", 4)
    val ratePerHour = integer("rate_per_hour").nullable()
    val status = enumerationByName<CarStatus>("status", 16).default(CarStatus.DRAFT)
}

data class CarDTO(
    val ownerName: String,
    val brandName: String,
    val modelName: String,
    val licensePlate: String,
    val color: String,
    val fuelType: FuelType,
    val ratePerHour: Int?,
    val status: CarStatus,
    val locationLatitude: Float,
    val locationLongitude: Float,
    val photos: List<String>,
) {
    constructor(car: Car): this(
        ownerName = car.owner.firstName + " " + car.owner.lastName,
        brandName = car.brandName,
        modelName = car.modelName,
        licensePlate = car.licensePlate,
        color = car.color,
        fuelType = car.fuelType,
        ratePerHour = car.ratePerHour,
        status = car.status,
        locationLatitude = car.owner.latitude,
        locationLongitude = car.owner.longitude,
        photos = car.photos.map { "uploads/${it.path}" }
    )
}