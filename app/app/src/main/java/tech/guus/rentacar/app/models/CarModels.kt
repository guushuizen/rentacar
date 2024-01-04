package tech.guus.rentacar.app.models

import androidx.compose.ui.graphics.Color
import tech.guus.rentacar.app.BASE_URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID


data class ListCarResponse(
    val cars: List<ListedCar>
)

data class ReserveCarRequest(
    val startDateTime: String,
    val endDateTime: String
)

data class ReserveCarResponse(
    val startDateTime: String,
    val endDateTime: String,
    val totalPriceInCents: Long,
    val reservedCar: ListedCar
)

enum class CarStatus(val value: String) {
    ACTIVE("ACTIVE"), DRAFT("DRAFT")
}

data class ListedCar(
    val id: UUID,
    val ownerName: String,
    val brandName : String,
    val modelName: String,
    val licensePlate: String,
    val color: String,
    val fuelType: String,
    val ratePerHour: Float?,
    val status: CarStatus,
    val locationLatitude: Float,
    val locationLongitude: Float,
    val locationString: String,
    var photos: List<String>,
    val reservedDates: List<List<String>>  // Should become datetimes at some point.
) {
    fun title(): String {
        return "$brandName $modelName"
    }

    init {
        photos = photos.map {photoPath ->
            "$BASE_URL/${photoPath}"
        }
    }

    fun renderPricePerHour(): String {
        return "€${(ratePerHour ?: 0F).toBigDecimal().setScale(2)}/uur"
    }

    fun humanFuelType(): String {
        when (fuelType) {
            "ICE" -> return "Verbrandingsmotor"
            "FCEV" -> return "Waterstof"
            "BEV" -> return "Elektrisch"
            else -> return "Onbekend"
        }
    }

    fun renderAgenda(): List<DayInAgenda> {
        val dates = mutableMapOf<LocalDate, DayInAgenda>()

        this.reservedDates.map {
            val startInstant = Instant.parse(it[0]).atZone(ZoneOffset.UTC)
            val endInstant = Instant.parse(it[1]).atZone(ZoneOffset.UTC)
            val startDate = startInstant.toLocalDate()
            val endDate = endInstant.toLocalDate()
            val delta = endDate.compareTo(startDate)

            for (i in 0..delta) {
                val currentDate = startDate.plusDays(i.toLong())
                val isFullDayBooking = currentDate.isAfter(startDate) && currentDate.isBefore(endDate)

                val alreadyExistingDay = dates[currentDate]
                if (alreadyExistingDay == null) {
                    dates[currentDate] = DayInAgenda(
                        date = currentDate,
                        state = if (isFullDayBooking) DayState.FULLY_BOOKED else DayState.PARTIALLY_BOOKED,
                        reservations = listOf(startInstant to endInstant)
                    )
                } else {
                    dates[currentDate] = alreadyExistingDay.copy(
                        state = DayState.FULLY_BOOKED, // A car can't have >2 reservations on a single day.
                        reservations = alreadyExistingDay.reservations.plus(startInstant to endInstant)
                    )
                }
            }
        }

        return dates.values.toList().sortedBy { it.date }
    }
}

enum class DayState {
    FULLY_BOOKED, PARTIALLY_BOOKED, EMPTY;

    fun toColor(): Color {
        return when (this) {
            FULLY_BOOKED -> Color(0xFFD4455B)
            PARTIALLY_BOOKED -> Color(0xFFF8C325)
            EMPTY -> Color(0xFF1BAE9F)
        }
    }
}

data class DayInAgenda(
    val date: LocalDate,
    val state: DayState,
    val reservations: List<Pair<ZonedDateTime, ZonedDateTime>>
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
) {
    fun hasNoFilters(): Boolean {
        return chosenBrandName == null && chosenModelName == null && chosenCoordinates == null && chosenRadius == null
    }
}