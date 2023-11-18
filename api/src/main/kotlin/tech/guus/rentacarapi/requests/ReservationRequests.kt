package tech.guus.rentacarapi.requests

import java.time.Instant


data class ReserveCarRequest(
    val startDateTime: Instant,
    val endDateTime: Instant
)
