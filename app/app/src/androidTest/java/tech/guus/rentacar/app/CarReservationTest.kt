package tech.guus.rentacar.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import tech.guus.rentacar.app.activities.MainComposition
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.utils.DummyAppContainer
import tech.guus.rentacar.app.utils.LOGGED_IN_USER
import tech.guus.rentacar.app.utils.generateDummyCar
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertTrue

class CarReservationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testReserveCar() {
        val now = LocalDateTime.now()
        val car = generateDummyCar()
        val carRepositoryMock = mock<CarRepository> {
            onBlocking { getAllCars(any()) } doReturn listOf(
                car
            )
            onBlocking {
                reserveCar(eq(car.id.toString()), any(), any())
            } doReturnConsecutively listOf("Onbekende fout", null)
            on { getCar(any()) } doReturn car
        }

        val userRepositoryMock = mock<UserRepository> {
            on { loggedInUser } doReturn LOGGED_IN_USER
        }

        val appContainer = DummyAppContainer(
            carRepository = carRepositoryMock,
            userRepository = userRepositoryMock,
            locationService = mock(),
            myCarRepository = mock(),
        )

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(appContainer, startDestination = "cars")
            }
        }

        composeTestRule.onNodeWithText(car.title()).performClick()

        composeTestRule.onNodeWithContentDescription("Maak een reservering").performClick()

        composeTestRule.onNodeWithText("Totale tijd").assertExists()
        composeTestRule.onNodeWithText("Totale prijs").assertExists()

        composeTestRule.onNodeWithContentDescription("Reserveren").performClick()
        composeTestRule.onNodeWithText("Onbekende fout").assertExists()

        composeTestRule.onNodeWithContentDescription("Reserveren").performClick()

        // Assert we're back on the detail page.
        composeTestRule.onNodeWithContentDescription("Maak een reservering").assertExists()

        val startDateTimeCaptor = argumentCaptor<LocalDateTime>()
        val endDateTimeCaptor = argumentCaptor<LocalDateTime>()
        verifyBlocking(carRepositoryMock, times(2)) {
            reserveCar(
                eq(car.id.toString()),
                startDateTimeCaptor.capture(),
                endDateTimeCaptor.capture()
            )
        }

        assertTrue(
            startDateTimeCaptor.firstValue.truncatedTo(ChronoUnit.MINUTES)
                .isEqual(now.truncatedTo(ChronoUnit.MINUTES))
        )

        assertTrue(
            endDateTimeCaptor.firstValue.truncatedTo(ChronoUnit.MINUTES)
                .isEqual(now.plusHours(1L).truncatedTo(ChronoUnit.MINUTES))
        )
    }
}