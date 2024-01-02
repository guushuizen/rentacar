package tech.guus.rentacar.app.views

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.SnackbarHost
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.RegisterViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.Screen

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    viewModel: RegisterViewModel,
    appData: ApplicationData,
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.register() },
                modifier = Modifier.testTag("registerButton")
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Registreren")
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(text = Screen.Register.title, modifier = Modifier.padding(10.dp))
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
        }
    ) {
        Box(modifier = Modifier.padding(it)) {

            Column(modifier = Modifier.padding(10.dp)) {
                val firstName = viewModel.firstName.collectAsState()
                val lastName = viewModel.lastName.collectAsState()
                val email = viewModel.emailAddress.collectAsState()
                val password = viewModel.password.collectAsState()

                Row(
                    modifier = Modifier.padding(top = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 5.dp)
                    ) {
                        TextField(
                            value = firstName.value,
                            onValueChange = { v -> viewModel.updateFirstName(v) },
                            singleLine = true,
                            modifier = Modifier.testTag("firstName"),
                            label = { Text(text = "Voornaam") },
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                    ) {
                        TextField(
                            value = lastName.value,
                            onValueChange = { viewModel.updateLastName(it) },
                            singleLine = true,
                            label = { Text(text = "Achternaam") },
                            modifier = Modifier.testTag("lastName"),
                        )
                    }
                }

                TextField(
                    value = email.value,
                    onValueChange = { viewModel.updateEmailAddress(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email")
                        .padding(top = 15.dp),
                    singleLine = true,
                    label = { Text(text = "E-mailadres") }
                )

                TextField(
                    value = password.value,
                    onValueChange = { viewModel.updatePassword(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password")
                        .padding(top = 15.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Wachtwoord") }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 15.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                        granted -> if (granted) {
                            viewModel.determineLocation()
                        }
                    }

                Button(
                    onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("determineLocationButton"),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Locatie bepalen", color = MaterialTheme.colorScheme.onPrimary)
                }

                val streetName = viewModel.streetName.collectAsState()
                val houseNumber = viewModel.houseNumber.collectAsState()
                val postcode = viewModel.postcode.collectAsState()
                val city = viewModel.city.collectAsState()
                val country = viewModel.country.collectAsState()

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .weight(7F)
                            .padding(end = 5.dp)
                    ) {
                        TextField(
                            value = streetName.value,
                            onValueChange = { viewModel.updateStreetName(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            singleLine = true,
                            label = { Text(text = "Straatnaam") }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(4F)
                            .padding(end = 5.dp)
                    ) {
                        TextField(
                            value = houseNumber.value,
                            onValueChange = {
                                viewModel.updateHouseNumber(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            singleLine = true,
                            label = { Text(text = "Huisnummer") }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(
                        modifier = Modifier
                            .weight(4F)
                            .padding(end = 5.dp)
                    ) {
                        TextField(
                            value = postcode.value,
                            onValueChange = { viewModel.updatePostcode(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            singleLine = true,
                            label = { Text(text = "Postcode") }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(7F)
                            .padding(end = 5.dp)
                    ) {
                        TextField(
                            value = city.value,
                            onValueChange = {
                                viewModel.updateCity(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            singleLine = true,
                            label = { Text(text = "Stad") }
                        )
                    }
                }

                var dropdownExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(top = 15.dp),
                    expanded = dropdownExpanded,
                    onExpandedChange = {
                        dropdownExpanded = !dropdownExpanded
                    }
                ) {
                    TextField(
                        readOnly = true,
                        value = viewModel.countryOptions[country.value] ?: "",
                        onValueChange = { },
                        label = { Text("Land") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = dropdownExpanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = {
                            dropdownExpanded = false
                        }
                    ) {
                        viewModel.countryOptions.forEach { (key, value) ->
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.updateCountry(key)
                                    dropdownExpanded = false
                                }
                            ) {
                                Text(text = value)
                            }
                        }
                    }
                }
            }
        }
    }
}