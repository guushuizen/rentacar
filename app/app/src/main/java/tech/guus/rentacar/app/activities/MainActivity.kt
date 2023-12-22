package tech.guus.rentacar.app.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tech.guus.rentacar.app.AppContainer
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.LoginView
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.ApplicationWrapper
import tech.guus.rentacar.app.views.components.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AppContainer(this.applicationContext.dataStore)

        runBlocking { container.userRepository.attemptCachedLogin() }

        setContent {
            RentACarTheme {
                MainComposition(container)
            }
        }
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposition(
    container: AppContainer
) {

    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val snackbarHostState = remember { SnackbarHostState() }

    return NavHost(navController = navController, startDestination = "cars") {
        val applicationData = ApplicationData(
            drawerState = drawerState,
            navigationController = navController,
            userRepository = container.userRepository,
            snackbarHostState = snackbarHostState,
        )
        composable("cars") {
            ApplicationWrapper(appData = applicationData, viewTitle = Screen.Cars.title) {
                CarListView(
                    carListViewModel = CarListViewModel(container.carRepository)
                )
            }
        }

        composable(Screen.Login.route) {
            ApplicationWrapper(appData = applicationData, viewTitle = Screen.Login.title) {
                LoginView(
                    onClickRegistration = { navController.navigate(Screen.Register.route) },
                    viewModel = LoginViewModel(
                        userRepository = container.userRepository,
                        navController = navController,
                        snackbarHostState = applicationData.snackbarHostState,
                        focusManager = LocalFocusManager.current,
                    )
                )
            }
        }

        composable(Screen.Register.route) {
            ApplicationWrapper(appData = applicationData, viewTitle = Screen.Register.title) {
                Text(text = "Register")
            }
        }
    }
}
