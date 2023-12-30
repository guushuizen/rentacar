package tech.guus.rentacar.app.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import tech.guus.rentacar.app.AppContainer
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.CarDetailViewModel
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.viewmodels.CarReservationViewModel
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.viewmodels.RegisterViewModel
import tech.guus.rentacar.app.views.CarDetailView
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.CarReservation
import tech.guus.rentacar.app.views.LoginView
import tech.guus.rentacar.app.views.RegisterView
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.ApplicationWrapper
import tech.guus.rentacar.app.views.components.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AppContainer(this)

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
            ApplicationWrapper(appData = applicationData) {
                CarListView(
                    carListViewModel = CarListViewModel(
                        carRepository = container.carRepository,
                        locationService = container.locationService,
                        snackbarHostState = applicationData.snackbarHostState,
                    ),
                    appData = applicationData
                )
            }
        }

        composable(Screen.Login.route) {
            ApplicationWrapper(appData = applicationData) {
                LoginView(
                    onClickRegistration = { navController.navigate(Screen.Register.route) },
                    appData = applicationData,
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
            ApplicationWrapper(appData = applicationData) {
                RegisterView(
                    viewModel = RegisterViewModel(
                        snackbarHostState = applicationData.snackbarHostState,
                        locationService = container.locationService,
                        userRepository = applicationData.userRepository,
                        navigationController = applicationData.navigationController,
                    ),
                    appData = applicationData,
                )
            }
        }

        composable(Screen.CarDetails.route) {
            val carUuid = it.arguments?.getString("carUuid") ?: return@composable

            ApplicationWrapper(appData = applicationData) {
                CarDetailView(
                    viewModel = CarDetailViewModel(
                        carUuid = carUuid,
                        carRepository = container.carRepository,
                        navigationController = navController,
                        snackbarHostState = applicationData.snackbarHostState,
                        userRepository = container.userRepository,
                    ),
                    appData = applicationData
                )
            }
        }

        composable(Screen.CarReservation.route) {
            val carUuid = it.arguments?.getString("carUuid") ?: return@composable

                ApplicationWrapper(appData = applicationData) {
                    CarReservation(
                        viewModel = CarReservationViewModel(
                            carUuid = carUuid,
                            carRepository = container.carRepository,
                            navigationController = navController,
                            snackbarHostState = applicationData.snackbarHostState
                        ),
                        appData = applicationData
                    )
                }
        }
    }
}
