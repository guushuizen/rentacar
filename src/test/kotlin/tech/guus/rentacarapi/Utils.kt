package tech.guus.rentacarapi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.requests.LoginRequest
import tech.guus.rentacarapi.services.Database
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

fun setupTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
    testApplication(EmptyCoroutineContext) {
        environment {
            config = ApplicationConfig("application.test.conf")
        }

        application {
            module()
        }

        block()

        Database.resetDatabase()
    }
}

fun ApplicationTestBuilder.createUnauthenticatedClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            jackson()
        }
    }
}

suspend fun ApplicationTestBuilder.createAuthenticatedClient(): HttpClient {
    val jsonClient = createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
    }

    val jwt = generateJwt(jsonClient)

    return createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
    }
}

fun setupTestApplicationWithUser(block: suspend ApplicationTestBuilder.() -> Unit) = setupTestApplication {
    var client = createClient {
        install(ContentNegotiation) {
            jackson()
        }
    }

    createUser(
        client, CreateUserRequest(
            "Guus",
            "Huizen",
            "guus@guus.tech",
            "foo",
            "Hogeschoollaan",
            "1",
            "Breda",
            "NL",
            "123.000",
            5.8428F, // Nijmegen
            51.8449F
        )
    )

    val jwt = generateJwt(client)

    client = client.config {
        install(ContentNegotiation) {
            jackson()
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
    }

    val testLoginResponse = client.get("/users")
    assertEquals(HttpStatusCode.OK, testLoginResponse.status)

    block()
}

suspend fun generateJwt(client: HttpClient, emailAddress: String = "guus@guus.tech", password: String = "foo"): String? {
    val jwtResponse = client.post("/login") {
        contentType(ContentType.Application.Json)
        setBody(
            LoginRequest(
                emailAddress = emailAddress,
                password = password
            )
        )
    }
    return jwtResponse.body<Map<String, String>>()["token"]
}

suspend fun createUser(client: HttpClient, body: CreateUserRequest): UserDTO {
    val response = client.post("/users") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    assertEquals(HttpStatusCode.Created, response.status)

    return response.body() as UserDTO
}

fun createDummyCar(block: Car.() -> Unit = {}): Car {
    val user: User = transaction { User.find { Users.emailAddress eq "guus@guus.tech" }.first() }

    return transaction {
        Car.new(UUID.randomUUID()) {
            this.owner = user
            this.brandName = "VOLKSWAGEN"
            this.modelName = "SCIROCCO"
            this.color = "GRIJS"
            this.licensePlate = "L369JR"
            this.status = CarStatus.DRAFT
            this.fuelType = FuelType.ICE

            block()
        }
    }
}