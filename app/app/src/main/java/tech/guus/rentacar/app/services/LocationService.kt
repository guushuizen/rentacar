package tech.guus.rentacar.app.services

import androidx.activity.ComponentActivity
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.guus.rentacar.app.models.Coordinates
import tech.guus.rentacar.app.models.OpenStreetMapLocationInformation


interface LocationService {
    suspend fun getCurrentCoordinates(): Coordinates?
    suspend fun searchAddressByCoordinates(coordinates: Coordinates): OpenStreetMapLocationInformation?
}


class LocationServiceImpl(
    private val activity: ComponentActivity,
    private val httpClient: HttpClient,
) : LocationService {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this.activity)

    override suspend fun getCurrentCoordinates(): Coordinates? = withContext(Dispatchers.IO) {
        return@withContext try {
            val locationTask =
                this@LocationServiceImpl.fusedLocationProviderClient.getCurrentLocation(
                    CurrentLocationRequest.Builder().build(),
                    null
                )

            val result = Tasks.await(locationTask)
            Coordinates(latitude = result.latitude, longitude = result.longitude)
        } catch (e: SecurityException) {
            return@withContext null
        }
    }

    override suspend fun searchAddressByCoordinates(coordinates: Coordinates): OpenStreetMapLocationInformation? {
        val response = this.httpClient.get("https://nominatim.openstreetmap.org/reverse") {
            parameter("format", "jsonv2")
            parameter("lat", coordinates.latitude)
            parameter("lon", coordinates.longitude)
            parameter("addressdetails", 1)
            parameter("zoom", 18)
            parameter("layer", "address")
        }

        if (response.status != HttpStatusCode.OK)
            return null

        return response.body<OpenStreetMapLocationInformation>()
    }
}