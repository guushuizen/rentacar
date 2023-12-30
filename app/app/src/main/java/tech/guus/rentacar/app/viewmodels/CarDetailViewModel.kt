package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.views.components.Screen

open class CarDetailViewModel(
    private val carUuid: String,
    val carRepository: CarRepository,
    private val navigationController: NavController,
    private val snackbarHostState: SnackbarHostState,
    private val userRepository: UserRepository,
) : BaseCarViewModel(
    carUuid,
    carRepository,
    navigationController,
    snackbarHostState,
) {

    fun openReservationPopup() {
        if (userRepository.loggedInUser == null) {
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Je moet ingelogd zijn om een auto te reserveren!")
            }

            return
        }

        navigationController.navigate(Screen.CarReservation.route.replace("{carUuid}", carUuid))
    }
}