package tech.guus.rentacar.app.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.CreateCarViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCarView(
    viewModel: CreateCarViewModel,
    appData: ApplicationData,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(
                        text = Screen.CreateCar.title,
                        modifier = Modifier.padding(10.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { appData.navigationController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Sluiten"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createCar() }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Aanmaken")
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(10.dp)) {
                val licensePlate by viewModel.licensePlate.collectAsState()

                Text(
                    text = "Kenteken",
                    fontWeight = FontWeight.Bold
                )
                TextField(
                    value = licensePlate,
                    onValueChange = { value -> viewModel.updateLicensePlate(value.uppercase()) },
                    textStyle = TextStyle(fontSize = 15.sp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                )
            }
        }
    }
}