package tech.guus.rentacarapi

import com.typesafe.config.ConfigFactory
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.BeforeEach
import org.koin.ktor.ext.inject
import tech.guus.rentacarapi.repositories.UserRepository
import tech.guus.rentacarapi.repositories.UserRepositoryImpl
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.requests.LoginRequest
import tech.guus.rentacarapi.services.DatabaseService
import java.sql.Connection
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals


class UsersTest {

    fun setupTestApplication(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication(EmptyCoroutineContext) {
            environment {
                config = ApplicationConfig("application.test.conf")
            }

            application {
                init(HoconApplicationConfig(ConfigFactory.load("application.test.conf")))
            }

            block()
        }
    }

    @Test
    fun testRegistration() = setupTestApplication {
        var client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateUserRequest(
                    "Guus",
                    "Huizen",
                    "guus@guus.tech",
                    "foo",
                    "Hogeschoollaan",
                    "1",
                    "Breda",
                    "NL",
                    "123.000",
                    10.0F,
                    10.0F
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)

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
    }


    @Test
    fun testNoDuplicateEmailAddresses() = setupTestApplication {
        val request = CreateUserRequest(
            "Guus",
            "Huizen",
            "foo@guus.tech",
            "foo",
            "Hogeschoollaan",
            "1",
            "Breda",
            "NL",
            "123.000",
            10.0F,
            10.0F
        )

        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        assertEquals(HttpStatusCode.Created, client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.status)

        assertEquals(HttpStatusCode.Conflict, client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.status)
    }

    @Test
    fun testUnauthenticatedRoute() = setupTestApplication {
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val response = client.get("/users")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}