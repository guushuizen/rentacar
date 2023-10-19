package tech.guus.rentacarapi

import com.typesafe.config.ConfigFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.UserDTO
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.services.DatabaseService
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class UsersTest {

    @Test
    fun testRegistration() = setupTestApplication {
        val response = createUnauthenticatedClient().post("/users") {
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

        transaction(DatabaseService.database) {
            assertNotNull(User.find { Users.emailAddress eq "guus@guus.tech" }.firstOrNull())
        }

        val testLoginResponse = createAuthenticatedClient().get("/users")
        assertEquals(HttpStatusCode.OK, testLoginResponse.status)
        val body = testLoginResponse.body() as UserDTO
        assertEquals("Guus", body.firstName)
    }

    @Test
    fun testNoDuplicateEmailAddresses() = setupTestApplication {
        val request = CreateUserRequest(
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

        assertEquals(HttpStatusCode.Created, createUnauthenticatedClient().post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.status)

        assertEquals(HttpStatusCode.Conflict, createUnauthenticatedClient().post("/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.status)
    }

    @Test
    fun testUnauthenticatedRoute() = setupTestApplication {
        val response = createUnauthenticatedClient().get("/users")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}