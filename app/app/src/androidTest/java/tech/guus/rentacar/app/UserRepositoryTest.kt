package tech.guus.rentacar.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.junit.runner.RunWith
import tech.guus.rentacar.app.repositories.UserRepositoryImpl
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {
    private val testContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testCoroutineDispatcher = StandardTestDispatcher()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile =
            { testContext.preferencesDataStoreFile("PREFERENCES") }
        )

    @Test
    fun testLoginAndSaveToken() {
        val userRepository = UserRepositoryImpl(dataStore = testDataStore, httpClient = HttpClient(Android))

        testCoroutineScope.launch {
            assertEquals(null, userRepository.getToken())
            userRepository.storeToken("foo")
            assertEquals("foo", userRepository.getToken())
        }
    }
}