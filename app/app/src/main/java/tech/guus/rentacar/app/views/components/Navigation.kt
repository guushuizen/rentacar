@file:OptIn(ExperimentalMaterial3Api::class)

package tech.guus.rentacar.app.views.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.repositories.UserRepository


sealed class Screen(val route: String, val title: String) {
    object Cars : Screen("cars", "Alle auto's")
    object MyCars : Screen("my-cars", "Mijn auto's")
    object Login : Screen("login", "Inloggen")
}


data class NavigationData(
    val drawerState: DrawerState,
    val navigationController: NavController,
    val userRepository: UserRepository
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(navigationData: NavigationData, viewTitle: String, content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    return ModalNavigationDrawer(
        drawerState = navigationData.drawerState,
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
                    IconButton(onClick = { coroutineScope.launch { navigationData.drawerState.close() } }) {
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
                    selected = navigationData.navigationController.currentBackStackEntry?.destination?.route == "cars",
                    onClick = {
                        navigationData.navigationController.navigate("cars")
                        coroutineScope.launch { navigationData.drawerState.close() }
                    }
                )

                if (navigationData.userRepository.loggedInUser != null) {
                    NavigationDrawerItem(
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Menu,
                                    contentDescription = "Mijn auto's",
                                    modifier = Modifier.size(25.dp)
                                )
                                Text(
                                    text = "Mijn auto's",
                                    modifier = Modifier.padding(start = 7.dp)
                                )
                            }
                        },
                        selected = navigationData.navigationController.currentDestination?.route == "my-cars",
                        onClick = {
                            navigationData.navigationController.navigate("my-cars")
                            coroutineScope.launch { navigationData.drawerState.close() }
                        }
                    )
                }

                NavigationDrawerItem(
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Inloggen",
                                modifier = Modifier.size(25.dp)
                            )
                            Text(text = "Inloggen", modifier = Modifier.padding(start = 7.dp))
                        }
                    },
                    selected = navigationData.navigationController.currentBackStackEntry?.destination?.route == "login",
                    onClick = {
                        navigationData.navigationController.navigate("login")
                        coroutineScope.launch { navigationData.drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = viewTitle, modifier = Modifier.padding(10.dp))
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { navigationData.drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    }
}