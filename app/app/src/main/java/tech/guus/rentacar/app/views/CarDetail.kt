package tech.guus.rentacar.app.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.DayInAgenda
import tech.guus.rentacar.app.models.DayState
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.CarDetailViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarDetailView(
    viewModel: CarDetailViewModel,
    appData: ApplicationData,
) {
    val coroutineScope = rememberCoroutineScope()

    val listedCar by viewModel.car.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(
                        text = listedCar?.title() ?: "Laden...",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { appData.navigationController.popBackStack() } }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openReservationPopup() }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Maak een reservering")
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            if (listedCar == null && loading) {
                return@Scaffold Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                val pagerState = rememberPagerState(pageCount = { listedCar!!.photos.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) { page ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = listedCar!!.photos[page],
                            contentDescription = "Foto van ${listedCar!!.brandName} ${listedCar!!.modelName}",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxHeight()
                        )
                    }
                }

                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

            }

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = listedCar!!.title(), fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    Text(
                        text = "€ ${listedCar!!.ratePerHour?.toBigDecimal()?.setScale(2) ?: 0.00F}/uur",
                        fontSize = 16.sp,
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(top = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Detail(label = "Kleur", value = listedCar!!.color) }
                    item { Detail(label = "Brandstoftype", value = listedCar!!.humanFuelType()) }
                    item { Detail(label = "Locatie", value = listedCar!!.locationString) }
                    item { Detail(label = "Kenteken", value = listedCar!!.licensePlate) }
                }

                Text(text = "Agenda", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 15.dp))

                CarDetailAgenda(agenda = listedCar!!.renderAgenda())
            }
        }
    }
}

@Composable
fun Detail(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Text(text = value, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarDetailAgenda(agenda: List<DayInAgenda>, daysAhead: Int = 14) {
    val pagerState = rememberPagerState { daysAhead }
    HorizontalPager(
        modifier = Modifier.padding(top = 10.dp),
        state = pagerState,
        pageSize = PageSize.Fixed(45.dp)
    ) { currentDateOffset ->
        val currentDate = LocalDate.now().plusDays(currentDateOffset.toLong())
        val dayInAgenda = agenda.firstOrNull { it.date == currentDate }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp)
                .border(
                    width = 2.dp,
                    color = (dayInAgenda?.state ?: DayState.EMPTY).toColor(),
                    shape = RoundedCornerShape(5.dp)
                )
                .clip(RoundedCornerShape(5.dp))
                .background(
                    (dayInAgenda?.state ?: DayState.EMPTY)
                        .toColor()
                        .copy(alpha = 0.25F)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                Text(
                    text = currentDate.dayOfMonth.toString(),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = currentDate.month.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.forLanguageTag("nl")),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}