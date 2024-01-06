package tech.guus.rentacar.app

import android.support.test.uiautomator.UiDevice
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import tech.guus.rentacar.app.activities.MainComposition
import tech.guus.rentacar.app.models.ChosenFilterValues
import tech.guus.rentacar.app.models.Coordinates
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.services.LocationService
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.utils.DUMMY_LOCATION
import tech.guus.rentacar.app.utils.DummyAppContainer
import tech.guus.rentacar.app.utils.generateDummyCar
import tech.guus.rentacar.app.utils.grantPermission
import tech.guus.rentacar.app.viewmodels.CarListViewModel
import tech.guus.rentacar.app.views.CarListView

class CarListTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }


    @Test
    fun testRenderCarList() {
        val carRepositoryMock = mock<CarRepository> {
            onBlocking { getAllCars(any()) } doReturn listOf(
                generateDummyCar(brandName = "Volkswagen", modelName = "Golf"),
                generateDummyCar(brandName = "BMW", modelName = "320i"),
            )
        }

        val locationServiceMock = mock<LocationService> {
            onBlocking { getCurrentCoordinates() } doReturn Coordinates(51.0, 5.0)
            onBlocking { searchAddressByCoordinates(any()) } doReturn DUMMY_LOCATION
        }

        val appContainer = DummyAppContainer(
            carRepository = carRepositoryMock,
            userRepository = mock(),
            locationService = locationServiceMock,
            myCarRepository = mock(),
        )

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(container = appContainer) {
                    CarListView(
                        appData = it,
                        carListViewModel = CarListViewModel(
                            snackbarHostState = it.snackbarHostState,
                            carRepository = appContainer.carRepository,
                            locationService = appContainer.locationService,
                        )
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("BMW 320i").assertExists()
        composeTestRule.onNodeWithText("Volkswagen Golf").assertExists()

        composeTestRule.onNodeWithTag("filter-button").performClick()

        composeTestRule.onNodeWithTag("filter-dialog").assertExists()
        composeTestRule.onNodeWithTag("brand-filter").performClick()
        composeTestRule.onNodeWithText("Volkswagen").assertExists()
        composeTestRule.onNodeWithText("BMW").assertExists()

        composeTestRule.onNodeWithText("BMW").performClick()
        composeTestRule.onNodeWithText("Volkswagen").assertDoesNotExist()

        composeTestRule.onNodeWithTag("model-filter").performClick()
        composeTestRule.onNodeWithText("320i").assertExists()
        composeTestRule.onNodeWithText("320i").performClick()

        composeTestRule.onNodeWithTag("determine-location").performClick()

        grantPermission(device, "While using the app")
        composeTestRule.onNodeWithText(
            "${DUMMY_LOCATION.address.road} ${DUMMY_LOCATION.address.house_number}, ${DUMMY_LOCATION.address.postcode} ${DUMMY_LOCATION.address.city}, ${DUMMY_LOCATION.address.country_code.uppercase()}"
        ).assertExists()

        composeTestRule.onNodeWithTag("location-range-slider").assertExists()
        composeTestRule.onNodeWithTag("location-range-slider").performTouchInput {
            swipeRight()
        }

        composeTestRule.onNodeWithTag("filter-dialog-confirm").performClick()

        verifyBlocking(carRepositoryMock) {
            getAllCars(
                filterValues = ChosenFilterValues(
                    chosenBrandName = "BMW",
                    chosenModelName = "320i",
                    chosenCoordinates = Coordinates(51.0, 5.0),
                    chosenRadius = 244,
                )
            )
        }
    }
}