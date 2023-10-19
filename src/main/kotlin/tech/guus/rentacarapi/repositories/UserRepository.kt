package tech.guus.rentacarapi.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.requests.CreateUserRequest
import tech.guus.rentacarapi.services.DatabaseService
import tech.guus.rentacarapi.services.DatabaseService.dbQuery
import java.util.*

interface UserRepository {
    suspend fun findOne(uuid: String): User?

    suspend fun attemptLogin(emailAddress: String, password: String): User?

    suspend fun insert(createUserRequest: CreateUserRequest): User
}

class UserRepositoryImpl : UserRepository {
    override suspend fun findOne(uuid: String): User? = dbQuery {
        User.findById(UUID.fromString(uuid))
    }

    override suspend fun attemptLogin(emailAddress: String, password: String): User? = dbQuery {
        User.find {
            Users.emailAddress eq emailAddress
            Users.password eq password
        }
            .singleOrNull()
    }

    override suspend fun insert(createUserRequest: CreateUserRequest): User = dbQuery {
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