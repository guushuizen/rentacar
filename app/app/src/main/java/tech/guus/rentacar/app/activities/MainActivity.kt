package tech.guus.rentacar.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.LoginView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RentACarTheme {
                MainComposition()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposition() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    return ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(text = "Modal drawer")
            }
        }) {
        NavHost(navController = navController, startDestination = "cars") {
            composable("cars") { CarListView(
                carListViewModel = CarListViewModel(),
                onMenuButtonClicked = { coroutineScope.launch { drawerState.open() } }
            ) }
            composable("login") { LoginView() }
        }
    }
}