package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.ReserveCarRequest
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

fun Route.reservationRoutes() {
    post("reserve") {
        val carUuid = call.parameters["carUuid"]
        val user = call.principal<User>()!!
        val requestBody = call.receive<ReserveCarRequest>()

        val car = transaction {
            return@transaction Car.find {
                (Cars.ownerUuid neq user.id)
                    .and(Cars.id eq UUID.fromString(carUuid))
                    .and(Cars.status eq CarStatus.ACTIVE)
                    .and(Cars.ratePerHour neq null)
            }.firstOrNull()
        } ?: return@post call.respond(HttpStatusCode.NotFound, "You can't reserve a car with this ID!")

        val duration = Duration.between(requestBody.startDateTime, requestBody.endDateTime)
        if (duration.isNegative)
            return@post call.respond(HttpStatusCode.BadRequest, "The end date must be before the start date.")

        if (!(duration.toMinutes() >= 60 && duration.toDays() <= 7))
            return@post call.respond(
                HttpStatusCode.BadRequest,
                "You can only reserve for at least one hour and at last one week."
            )

        val conflictingReservations = transaction {
            return@transaction car.reservations.any {
                it.startDateTimeUtc.toInstant(ZoneOffset.UTC) < requestBody.startDateTime
                        && requestBody.endDateTime < it.endDateTimeUtc.toInstant(ZoneOffset.UTC)
            }
        }

        if (conflictingReservations) {
            return@post call.respond(
                HttpStatusCode.Conflict,
                "You can't reserve the car at these dates as there's a conflicting reservation"
            )
        }

        val totalPrice = (car.ratePerHour ?: 0) * duration.toHours() * 100

        return@post call.respond(HttpStatusCode.Created, transaction {
            val reservation = Reservation.new(UUID.randomUUID()) {
                this.startDateTimeUtc = LocalDateTime.ofInstant(requestBody.startDateTime, ZoneOffset.UTC)
                this.endDateTimeUtc = LocalDateTime.ofInstant(requestBody.endDateTime, ZoneOffset.UTC)
                this.car = car
                this.totalPrice = totalPrice.toInt()
                this.rentor = user
            }

            return@transaction ReservationDTO(reservation)
        })
    }
}