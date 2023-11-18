package tech.guus.rentacarapi

import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.UserDTO
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.requests.LoginRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class UsersTest {

    @Test
    fun testRegistration() = setupTestApplication {
        val unauthenticatedClient = createUnauthenticatedClient()

        val response = unauthenticatedClient.post("/users") {
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

        transaction {
            assertNotNull(User.find { Users.emailAddress eq "guus@guus.tech" }.firstOrNull())
        }

        val jwtResponse = unauthenticatedClient.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    emailAddress = "guus@guus.tech",
                    password = "foo"
                )
            )
        }
        assertEquals(HttpStatusCode.OK, jwtResponse.status)
        val jwtBody = jwtResponse.body<Map<String, Any>>()
        val jwt: String = jwtBody["token"] as String

        val testLoginResponse = unauthenticatedClient.get("/users") {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
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