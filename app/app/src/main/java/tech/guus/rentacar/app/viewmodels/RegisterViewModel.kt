package tech.guus.rentacar.app.viewmodels

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tech.guus.rentacar.app.models.CreateUserRequest
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.services.LocationService
import tech.guus.rentacar.app.views.components.Screen

class RegisterViewModel(
    private val locationService: LocationService,
    private val snackbarHostState: SnackbarHostState,
    private val userRepository: UserRepository,
    private val navigationController: NavController,
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

    // We don't need these properties displayed in the view.
    private var _latitude = 0f
    private var _longitude = 0f

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

    fun determineLocation() = viewModelScope.launch {
        val coordinates = locationService.getCurrentCoordinates()
            ?: return@launch

        val locationInformation = locationService.searchAddressByCoordinates(coordinates)
            ?: return@launch

        _streetName.value = locationInformation.address.road
        _houseNumber.value = locationInformation.address.house_number
        _postcode.value = locationInformation.address.postcode
        _city.value = locationInformation.address.city
        _country.value = locationInformation.address.country_code.uppercase()
        _latitude = coordinates.latitude.toFloat()
        _longitude = coordinates.longitude.toFloat()

        coroutineScope {
            snackbarHostState.showSnackbar(message = "De adresgegevens zijn ingevuld.")
        }
    }

    private fun validateFields(): Boolean {
        return listOf(
                firstName.value,
                lastName.value,
                emailAddress.value,
                password.value,
                streetName.value,
                houseNumber.value,
                postcode.value,
                city.value,
                country.value
            )
        .all { it.isNotBlank() && it.isNotEmpty() }
    }

    fun register() = viewModelScope.launch {
        if (!this@RegisterViewModel.validateFields()) {
            this@RegisterViewModel.snackbarHostState.showSnackbar(
                message = "Niet alle velden zijn juist ingevuld!"
            )
            return@launch
        }

        val success = this@RegisterViewModel.userRepository.registerUser(
            CreateUserRequest(
                firstName = _firstName.value,
                lastName = _lastName.value,
                emailAddress = _emailAddress.value,
                password = _password.value,
                streetName = _streetName.value,
                houseNumber = _houseNumber.value,
                postalCode = _postcode.value,
                city = _city.value,
                country = _country.value,
                latitude = _latitude,
                longitude = _longitude,
            )
        )

        if (success) {
            this@RegisterViewModel.navigationController.navigate(Screen.Cars.route)
            this@RegisterViewModel.snackbarHostState.showSnackbar(
                message = "U bent succesvol geregistreerd en aangemeld!"
            )
        } else {
            this@RegisterViewModel.snackbarHostState.showSnackbar(
                message = "Er is een fout opgetreden bij de registratie, zijn alle velden ingevuld?"
            )
        }
    }
}