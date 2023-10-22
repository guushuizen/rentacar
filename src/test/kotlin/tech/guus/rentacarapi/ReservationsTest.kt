package tech.guus.rentacarapi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.CarStatus
import tech.guus.rentacarapi.models.Reservation
import tech.guus.rentacarapi.models.Reservations
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.requests.ListCarResponse
import tech.guus.rentacarapi.requests.ReserveCarRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReservationsTest {

    @Test
    fun testReserveACar() = setupTestApplicationWithUser {
        val carToReserve = createDummyCar {
            status = CarStatus.ACTIVE
            ratePerHour = 10
        }

        val userToReserveWith = createUser(createUnauthenticatedClient(), CreateUserRequest(
            firstName = "Guus 2",
            lastName = "Huizen",
            emailAddress = "foo@bar.baz",
            password = "bar",
            streetName = "Hogeschoollaan",
            houseNumber = "1",
            city = "Breda",
            country = "NL",
            postalCode = "6512BN",
            latitude = 5.8428F,
            longitude = 51.8449F
        ))

        val customClient = createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                }
            }

            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "foo@bar.baz", password = "bar")
                    }
                }
            }
        }

        val now = Clock.System.now()
        val response = customClient.post("/cars/${carToReserve.id}/reserve") {
            contentType(ContentType.Application.Json)
            setBody(
                ReserveCarRequest(
                    startDateTime = now.plus(24, DateTimeUnit.HOUR).toJavaInstant(),
                    endDateTime = now.plus(48, DateTimeUnit.HOUR).toJavaInstant()
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val reservation = transaction {
            return@transaction Reservation.find {
                Reservations.rentorUuid eq userToReserveWith.uuid
            }.first()
        }

        assertEquals(24 * carToReserve.ratePerHour!! * 100, reservation.totalPrice)

        val carResponse = customClient.get("/cars").body<ListCarResponse>()
        val reservedCar = carResponse.cars.first { it.id == carToReserve.id.value }
        assertEquals(1, reservedCar.reservedDates.count())

        assertEquals(HttpStatusCode.Conflict, customClient.post("/cars/${carToReserve.id}/reserve") {
            contentType(ContentType.Application.Json)
            setBody(
                ReserveCarRequest(
                    startDateTime = now.plus(36, DateTimeUnit.HOUR).toJavaInstant(),
                    endDateTime = now.plus(40, DateTimeUnit.HOUR).toJavaInstant()
                )
            )
        }.status)
    }
}