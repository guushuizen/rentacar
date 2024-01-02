package tech.guus.rentacar.app

import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import tech.guus.rentacar.app.activities.MainComposition
import tech.guus.rentacar.app.models.Coordinates
import tech.guus.rentacar.app.models.OpenStreetMapLocationAddress
import tech.guus.rentacar.app.models.OpenStreetMapLocationInformation
import tech.guus.rentacar.app.models.UserDTO
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.services.LocationService
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.utils.DummyAppContainer
import tech.guus.rentacar.app.utils.generateDummyCar
import tech.guus.rentacar.app.utils.grantPermission
import tech.guus.rentacar.app.viewmodels.RegisterViewModel
import tech.guus.rentacar.app.views.RegisterView
import tech.guus.rentacar.app.views.components.Screen
import java.util.UUID

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RegistrationTest {


    private val location = OpenStreetMapLocationInformation(
        place_id = "123",
        lat = 0.0,
        0.0,
        OpenStreetMapLocationAddress(
            road = "Hogeschoollaan",
            house_number = "1",
            city = "Breda",
            postcode = "4818CR",
            country_code = "NL"
        )
    )


    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testRegistrationValidatesAllFieldsFilledIn() {
        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(DummyAppContainer(mock(), mock(), mock())) {
                    RegisterView(
                        appData = it,
                        viewModel = RegisterViewModel(
                            userRepository = mock(),
                            snackbarHostState = it.snackbarHostState,
                            navigationController = it.navigationController,
                            locationService = mock()
                        )
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("registerButton").performClick()

        composeTestRule.onNodeWithText("Niet alle velden zijn juist ingevuld!").assertExists()
    }

    /***
     * This test has to be ran first, because of a weird issue with the permissions needed to
     * be granted in order for the GPS location service to work, hence the `aaa_` prefix and the
     * `FixMethodOrder(MethodSorters.NAME_ASCENDING)` annotation.
     */
    @Test
    fun aaa_testRegistrationSucceedsAndForwardsToCarList() {
        val coords = Coordinates(0.0, 0.0)
        val locationServiceMock = mock<LocationService> {
            onBlocking { getCurrentCoordinates() }.doReturn(coords)
            onBlocking { searchAddressByCoordinates(coords) }.doReturn(location)
        }

        val userRepositoryMock = mock<UserRepository> {
            onBlocking { registerUser(any()) }.doReturn(true)
            onBlocking { login("guus@guus.tech", "password") }.doReturn("token")
        }

        val carRepositoryMock = mock<CarRepository> {
            onBlocking { getAllCars(any()) }.doReturn(listOf(generateDummyCar()))
        }

        val appContainer = DummyAppContainer(
            userRepository = userRepositoryMock,
            locationService = locationServiceMock,
            carRepository = carRepositoryMock
        )

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(appContainer, Screen.Register.route)
            }
        }

        composeTestRule.onNodeWithTag("determineLocationButton").performClick()

        grantPermission(device, "While using the app")

        composeTestRule.onNodeWithTag("firstName").performTextInput("Guus")
        composeTestRule.onNodeWithTag("lastName").performTextInput("Huizen")
        composeTestRule.onNodeWithTag("email").performTextInput("guus@guus.tech")
        composeTestRule.onNodeWithTag("password").performTextInput("password")

        userRepositoryMock.stub {
            on { loggedInUser } doReturn UserDTO(
                uuid = UUID.randomUUID(),
                firstName = "Guus",
                lastName = "Huizen",
                emailAddress = "guus@guus.tech",
                streetName = "Hogeschoollaan",
                houseNumber = "1",
                postalCode = "4818CR",
                city = "Breda",
                country = "Nederland",
                latitude = 0.0f,
                longitude = 0.0f,
            )
        }

        composeTestRule.onNodeWithTag("registerButton").performClick()

        verifyBlocking(userRepositoryMock) { registerUser(any()) }

        composeTestRule.onNodeWithTag("home-topbar").assertExists()

        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Welkom, Guus").assertExists()
    }
}