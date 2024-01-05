package tech.guus.rentacar.app

import org.junit.Test
import tech.guus.rentacar.app.models.CarStatus
import tech.guus.rentacar.app.models.DayInAgenda
import tech.guus.rentacar.app.models.DayState
import tech.guus.rentacar.app.models.ListedCar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals

class CarModelsTest {

    private fun dummyCar(): ListedCar {
        return ListedCar(
            id = UUID.randomUUID(),
            ownerName = "",
            brandName = "",
            modelName = "",
            licensePlate = "",
            color = "",
            fuelType = "",
            ratePerHour = 0f,
            status = CarStatus.ACTIVE,
            locationLatitude = 0f,
            locationLongitude = 0f,
            locationString = "",
            photos = emptyList(),
            reservedDates = emptyList()
        )
    }

    @Test
    fun `render agenda with one reservation`() {
        val reservation = listOf("2024-01-02T10:34:53Z", "2024-01-05T11:34:53Z")
        val dummyCar = dummyCar().copy(
            reservedDates = listOf(reservation)
        )

        val parsedReservation =
            Instant.parse(reservation[0]).atZone(ZoneOffset.UTC) to
                    Instant.parse(reservation[1]).atZone(ZoneOffset.UTC)

        assertEquals(listOf(
            DayInAgenda(date = LocalDate.parse("2024-01-02"), state = DayState.PARTIALLY_BOOKED, reservations = listOf(parsedReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-03"), state = DayState.FULLY_BOOKED, reservations = listOf(parsedReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-04"), state = DayState.FULLY_BOOKED, reservations = listOf(parsedReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-05"), state = DayState.PARTIALLY_BOOKED, reservations = listOf(parsedReservation)),
        ), dummyCar.renderAgenda())
    }

    @Test
    fun `render agenda with two reservations ending and starting on the same day`() {
        val firstReservation = listOf("2024-01-02T10:34:53Z", "2024-01-04T11:34:53Z")
        val secondReservation = listOf("2024-01-04T12:34:53Z", "2024-01-06T11:34:53Z")
        val dummyCar = dummyCar().copy(
            reservedDates = listOf(firstReservation, secondReservation)
        )

        val parsedFirstReservation = Instant.parse(firstReservation[0]).atZone(ZoneOffset.UTC) to Instant.parse(firstReservation[1]).atZone(ZoneOffset.UTC)
        val parsedSecondReservation = Instant.parse(secondReservation[0]).atZone(ZoneOffset.UTC) to Instant.parse(secondReservation[1]).atZone(ZoneOffset.UTC)

        assertEquals(listOf(
            DayInAgenda(date = LocalDate.parse("2024-01-02"), state = DayState.PARTIALLY_BOOKED, reservations = listOf(parsedFirstReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-03"), state = DayState.FULLY_BOOKED, reservations = listOf(parsedFirstReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-04"), state = DayState.FULLY_BOOKED, reservations = listOf(parsedFirstReservation, parsedSecondReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-05"), state = DayState.FULLY_BOOKED, reservations = listOf(parsedSecondReservation)),
            DayInAgenda(date = LocalDate.parse("2024-01-06"), state = DayState.PARTIALLY_BOOKED, reservations = listOf(parsedSecondReservation)),
        ), dummyCar.renderAgenda())
    }
}