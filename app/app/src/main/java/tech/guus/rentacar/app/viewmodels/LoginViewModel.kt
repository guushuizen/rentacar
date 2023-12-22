package tech.guus.rentacar.app.viewmodels

import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.focus.FocusManager
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.views.components.Screen

class LoginViewModel(
    private val userRepository: UserRepository,
    private val navController: NavController,
    private val snackbarHostState: SnackbarHostState,
    private val focusManager: FocusManager
) : BaseViewModel() {
    override val screenTitle: String = "Inloggen"

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun updateEmail(email: String) {
        _email.value = email
    }

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun attemptLogin() {
        viewModelScope.launch {
            this@LoginViewModel.focusManager.clearFocus()

            val token = userRepository.login(email.value, password.value)

            if (token == null) {
                this@LoginViewModel.snackbarHostState.showSnackbar(
                    "De door jouw ingevulde gegevens zijn incorrect"
                )

                return@launch
            }

            userRepository.storeToken(token)

            this@LoginViewModel.snackbarHostState.showSnackbar("Succesvol ingelogd")

            this@LoginViewModel.navController.navigate(Screen.Cars.route)
        }
    }
}