package tech.guus.rentacar.app.utils

import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import tech.guus.rentacar.app.models.CarStatus
import tech.guus.rentacar.app.models.ListedCar
import tech.guus.rentacar.app.models.OpenStreetMapLocationAddress
import tech.guus.rentacar.app.models.OpenStreetMapLocationInformation
import java.util.UUID


fun generateDummyCar(
    brandName: String = "Volkswagen",
    modelName: String = "Scirocco",
    licensePlate: String = "L369JR"
): ListedCar {
    return ListedCar(
        id = UUID.randomUUID(),
        brandName = brandName,
        modelName = modelName,
        color = "Grijs",
        fuelType = "Benzine",
        licensePlate = licensePlate,
        ratePerHour = 10.0F,
        locationLatitude = 51F,
        locationLongitude = 5F,
        ownerName = "Guus Huizen",
        locationString = "2992HL Barendrecht",
        photos = listOf("https://placehold.it/300x100"),
        status = CarStatus.ACTIVE,
        reservedDates = emptyList(),
    )
}


val DUMMY_LOCATION = OpenStreetMapLocationInformation(
    place_id = "123",
    lat = 0.0,
    0.0,
    OpenStreetMapLocationAddress(
        road = "Hogeschoollaan",
        house_number = "1",
        city = "Breda",
        postcode = "4818CR",
        country_code = "NL"
    )
)


fun grantPermission(device: UiDevice, permissionTitle: String?) {
    try {
        val permissionEntry = device.findObject(UiSelector().text(permissionTitle))
        permissionEntry.click()
    } catch (e: UiObjectNotFoundException) {
        println("There is no $permissionTitle permission dialog to interact with")
    }
}
