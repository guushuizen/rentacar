package tech.guus.rentacarapi.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateCarRequest
import tech.guus.rentacarapi.requests.ListCarResponse
import tech.guus.rentacarapi.requests.UpdateCarRequest
import tech.guus.rentacarapi.services.LicensePlateService
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*


fun Route.carRoutes() {
    route("cars") {
        get {
            val brandNameFilter = call.request.queryParameters["brandName"]
            val modelNameFilter = call.request.queryParameters["modelName"]

            var op = Op.build { Cars.status eq CarStatus.ACTIVE }

            if (brandNameFilter != null) op = op.and(Cars.brandName.upperCase() like brandNameFilter.uppercase())
            if (modelNameFilter != null) op = op.and(Cars.modelName.upperCase() like modelNameFilter.uppercase())

            var currentLongitude = call.request.queryParameters["currentLongitude"]
            var currentLatitude = call.request.queryParameters["currentLatitude"]
            var filterRadius = call.request.queryParameters["filterRadius"]
            val combinedArgs = arrayOf(currentLongitude, currentLatitude, filterRadius)

            if (combinedArgs.all { it != null && it.toFloatOrNull() != null }) {
                val distanceFunc = (CustomFunction<Float>(
                    "SQRT",
                    FloatColumnType(),
                    CustomFunction<Float>(
                        "POWER",
                        FloatColumnType(),
                        CustomFunction<Float>("GREATEST", FloatColumnType(), Users.longitude, floatLiteral(currentLongitude!!.toFloat()))
                                - CustomFunction("LEAST", FloatColumnType(), Users.longitude, floatLiteral(currentLongitude.toFloat())),
                        intLiteral(2)
                    ) +
                            CustomFunction(
                                "POWER",
                                FloatColumnType(),
                                CustomFunction<Float>("GREATEST", FloatColumnType(), Users.latitude, floatLiteral(currentLatitude!!.toFloat()))
                                        - CustomFunction("LEAST", FloatColumnType(), Users.latitude, floatLiteral(currentLatitude.toFloat())),
                                intLiteral(2)
                            )
                ) * floatLiteral(111.32F)) // 1 degree ~= 111.32km
                op = op.and(distanceFunc lessEq floatLiteral(filterRadius!!.toFloat()))
            } else if (combinedArgs.any { it != null }) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "In order to filter on location, you must supply all float-valued `currentLongitude`, " +
                            "`currentLatitude` and `filterRadius` query parameters."
                )
            }

            return@get call.respond(ListCarResponse(transaction {
                return@transaction Cars
                    .innerJoin(Users)
                    .select(op)
                    .map(Car::wrapRow)
                    .map { CarDTO(it) }
            }))
        }

        authenticate {
            post {
                val request = call.receive<CreateCarRequest>()

                val existingCar = transaction {
                    return@transaction Cars.select { Cars.licensePlate eq request.licensePlate }.firstOrNull()
                }

                if (existingCar != null) {
                    return@post call.respond(HttpStatusCode.Conflict)
                }

                val licensePlateInfo =
                    async { LicensePlateService.getLicensePlateDetails(request.licensePlate) }.await()
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
                            if (request.ratePerHour != null) ratePerHour = request.ratePerHour
                        }

                        CarDTO(car)
                    })
                } catch (exc: ExposedSQLException) {
                    if (exc.cause is SQLIntegrityConstraintViolationException || exc.cause is BatchUpdateException) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            "Someone has already registered this license plate."
                        )
                    } else {
                        return@post call.respond(HttpStatusCode.InternalServerError)
                    }
                }
            }

            patch("{carUuid}") {
                val carUuid = call.parameters["carUuid"]
                val user = call.principal<User>()!!
                val car = transaction {
                    Car.find { (Cars.id eq UUID.fromString(carUuid)) and (Cars.ownerUuid eq user.id) }.firstOrNull()
                } ?: return@patch call.respond(HttpStatusCode.Forbidden)

                val requestBody = call.receive<UpdateCarRequest>()

                if ((car.ratePerHour == null && requestBody.ratePerHour == null) && requestBody.status == CarStatus.ACTIVE) {
                    return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        "You can only activate your listing if you've set an hourly rate for your car."
                    )
                }

                transaction {
                    car.status = requestBody.status
                    if (requestBody.ratePerHour != null) car.ratePerHour = requestBody.ratePerHour
                }

                return@patch call.respond(HttpStatusCode.OK)
            }
        }
    }
}
