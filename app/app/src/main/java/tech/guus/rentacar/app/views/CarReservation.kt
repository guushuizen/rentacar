package tech.guus.rentacar.app.views

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.CarReservationViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarReservation(
    viewModel: CarReservationViewModel,
    appData: ApplicationData
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
                        text = "Reserveren",
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
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Sluiten"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.reserveCar() }) {
                Icon(Icons.Filled.Check, contentDescription = "Reserveren")
            }
        }
    ) {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxWidth()) {
            Column(modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()) {
                Text(
                    text = "Je gaat de volgende auto reserveren",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Text(
                    text = "${listedCar!!.title()} - ${listedCar!!.licensePlate}",
                    fontSize = 18.sp
                )


                Text(
                    text = listedCar!!.renderPricePerHour(),
                    fontSize = 16.sp,
                    color = Color.Gray,
                )

                val startDateTime = viewModel.startDateTime.collectAsState()

                DateTimePicker(
                    value = startDateTime.value,
                    onValueChange = { value -> viewModel.updateStartDateTime(value) },
                    dateLabel = "Startdatum",
                    timeLabel = "Starttijd",
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                )

                val endDateTime = viewModel.endDateTime.collectAsState()

                DateTimePicker(
                    value = endDateTime.value,
                    onValueChange = { value -> viewModel.updateEndDateTime(value) },
                    dateLabel = "Einddatum",
                    timeLabel = "Eindtijd",
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                )

                Row(modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp)) {
                        Text(
                            text = "Totale tijd",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = viewModel.renderTotalTime(),
                        )
                    }
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)) {
                        Text(
                            text = "Totale prijs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = viewModel.renderTotalPrice(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    value: LocalDateTime,
    onValueChange: (LocalDateTime) -> Unit,
    dateLabel: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = DateTimeFormatter.ofPattern("ccc d MMMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {

        val openDatePickerDialog = remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        )

        Column(modifier = Modifier
            .weight(1f)
            .padding(end = 5.dp)) {
            Text(text = dateLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Box(
                modifier = Modifier
                    .border(1.dp, color = Color.LightGray, shape = RoundedCornerShape(5.dp))
                    .clip(RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .clickable { openDatePickerDialog.value = true }
            ) {
                Text(
                    text = value.format(dateFormatter),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        if (openDatePickerDialog.value) {
            DatePickerDialog(
                onDismissRequest = {
                    openDatePickerDialog.value = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        onValueChange(
                            Instant
                                .ofEpochMilli(datePickerState.selectedDateMillis!!)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .atTime(value.toLocalTime())
                        )
                        openDatePickerDialog.value = false
                    }) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        openDatePickerDialog.value = false
                    }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

        val openTimePickerDialog = remember { mutableStateOf(false) }
        val timePickerState = rememberTimePickerState(
            initialHour = value.hour,
            initialMinute = value.minute,
        )

        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 5.dp)) {
            Text(text = timeLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Box(
                modifier = Modifier
                    .border(1.dp, color = Color.LightGray, shape = RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .clickable { openTimePickerDialog.value = true }
            ) {
                Text(
                    text = value.format(timeFormatter),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        if (openTimePickerDialog.value) {
            DatePickerDialog(
                onDismissRequest = {
                    openTimePickerDialog.value = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        onValueChange(
                            value.toLocalDate().atTime(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                        openTimePickerDialog.value = false
                    }) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        openTimePickerDialog.value = false
                    }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp)
                )
            }
        }
    }
}