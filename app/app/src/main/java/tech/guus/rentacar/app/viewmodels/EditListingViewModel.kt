package tech.guus.rentacar.app.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material.SnackbarHostState
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.models.CarStatus
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.repositories.MyCarRepository
import tech.guus.rentacar.app.views.components.Screen
import java.io.File
import java.io.FileOutputStream


class EditListingViewModel(
    val carUuid: String,
    private val myCarRepository: MyCarRepository,
    private val navigationController: NavController,
    private val snackbarHostState: SnackbarHostState,
) : BaseViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _showCamera = MutableStateFlow(false)
    val showCamera = _showCamera.asStateFlow()

    private val _car = MutableStateFlow<ListedCar?>(null)
    val car = _car.asStateFlow()

    private val _ratePerHour = MutableStateFlow("")
    val ratePerHour = _ratePerHour.asStateFlow()

    private val _carPhotos = MutableStateFlow<List<Uri>>(emptyList())
    val carPhotos = _carPhotos.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    init {
        _loading.update { true }

        val foundCar = myCarRepository.getCar(carUuid)

        if (foundCar == null) {
            navigationController.navigate(Screen.MyCars.route)
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Deze auto kon niet gevonden worden!")
            }
        } else {
            _car.value = foundCar

            if (foundCar.ratePerHour != null) {
                _ratePerHour.update { foundCar.ratePerHour.toBigDecimal().setScale(2).toString() }
            }

            viewModelScope.launch {
                _carPhotos.update { myCarRepository.loadCarPictures(foundCar.photos) }
                _isActive.update { foundCar.status == CarStatus.ACTIVE }
            }

            _loading.update { false }
        }

    }

    private fun loadImageToTmpFile(url: String): Uri {
        val file = File.createTempFile("rentacar", ".jpg")
        val outStream = FileOutputStream(file)
        outStream.write(Uri.parse(url).toFile().readBytes())
        outStream.close()

        return file.toUri()
    }

    fun updateIsActive(value: Boolean) {
        _isActive.update { value }
    }

    fun updateShowCamera(value: Boolean) {
        _showCamera.update { value }
    }

    fun updateRatePerHour(value: String) {
        _ratePerHour.update { value }
    }

    fun addImage(image: Bitmap) {
        val file = File.createTempFile("rentacar", ".jpg")
        val outStream = FileOutputStream(file)
        image.compress(Bitmap.CompressFormat.JPEG, 75, outStream)
        outStream.close()

        _carPhotos.update {
            _carPhotos.value.toMutableList().apply {
                add(file.toUri())
            }
        }

        updateShowCamera(false)
    }

    fun removeImage(index: Int) {
        _carPhotos.update {
            it.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun saveListing() = viewModelScope.launch {
        try {
            myCarRepository.savePhotos(_car.value!!.id, carPhotos.value)
            myCarRepository.updateCar(
                _car.value!!.id,
                ratePerHour.value.toFloat(),
                if (isActive.value) CarStatus.ACTIVE else CarStatus.DRAFT
            )

            navigationController.navigate(Screen.MyCars.route)
            viewModelScope.launch {
                snackbarHostState.showSnackbar("Gelukt! Je listing is aangepast!")
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar(
                e.message ?: "Er is iets misgegaan, probeer het later opnieuw."
            )
        }
    }
}