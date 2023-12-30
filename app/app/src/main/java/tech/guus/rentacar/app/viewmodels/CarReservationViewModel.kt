package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.repositories.CarRepository
import java.time.Duration
import java.time.LocalDateTime

class CarReservationViewModel(
    private val carUuid: String,
    val carRepository: CarRepository,
    private val navigationController: NavController,
    private val snackbarHostState: SnackbarHostState,
) : BaseCarViewModel(
    carUuid,
    carRepository,
    navigationController,
    snackbarHostState
) {
    private val _startDateTime = MutableStateFlow(LocalDateTime.now())

    val startDateTime = _startDateTime.asStateFlow()

    private val _endDateTime = MutableStateFlow(LocalDateTime.now().plusHours(1))
    val endDateTime = _endDateTime.asStateFlow()

    fun updateStartDateTime(value: LocalDateTime) = _startDateTime.update { value }

    fun updateEndDateTime(value: LocalDateTime) = _endDateTime.update { value }

    fun renderTotalTime(): String {
        val duration = Duration.between(_startDateTime.value, _endDateTime.value)
        val hours = duration.toHours() % 24
        val days = duration.toDays()

        return if (days > 0 && hours  > 0) {
            "$days dagen, $hours uur"
        } else if (days == 0L && hours > 0) {
            "$hours uur"
        } else if (days > 0 && hours == 0L) {
            "$days dagen"
        } else {
            "0 uur"
        }
    }

    fun renderTotalPrice(): String {
        val duration = Duration.between(_startDateTime.value, _endDateTime.value)
        val price = duration.toHours() * (this.car.value?.ratePerHour ?: 0F)

        return "€${price.toBigDecimal().setScale(2)} euro"
    }

    fun reserveCar() = viewModelScope.launch {
        val startDateTime = _startDateTime.value
        val endDateTime = _endDateTime.value

        val statusMessage = carRepository.reserveCar(carUuid, startDateTime, endDateTime)

        if (statusMessage != null) {
            snackbarHostState.showSnackbar(statusMessage)
        } else {
            navigationController.popBackStack()
            snackbarHostState.showSnackbar(
                message = "Gelukt! Je reservering is gemaakt.",
            )
        }
    }
}
