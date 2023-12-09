package tech.guus.rentacar.app.repositories

import tech.guus.rentacar.app.models.UserDTO

abstract class UserRepository {

    abstract var loggedInUser: UserDTO?

    abstract suspend fun login(email: String, password: String): Boolean

}

class UserRepositoryImpl : UserRepository() {
    override var loggedInUser: UserDTO? = null
//        get() = throw NotImplementedError()

    override suspend fun login(email: String, password: String): Boolean {
        return true
    }
}