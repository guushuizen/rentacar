package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.views.components.Screen
import java.util.UUID

class MyCarListViewModel(
    private val myCarRepository: MyCarRepository,
    private val userRepository: UserRepository,
    private val navigationController: NavController,
    private val snackbarHostState: SnackbarHostState,
) : BaseViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _cars = MutableStateFlow<List<ListedCar>>(emptyList())
    val cars = _cars.asStateFlow()

    init {
        if (userRepository.loggedInUser == null) {
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Je moet ingelogd zijn om deze pagina te bekijken")
            }
            navigationController.navigate(Screen.Cars.route)
        } else {
            refreshCarList()
        }
    }

    fun refreshCarList() = viewModelScope.launch {
        _cars.value = myCarRepository.getCars()
        _loading.update { false }
    }

    fun openAddCarPopup() {
        navigationController.navigate(route = Screen.CreateCar.route)
    }

    fun openEditCarListing(carUuid: UUID) {
        navigationController.navigate(route = Screen.EditListing.route.replace("{carUuid}", carUuid.toString()))
    }
}