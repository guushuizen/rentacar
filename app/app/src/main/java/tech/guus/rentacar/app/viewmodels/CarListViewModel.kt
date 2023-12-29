package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
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
import tech.guus.rentacar.app.models.AvailableFilterValues
import tech.guus.rentacar.app.models.ChosenFilterValues
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.services.LocationService

class CarListViewModel(
    private val carRepository: CarRepository,
    private val locationService: LocationService,
    private val snackbarHostState: SnackbarHostState,
) : BaseViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _cars = MutableStateFlow(emptyList<ListedCar>())
    val listedCars: StateFlow<List<ListedCar>> = _cars.asStateFlow()

    private val _openFilterDialog = MutableStateFlow(false)
    val openFilterDialog: StateFlow<Boolean> = _openFilterDialog.asStateFlow()

    var availableFilterValues: AvailableFilterValues? = null

    private val _chosenFilterValues = MutableStateFlow(
        ChosenFilterValues(null, null, null, null)
    )
    val chosenFilterValues: StateFlow<ChosenFilterValues> = _chosenFilterValues.asStateFlow()

    private val _loadingLocation = MutableStateFlow(false)
    val loadingLocation: StateFlow<Boolean> = _loadingLocation.asStateFlow()

    private val _currentLocationString = MutableStateFlow("")
    val currentLocationString: StateFlow<String> = _currentLocationString.asStateFlow()

    init {
        refreshCarList()
    }

    fun refreshCarList() {
        viewModelScope.launch {
            _openFilterDialog.update { false }
            _loading.update { true }

            _cars.update { carRepository.getAllCars(filterValues = chosenFilterValues.value) }

            if (availableFilterValues == null)
                availableFilterValues = AvailableFilterValues(
                    availableBrandNames = _cars.value.map { it.brandName }.distinct(),
                    availableModelNames = _cars.value.map { it.modelName }.distinct(),
                )
            _loading.update { false }
        }
    }

    fun determineCurrentLocation() {
        viewModelScope.launch {
            _loadingLocation.update { true }

            val location = locationService.getCurrentCoordinates()

            if (location == null) {
                this@CarListViewModel.snackbarHostState.showSnackbar(
                    message = "Je locatie kon niet bepaald worden. Staan je instellingen goed?"
                )
                return@launch
            }

            val foundLocation = locationService.searchAddressByCoordinates(location)
            if (foundLocation == null) {
                this@CarListViewModel.snackbarHostState.showSnackbar(
                    message = "Je locatie kon niet bepaald worden. Staan je instellingen goed?"
                )
                return@launch
            }

            _currentLocationString.update { "${foundLocation.address.road} ${foundLocation.address.house_number}, ${foundLocation.address.postcode} ${foundLocation.address.city}, ${foundLocation.address.country_code.uppercase()}" }
            _chosenFilterValues.update { it.copy(chosenCoordinates = location) }
            _loadingLocation.update { false }
        }
    }

    fun updateOpenFilterDialog(value: Boolean) {
        _openFilterDialog.value = value
    }

    fun setChosenBrandName(value: String?) {
        this._chosenFilterValues.value = this._chosenFilterValues.value.copy(chosenBrandName = value)
    }

    fun setChosenModelName(value: String?) {
        this._chosenFilterValues.value = this._chosenFilterValues.value.copy(chosenModelName = value)
    }

    fun updateChosenRadius(value: Float) {
        this._chosenFilterValues.value = this._chosenFilterValues.value.copy(chosenRadius = value.toInt())
    }
}
