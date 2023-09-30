package tech.guus.rentacarapi.repositories

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.services.DatabaseService

interface UserRepository {
    suspend fun getAll(): List<User>

    suspend fun findOne(uuid: String): User?

    suspend fun attemptLogin(emailAddress: String, password: String): User?

    suspend fun insert(user: User): User
}

class UserRepositoryImpl(private val databaseService: DatabaseService) : UserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        uuid = row[Users.uuid],
        firstName = row[Users.firstName],
        lastName = row[Users.lastName],
        emailAddress = row[Users.emailAddres],
        password = row[Users.password],
        streetName = row[Users.streetName],
        houseNumber = row[Users.houseNumber],
        postalCode = row[Users.postalCode],
        city = row[Users.city],
        country = row[Users.country],
        latitude = row[Users.latitude],
        longitude = row[Users.longitude]
    )
    override suspend fun getAll(): List<User> = databaseService.dbQuery {
        Users.selectAll()
            .map(::resultRowToUser)
    }

    override suspend fun findOne(uuid: String): User? = databaseService.dbQuery {
        Users.select { Users.uuid eq uuid }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun attemptLogin(emailAddress: String, password: String): User? = databaseService.dbQuery {
        Users.select {
            Users.emailAddres eq emailAddress
            Users.password eq password
        }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun insert(user: User): User = databaseService.dbQuery {
        val insertStatement = Users.insert {
            it[uuid] = user.uuid
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[emailAddres] = user.emailAddress
            it[password] = user.password
            it[streetName] = user.streetName
            it[houseNumber] = user.houseNumber
            it[postalCode] = user.postalCode
            it[city] = user.city
            it[country] = user.country
            it[latitude] = user.latitude
            it[longitude] = user.longitude
        }

        insertStatement.resultedValues?.singleOrNull()!!.let(::resultRowToUser)
    }
}