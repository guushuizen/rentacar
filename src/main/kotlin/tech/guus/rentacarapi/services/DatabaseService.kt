package tech.guus.rentacarapi.services

import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import tech.guus.rentacarapi.models.Cars
import tech.guus.rentacarapi.models.Users
import java.sql.Connection
import java.sql.DriverManager

object DatabaseService {
    val tables = setOf(
        Users,
        Cars
    )

    var database: Database? = null

    fun init(config: HoconApplicationConfig) {
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_READ_UNCOMMITTED

        val driver = config.propertyOrNull("ktor.database.driver")!!.getString()
        val url = config.propertyOrNull("ktor.database.url")!!.getString()
        val username = config.propertyOrNull("ktor.database.username")!!.getString()
        val password = config.propertyOrNull("ktor.database.password")!!.getString()

        database = Database.connect(
            url = url,
            driver = driver,
            user = username,
            password = password,
        )

        // Required to keep in memory database alive during tests
        val keepAliveConnection = DriverManager.getConnection(url, username, password)

        transaction(database) {
            tables.forEach { SchemaUtils.create(it) }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    fun resetDatabase() {
        transaction(database) {
            SchemaUtils.drop(*tables.toTypedArray())
            SchemaUtils.create(*tables.toTypedArray())
        }
    }
}