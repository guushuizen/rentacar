package tech.guus.rentacar.app.views

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarHost
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.EditListingViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.CameraDialog
import tech.guus.rentacar.app.views.components.Detail


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListing(
    isCreation: Boolean = false,
    viewModel: EditListingViewModel,
    appData: ApplicationData
) {
    val coroutineScope = rememberCoroutineScope()
    val showCamera by viewModel.showCamera.collectAsState()
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                viewModel.updateShowCamera(true)
            } else {
                coroutineScope.launch {
                    appData.snackbarHostState.showSnackbar("Camera permissie is vereist!")
                }
            }
        }

    if (showCamera) {
        CameraDialog(
            onPhotoCaptured = { viewModel.addImage(it) },
            onDialogClose = { viewModel.updateShowCamera(false) }
        )

        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(
                        text = if (isCreation) "Maak jouw listing af" else "Pas jouw listing aan",
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
            FloatingActionButton(onClick = { viewModel.saveListing() }) {
                Icon(Icons.Filled.Check, contentDescription = "Opslaan")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val loading by viewModel.loading.collectAsState()

            if (loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            val carPhotos by viewModel.carPhotos.collectAsState()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = { GridItemSpan(2)}) {
                    Column {
                        if (isCreation) {
                            Text(
                                text = "De gevonden auto:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            )
                        }

                        val listedCar by viewModel.car.collectAsState()

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) { Detail(label = "Merk", value = listedCar!!.brandName) }
                            Column(modifier = Modifier.weight(1f)) { Detail(label = "Model", value = listedCar!!.modelName) }
                        }

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)) {
                            Column(modifier = Modifier.weight(1f)) { Detail(label = "Kleur", value = listedCar!!.color) }
                            Column(modifier = Modifier.weight(1f)) { Detail(label = "Brandstoftype", value = listedCar!!.humanFuelType()) }
                        }

                        val ratePerHour by viewModel.ratePerHour.collectAsState()

                        Text(
                            text = "Hoeveel kost de auto per uur?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 25.dp, bottom = 5.dp)
                        )
                        TextField(
                            value = ratePerHour,
                            onValueChange = { value -> viewModel.updateRatePerHour(value) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Text(text = "€")
                            },
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(vertical = 15.dp)
                                .clickable { viewModel.updateIsActive(!viewModel.isActive.value) }
                        ) {
                            val isActive by viewModel.isActive.collectAsState()
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { viewModel.updateIsActive(!isActive) },
                            )

                            Text(
                                text = "Activeer deze listing",
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }

                        Text(
                            text = "Voeg foto's toe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 25.dp, bottom = 5.dp)
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .height(200.dp)
                            .clickable { launcher.launch(Manifest.permission.CAMERA) },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FloatingActionButton(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Voeg foto toe")
                        }
                        Text(
                            "Voeg foto toe",
                            modifier = Modifier.padding(top = 15.dp)
                        )
                    }
                }

                itemsIndexed(carPhotos) {index, item ->
                    CarPicture(image = item, onRemoveImage = { viewModel.removeImage(index) })
                }
            }
        }
    }
}

@Composable
fun CarPicture(
    image: Uri,
    onRemoveImage: () -> Unit,
) {
    Box(Modifier.height(200.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = image,
                contentDescription = "Picture of a car"
            )
        }

        IconButton(onClick = onRemoveImage, Modifier.size(25.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


