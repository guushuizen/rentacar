package tech.guus.rentacar.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.AppContainer
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.BaseViewModel
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.LoginView
import tech.guus.rentacar.app.views.MyCarsView
import tech.guus.rentacar.app.views.components.Navigation
import tech.guus.rentacar.app.views.components.NavigationData
import tech.guus.rentacar.app.views.components.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AppContainer()

        setContent {
            RentACarTheme {
                MainComposition(container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposition(
    container: AppContainer
) {
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    return NavHost(navController = navController, startDestination = "cars") {
        val navigationData = NavigationData(
            drawerState = drawerState,
            navigationController = navController,
            userRepository = container.userRepository
        )
        composable("cars") {
            Navigation(navigationData = navigationData, viewTitle = Screen.Cars.title) {
                CarListView(
                    carListViewModel = CarListViewModel(container.carRepository)
                )
            }
        }

//        composable("my-cars") { MyCarsView(container.userRepository) }

        composable(Screen.Login.route) {
            Navigation(navigationData = navigationData, viewTitle = Screen.Login.title) {
                LoginView(
                    viewModel = LoginViewModel()
                )
            }
        }
    }
}

fun getTitleByRoute(route: String?): String {
    return when (route) {
        "cars" -> "Alle auto's"
        "my-cars" -> "Mijn auto's"
        "login" -> "Inloggen"
        else -> "RentACar"
    }
}