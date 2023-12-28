package tech.guus.rentacar.app.viewmodels

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tech.guus.rentacar.app.services.LocationService

class RegisterViewModel(
    private val locationService: LocationService,
    private val snackbarHostState: SnackbarHostState,
    private val activity: ComponentActivity
) : BaseViewModel() {

    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()

    private val _emailAddress = MutableStateFlow("")
    val emailAddress = _emailAddress.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _streetName = MutableStateFlow("")
    val streetName = _streetName.asStateFlow()

    private val _houseNumber = MutableStateFlow("")
    val houseNumber = _houseNumber.asStateFlow()

    private val _postcode = MutableStateFlow("")
    val postcode = _postcode.asStateFlow()

    private val _city = MutableStateFlow("")
    val city = _city.asStateFlow()

    private val _country = MutableStateFlow("")
    val country = _country.asStateFlow()

    val countryOptions = mapOf(
        "NL" to "Nederland",
        "BE" to "België",
    )

    fun updateFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun updateLastName(lastName: String) {
        _lastName.value = lastName
    }

    fun updateEmailAddress(email: String) {
        _emailAddress.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun updateStreetName(streetName: String) {
        _streetName.value = streetName
    }

    fun updateHouseNumber(houseNumber: String) {
        _houseNumber.value = houseNumber
    }

    fun updatePostcode(postcode: String) {
        _postcode.value = postcode
    }

    fun updateCity(city: String) {
        _city.value = city
    }

    fun updateCountry(country: String) {
        _country.value = country
    }

    fun determineLocation() = viewModelScope.launch { return@launch withContext(Dispatchers.IO) {
        val coordinates = locationService.getCurrentCoordinates()
            ?: return@withContext

        val locationInformation = locationService.searchAddressByCoordinates(coordinates)
            ?: return@withContext

        _streetName.value = locationInformation.address.road
        _houseNumber.value = locationInformation.address.house_number
        _postcode.value = locationInformation.address.postcode
        _city.value = locationInformation.address.city
        _country.value = locationInformation.address.country_code.uppercase()

        coroutineScope {
            snackbarHostState.showSnackbar(message = "De adresgegevens zijn ingevuld.")
        }
    } }
}