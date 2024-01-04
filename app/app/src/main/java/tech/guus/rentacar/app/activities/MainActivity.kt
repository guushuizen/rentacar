package tech.guus.rentacar.app.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import tech.guus.rentacar.app.AppContainer
import tech.guus.rentacar.app.AppContainerInterface
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.CarDetailViewModel
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.viewmodels.CarReservationViewModel
import tech.guus.rentacar.app.viewmodels.CreateCarViewModel
import tech.guus.rentacar.app.viewmodels.EditListingViewModel
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.viewmodels.MyCarListViewModel
import tech.guus.rentacar.app.viewmodels.RegisterViewModel
import tech.guus.rentacar.app.views.CarDetailView
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.CarReservation
import tech.guus.rentacar.app.views.CreateCarView
import tech.guus.rentacar.app.views.EditListing
import tech.guus.rentacar.app.views.LoginView
import tech.guus.rentacar.app.views.MyCarsView
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


@Composable
fun MainComposition(
    container: AppContainerInterface,
    startDestination: String = "cars",
    content: @Composable ((ApplicationData) -> Unit)? = null,  // Used for UI testing
) {
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val snackbarHostState = remember { SnackbarHostState() }

    val applicationData = ApplicationData(
        drawerState = drawerState,
        navigationController = navController,
        snackbarHostState = snackbarHostState,
    )

    if (content != null) {
        ApplicationWrapper(appData = applicationData, userRepository = container.userRepository) {
            content(applicationData)
        }
        return
    }

    return NavHost(navController = navController, startDestination = startDestination) {
        composable("cars") {
            ApplicationWrapper(appData = applicationData, container.userRepository) {
                CarListView(
                    carListViewModel = viewModel {
                        CarListViewModel(
                            carRepository = container.carRepository,
                            locationService = container.locationService,
                            snackbarHostState = applicationData.snackbarHostState,
                        )
                    },
                    appData = applicationData
                )
            }
        }

        composable(Screen.Login.route) {
            ApplicationWrapper(appData = applicationData, container.userRepository) {
                val focusManager = LocalFocusManager.current
                LoginView(
                    onClickRegistration = { navController.navigate(Screen.Register.route) },
                    appData = applicationData,
                    viewModel = viewModel {
                        LoginViewModel(
                            userRepository = container.userRepository,
                            navController = navController,
                            snackbarHostState = applicationData.snackbarHostState,
                            focusManager = focusManager,
                        )
                    }
                )
            }
        }

        composable(Screen.Register.route) {
            ApplicationWrapper(appData = applicationData, container.userRepository) {
                RegisterView(
                    viewModel = viewModel {
                        RegisterViewModel(
                            snackbarHostState = applicationData.snackbarHostState,
                            locationService = container.locationService,
                            userRepository = container.userRepository,
                            navigationController = applicationData.navigationController,
                        )
                    },
                    appData = applicationData,
                )
            }
        }

        composable(Screen.CarDetails.route) {
            val carUuid = it.arguments?.getString("carUuid") ?: return@composable

            ApplicationWrapper(appData = applicationData, container.userRepository) {
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

            ApplicationWrapper(appData = applicationData, container.userRepository) {
                CarReservation(
                    viewModel = viewModel {
                        CarReservationViewModel(
                            carUuid = carUuid,
                            carRepository = container.carRepository,
                            navigationController = navController,
                            snackbarHostState = applicationData.snackbarHostState
                        )
                    },
                    appData = applicationData
                )
            }
        }

        composable(Screen.MyCars.route) {
            ApplicationWrapper(appData = applicationData, container.userRepository) {
                MyCarsView(
                    myCarListViewModel = viewModel {
                        MyCarListViewModel(
                            myCarRepository = container.myCarRepository,
                            navigationController = navController,
                            snackbarHostState = applicationData.snackbarHostState,
                            userRepository = container.userRepository,
                        )
                    },
                    appData = applicationData
                )
            }
        }

        composable(Screen.CreateCar.route) {
            ApplicationWrapper(appData = applicationData, container.userRepository) {
                CreateCarView(
                    viewModel = viewModel {
                        CreateCarViewModel(
                            myCarRepository = container.myCarRepository,
                            navigationController = navController,
                            snackbarHostState = applicationData.snackbarHostState,
                        )
                    },
                    appData = applicationData
                )
            }
        }

        composable(Screen.EditListing.route) {
            val carUuid = it.arguments?.getString("carUuid") ?: return@composable

            ApplicationWrapper(appData = applicationData, container.userRepository) {
                EditListing(
                    viewModel = viewModel {
                        EditListingViewModel(
                            carUuid = carUuid,
                            myCarRepository = container.myCarRepository,
                            snackbarHostState = snackbarHostState,
                            navigationController = navController,
                        )
                    },
                    appData = applicationData
                )
            }
        }

    }
}
