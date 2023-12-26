package tech.guus.rentacar.app.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.CarRepository

class CarListViewModel(
    private val carRepository: CarRepository
) : BaseViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _cars = MutableStateFlow(emptyList<ListedCar>())
    val listedCars: StateFlow<List<ListedCar>> = _cars.asStateFlow()

    init {
        refreshCarList()
    }

    fun refreshCarList() {
        viewModelScope.launch {
            _cars.update { carRepository.getAllCars() }
            _loading.update { false }
        }
    }
}
