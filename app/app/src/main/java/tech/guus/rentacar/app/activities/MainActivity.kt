package tech.guus.rentacar.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.AppContainer
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.views.CarListView
import tech.guus.rentacar.app.views.LoginView

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
    val coroutineScope = rememberCoroutineScope()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    return ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RentACar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                    IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Menu sluiten",
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
                NavigationDrawerItem(
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(25.dp)
                            )
                            Text(text = "Alle auto's", modifier = Modifier.padding(start = 7.dp))
                        }
                    },
                    selected = navController.currentDestination?.route == "cars",
                    onClick = { navController.navigate("cars") }
                )
            }
        }) {
        NavHost(navController = navController, startDestination = "cars") {
            composable("cars") { CarListView(
                carListViewModel = CarListViewModel(container.carRepository),
                onMenuButtonClicked = { coroutineScope.launch { drawerState.open() } }
            ) }
            composable("login") { LoginView() }
        }
    }
}