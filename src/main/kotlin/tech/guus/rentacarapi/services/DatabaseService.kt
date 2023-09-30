package tech.guus.rentacarapi.services

import com.typesafe.config.ConfigObject
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.Users
import java.sql.DriverManager

class DatabaseService(config: HoconApplicationConfig) {
    private val driver = config.propertyOrNull("ktor.database.driver")!!.getString()
    private val url = config.propertyOrNull("ktor.database.url")!!.getString()
    private val username = config.propertyOrNull("ktor.database.username")!!.getString()
    private val password = config.propertyOrNull("ktor.database.password")!!.getString()

    private val database: Database = Database.connect(
        url =  url,
        driver = driver,
        user = username,
        password = password,
    )

    init {
        // Required to keep in memory database alive during tests
        val keepAliveConnection = DriverManager.getConnection(url, username, password)

        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}