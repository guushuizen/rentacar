package tech.guus.rentacarapi.repositories

import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.services.DatabaseService.dbQuery
import java.util.*


class UserRepository {

    suspend fun attemptLogin(emailAddress: String, password: String): User? = dbQuery {
        User.find {
            Users.emailAddress eq emailAddress
            Users.password eq password
        }
            .singleOrNull()
    }

    suspend fun insert(createUserRequest: CreateUserRequest): User = dbQuery {
        User.new(UUID.randomUUID()) {
            firstName = createUserRequest.firstName
            lastName = createUserRequest.lastName
            emailAddress = createUserRequest.emailAddress
            password = createUserRequest.password
            streetName = createUserRequest.streetName
            houseNumber = createUserRequest.houseNumber
            postalCode = createUserRequest.postalCode
            city = createUserRequest.city
            country = createUserRequest.country
            latitude = createUserRequest.latitude
            longitude = createUserRequest.longitude
        }
    }
}