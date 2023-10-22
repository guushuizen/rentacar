package tech.guus.rentacarapi.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

class Reservation(id: EntityID<UUID>): Entity<UUID>(id) {
    companion object : EntityClass<UUID, Reservation>(Reservations)

    var car by Car referencedOn Reservations.carUuid
    var startDateTimeUtc by Reservations.startDateTimeUtc
    var endDateTimeUtc by Reservations.endDateTimeUtc
    var totalPrice by Reservations.totalPrice
    var rentor by User referencedOn Reservations.rentorUuid
}

object Reservations : UUIDTable() {
    val carUuid = reference("car_uuid", Cars)
    val rentorUuid = reference("rentor_uuid", Users)
    val startDateTimeUtc = datetime("start_date_time_utc")
    val endDateTimeUtc = datetime("end_date_time_utc")
    val totalPrice = integer("total_price")
}

data class ReservationDTO(
    val startDateTime: String,
    val endDateTime: String,
    val totalPriceInCents: Int,
    val reservedCar: CarDTO
) {
    constructor(reservation: Reservation): this(
        startDateTime = reservation.startDateTimeUtc.toString(),
        endDateTime = reservation.endDateTimeUtc.toString(),
        totalPriceInCents = reservation.totalPrice,
        reservedCar = CarDTO(reservation.car)
    )
}