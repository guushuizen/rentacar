package tech.guus.rentacar.app.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.CarStatus
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.ui.theme.Purple40
import tech.guus.rentacar.app.ui.theme.Red40
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.MyCarListViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.CarListItem
import tech.guus.rentacar.app.views.components.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyCarsView(
    myCarListViewModel: MyCarListViewModel,
    appData: ApplicationData,
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(
                        text = Screen.MyCars.title,
                        modifier = Modifier
                            .testTag("home-topbar")
                            .padding(10.dp)
                    )
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
            FloatingActionButton(onClick = { myCarListViewModel.openAddCarPopup() }) {
                Icon(Icons.Filled.Add, contentDescription = "Nieuwe auto")
            }
        }
    ) {
        val cars by myCarListViewModel.cars.collectAsState()
        val loading by myCarListViewModel.loading.collectAsState()

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
                    myCarListViewModel.refreshCarList()
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
                            MyCarListItem(
                                car = it,
                                onClickActivate = { myCarListViewModel.openEditCarListing(it.id) },
                            )
                        }
                    }

                    if (cars.isEmpty()) {
                        item {
                            Text(
                                text = "Je hebt nog geen auto's toegevoegd!",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
}

@Composable
fun MyCarListItem(
    car: ListedCar,
    onClickActivate: () -> Unit,
) {
    return Box(
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                AsyncImage(
                    model = if (car.photos.isNotEmpty()) car.photos[0] else "https://placehold.it/400x200",
                    contentDescription = "Photo of a ${car.brandName} ${car.modelName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(200.dp)
                )

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row {
                        Text(
                            fontWeight = FontWeight.Bold,
                            text = "${car.brandName} ${car.modelName}",
                            textAlign = TextAlign.Start,
                        )

                        Text(
                            text = "€${(car.ratePerHour ?: 0F).toBigDecimal().setScale(2)}/uur",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Button(
                            onClick = onClickActivate,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Purple40, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(color = Color.White, text = "Bewerken")
                        }
                    }
                }
            }
        }
    }
}