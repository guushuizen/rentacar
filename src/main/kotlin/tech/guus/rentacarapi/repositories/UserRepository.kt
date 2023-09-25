package tech.guus.rentacarapi.repositories

import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.all
import org.ktorm.entity.filter
import org.ktorm.entity.firstOrNull
import tech.guus.rentacarapi.models.User
import tech.guus.rentacarapi.models.Users
import tech.guus.rentacarapi.models.users
import tech.guus.rentacarapi.services.DatabaseService

interface UserRepository {
    fun getAll(): List<User>

    fun findOne(uuid: String): User?

    fun insert(user: User)
}

class UserRepositoryImpl(private val databaseService: DatabaseService) : UserRepository {
    override fun getAll(): List<User> {
        return databaseService.database
            .from(Users)
            .select()
            .map { row -> Users.createEntity(row) }
    }

    override fun findOne(uuid: String): User? {
        return databaseService.database.users
            .filter { it.uuid eq uuid }
            .firstOrNull()
    }

    override fun insert(user: User) {
        databaseService.database.users.add(user)
    }
}