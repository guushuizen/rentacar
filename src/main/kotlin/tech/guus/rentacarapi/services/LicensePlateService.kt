package tech.guus.rentacarapi.services

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*


/**
 * [
 *   {
 *     "kenteken": "L369JR",
 *     "brandstof_volgnummer": "1",
 *     "brandstof_omschrijving": "Benzine",
 *     "brandstofverbruik_buiten": "5.30",
 *     "brandstofverbruik_gecombineerd": "6.40",
 *     "brandstofverbruik_stad": "8.40",
 *     "co2_uitstoot_gecombineerd": "149",
 *     "geluidsniveau_rijdend": "72",
 *     "geluidsniveau_stationair": "76",
 *     "emissiecode_omschrijving": "5",
 *     "milieuklasse_eg_goedkeuring_licht": "715/2007*692/2008A",
 *     "uitstoot_deeltjes_licht": "0.00160",
 *     "nettomaximumvermogen": "90.00",
 *     "toerental_geluidsniveau": "3750",
 *     "uitlaatemissieniveau": "EURO 5 A"
 *   }
 * ]
 */

data class EconomicLicensePlateInfo(
    val kenteken: String,
    val brandstof_omschrijving: String,
)

data class BasicLicensePlateInfo(
    val kenteken: String,
    val merk: String,
    val handelsbenaming: String,
    val eerste_kleur: String,
)

class LicensePlateService {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getLicensePlateDetails(licensePlate: String): Pair<BasicLicensePlateInfo, EconomicLicensePlateInfo>? {
        val basicInfoResponse = httpClient.get("https://opendata.rdw.nl/resource/m9d7-ebf2.json") {
            url {
                parameter("kenteken", licensePlate)
            }
            accept(ContentType.Application.Json)
        }

        val basicInfoResults = basicInfoResponse.body<Array<BasicLicensePlateInfo>>()

        if (basicInfoResults.isEmpty()) {
            return null
        }

        val economicInfoResponse = httpClient.get("https://opendata.rdw.nl/resource/8ys7-d773.json") {
            url {
                parameter("kenteken", licensePlate)
            }
            accept(ContentType.Application.Json)
        }

        val economicResults = economicInfoResponse.body<Array<EconomicLicensePlateInfo>>()

        if (economicResults.isEmpty()) {
            return null
        }

        return Pair(basicInfoResults.first(), economicResults.first())
    }
}