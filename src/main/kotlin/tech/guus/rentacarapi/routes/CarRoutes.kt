package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.services.LicensePlateService
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*


fun Route.carRoutes() {
    val licensePlateService by inject<LicensePlateService>()
    route("cars") {
        authenticate {
            post {
                val request = call.receive<CreateCarRequest>()

                val existingCar = transaction {
                    return@transaction Cars.select {Cars.licensePlate eq request.licensePlate}.firstOrNull()
                }
                if (existingCar != null) {
                    return@post call.respond(HttpStatusCode.Conflict)
                }

                val licensePlateInfo = licensePlateService.getLicensePlateDetails(request.licensePlate)
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                try {
                    return@post call.respond(HttpStatusCode.Created, transaction {
                        val car = Car.new(UUID.randomUUID()) {
                            owner = call.principal<User>()!!
                            brandName = licensePlateInfo.first.merk
                            modelName = licensePlateInfo.first.handelsbenaming
                            licensePlate = request.licensePlate
                            color = licensePlateInfo.first.eerste_kleur
                            fuelType = FuelType.fromRdwDescription(licensePlateInfo.second.brandstof_omschrijving)
                        }

                        CarDTO(car)
                    })
                } catch (exc: ExposedSQLException) {
                    if (exc.cause is SQLIntegrityConstraintViolationException || exc.cause is BatchUpdateException ) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            "Someone has already registered with this e-mail address"
                        )
                    } else {
                        return@post call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }
        }
    }
}