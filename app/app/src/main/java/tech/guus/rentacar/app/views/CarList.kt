package tech.guus.rentacar.app.views

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SnackbarHost
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.Purple40
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.CarListItem
import tech.guus.rentacar.app.views.components.Screen
import kotlin.math.exp

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CarListView(
    carListViewModel: CarListViewModel,
    appData: ApplicationData,
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(text = Screen.Cars.title, modifier = Modifier.padding(10.dp))
                },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { appData.drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { carListViewModel.updateOpenFilterDialog(true) }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Filter")
            }
        }
    ) {
        val cars by carListViewModel.listedCars.collectAsState()
        val loading by carListViewModel.loading.collectAsState()

        if (loading && cars.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        } else {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = loading,
                onRefresh = {
                    carListViewModel.refreshCarList()
                }
            )

            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .padding(it)
                    .fillMaxSize()
            ) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    cars.forEach {
                        item {
                            CarListItem(
                                car = it,
                                onClick = {
                                    appData.navigationController.navigate(
                                        Screen.CarDetails.route.replace("{carUuid}", it.id.toString())
                                    )
                                }
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = loading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    val openFilterDialog by carListViewModel.openFilterDialog.collectAsState()
    if (openFilterDialog) {
        FilterDialog(
            viewModel = carListViewModel
        )
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterDialog(viewModel: CarListViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.updateOpenFilterDialog(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.updateOpenFilterDialog(false);
                    viewModel.refreshCarList()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Purple40)
            ) {
                Text(text = "Filteren", color = Purple40, fontWeight = FontWeight.SemiBold)
            }
        },
        title = { Text("Filter", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                val chosenFilterValues by viewModel.chosenFilterValues.collectAsState()

                var brandNameDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = brandNameDropdownExpanded,
                    onExpandedChange = { brandNameDropdownExpanded = !brandNameDropdownExpanded },
                ) {
                    TextField(
                        readOnly = true,
                        value = chosenFilterValues.chosenBrandName ?: "Geen",
                        onValueChange = { },
                        label = { Text("Merk") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = brandNameDropdownExpanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = brandNameDropdownExpanded,
                        onDismissRequest = {
                            brandNameDropdownExpanded = false
                        }
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.setChosenBrandName(null)
                            brandNameDropdownExpanded = false
                        }) {
                            Text(text = "Geen")
                        }
                        viewModel.availableFilterValues?.availableBrandNames?.forEach { value ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.setChosenBrandName(value)
                                    brandNameDropdownExpanded = false
                                }
                            ) {
                                Text(text = value)
                            }
                        }
                    }
                }

                var modelNameDropdownExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = modelNameDropdownExpanded,
                    onExpandedChange = { modelNameDropdownExpanded = !modelNameDropdownExpanded },
                ) {
                    TextField(
                        readOnly = true,
                        value = chosenFilterValues.chosenModelName ?: "Geen",
                        onValueChange = { },
                        label = { Text("Model") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = modelNameDropdownExpanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = modelNameDropdownExpanded,
                        onDismissRequest = {
                            modelNameDropdownExpanded = false
                        }
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.setChosenModelName(null)
                            modelNameDropdownExpanded = false
                        }) {
                            Text(text = "Geen")
                        }
                        viewModel.availableFilterValues?.availableModelNames?.forEach { value ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.setChosenModelName(value)
                                    modelNameDropdownExpanded = false
                                }
                            ) {
                                Text(text = value)
                            }
                        }
                    }
                }

                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())
                    { granted ->
                        if (granted) {
                            viewModel.determineCurrentLocation()
                        }
                    }

                Text(
                    text = "Afstand en locatie",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 15.dp)
                )

                val loadingLocation by viewModel.loadingLocation.collectAsState()

                val currentLocationString by viewModel.currentLocationString.collectAsState()

                if (currentLocationString == "") {
                    TextButton(
                        onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loadingLocation.not()
                    ) {
                        Text(
                            "Bepaal huidige locatie",
                            color = Purple40,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (loadingLocation)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(top = 10.dp))
                        }
                } else {
                    Text(
                        text = "Huidige locatie",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 5.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(text = currentLocationString, modifier = Modifier.padding(top = 5.dp), textAlign = TextAlign.Center)
                    
                    Text(
                        text = "Maximale afstand tot huurauto",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 15.dp),
                    )
                    Slider(
                        value = chosenFilterValues.chosenRadius?.toFloat() ?: 0F,
                        onValueChange = { viewModel.updateChosenRadius(it) },
                        enabled = true,
                        valueRange = 0F..250F,
                        steps = 250
                    )
                    Text(
                        text = "${chosenFilterValues.chosenRadius ?: 0} km",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}