package tech.guus.rentacar.app.utils

import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import tech.guus.rentacar.app.models.ListedCar
import java.util.UUID


fun generateDummyCar(): ListedCar {
    return ListedCar(
        id = UUID.randomUUID(),
        brandName = "Volkswagen",
        modelName = "Scirocco",
        color = "Grijs",
        fuelType = "Benzine",
        licensePlate = "L-369-JR",
        ratePerHour = 10.0F,
        locationLatitude = 51F,
        locationLongitude = 5F,
        ownerName = "Guus Huizen",
        locationString = "2992HL Barendrecht",
        photos = listOf("https://placehold.it/300x100"),
        status = "AVAILABLE",
        reservedDates = emptyList(),
    )
}


@Throws(UiObjectNotFoundException::class)
fun grantPermission(device: UiDevice, permissionTitle: String?) {
    val permissionEntry = device.findObject(UiSelector().text(permissionTitle))
    permissionEntry.click()
}
