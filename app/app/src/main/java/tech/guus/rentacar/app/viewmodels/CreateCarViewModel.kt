package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.views.components.Screen

class CreateCarViewModel(
    private val myCarRepository: MyCarRepository,
    private val snackbarHostState: SnackbarHostState,
    private val navigationController: NavController,
) : BaseViewModel() {
    private val _licensePlate = MutableStateFlow("")
    val licensePlate = _licensePlate.asStateFlow()

    fun updateLicensePlate(value: String) {
        _licensePlate.update { value }
    }

    fun createCar() = viewModelScope.launch {
        try {
            val createdCar = myCarRepository.createCar(licensePlate.value)

            navigationController.navigate(Screen.EditListing.route.replace("{carUuid}", createdCar.id.toString()))
        } catch (e: Exception) {
            snackbarHostState.showSnackbar(
                e.message ?: "Er is een onbekende fout opgetreden, probeer het later opnieuw."
            )
        }
    }
}