package tech.guus.rentacarapi

import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import tech.guus.rentacarapi.models.CreateUserRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class UsersTest {

    @Test
    fun testRoot() = testApplication {
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
    }


    @Test
    fun testRegistration() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(
                "Guus",
                "Huizen",
                "guus@guus.tech",
                "foo",
                "Hogeschoollaan",
                "1",
                "Breda",
                "NL",
                "123.000",
                "10.000"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}