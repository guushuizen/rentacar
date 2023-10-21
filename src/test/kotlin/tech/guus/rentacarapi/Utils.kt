package tech.guus.rentacarapi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.*
import tech.guus.rentacarapi.requests.CreateUserRequest
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

fun ApplicationTestBuilder.createAuthenticatedClient(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }

        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(username = "guus@guus.tech", password = "foo")
                }
            }
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

    client = createClient {
        install(ContentNegotiation) {
            jackson()
        }

        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = "guus@guus.tech", password = "foo")
                }
            }
        }
    }

    val testLoginResponse = client.get("/users")
    assertEquals(HttpStatusCode.OK, testLoginResponse.status)

    block()
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