package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.views.components.Screen

open class BaseCarViewModel(
    private val carUuid: String,
    private val carRepository: CarRepository,
    private val navigationController: NavController,
    private val snackbarHostState: SnackbarHostState,
) : BaseViewModel() {
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _car = MutableStateFlow<ListedCar?>(null)
    val car: StateFlow<ListedCar?> = _car.asStateFlow()

    init {
        retrieveCarDetails()
    }

    private fun retrieveCarDetails() = viewModelScope.launch {
        _loading.update { true }

        val foundCar = carRepository.getCar(carUuid)

        if (foundCar == null) {
            navigateAway()
            return@launch
        }

        _car.update { foundCar }
        _loading.update { false }
    }

    suspend fun navigateAway() {
        navigationController.navigate(Screen.Cars.route)
        snackbarHostState.showSnackbar("De auto kon niet gevonden worden!")
    }
}