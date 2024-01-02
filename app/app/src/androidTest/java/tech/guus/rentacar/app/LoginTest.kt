package tech.guus.rentacar.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import tech.guus.rentacar.app.activities.MainComposition
import tech.guus.rentacar.app.repositories.CarRepository
import tech.guus.rentacar.app.repositories.UserRepository
import tech.guus.rentacar.app.ui.theme.RentACarTheme
import tech.guus.rentacar.app.utils.DummyAppContainer
import tech.guus.rentacar.app.utils.generateDummyCar
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.views.LoginView

class LoginTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testLoginRenders() {
        val appContainer = DummyAppContainer(mock(), mock(), mock())

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(appContainer) {
                    LoginView(
                        onClickRegistration = {},
                        appData = it,
                        viewModel = LoginViewModel(
                            appContainer.userRepository,
                            it.navigationController,
                            it.snackbarHostState,
                            mock()
                        )
                    )
                }
            }
        }

        composeTestRule.onNode(
            hasText("Inloggen") and
                    hasClickAction() and
                    hasTestTag("login-button")
        ).assertExists()
    }

    @Test
    fun testLoginRendersErrorMessage() {
        val userRepositoryMock = mock<UserRepository> {
            onBlocking { login(any(), any()) } doReturn null
        }
        val appContainer = DummyAppContainer(userRepositoryMock, mock(), mock())

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(appContainer) {
                    LoginView(
                        onClickRegistration = {},
                        appData = it,
                        viewModel = LoginViewModel(
                            navController = it.navigationController,
                            snackbarHostState = it.snackbarHostState,
                            userRepository = appContainer.userRepository,
                            focusManager = mock()
                        )
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(
            text = "De door jouw ingevulde gegevens zijn incorrect"
        ).assertDoesNotExist()

        composeTestRule.onNodeWithTag("email").performTextInput("foo@bar.baz")
        composeTestRule.onNodeWithTag("password").performTextInput("foo")
        composeTestRule.onNode(
            hasText("Inloggen") and
                    hasClickAction() and
                    hasTestTag("login-button")
        ).performClick()

        composeTestRule.onNodeWithText(
            text = "De door jouw ingevulde gegevens zijn incorrect"
        ).assertExists()

        verifyBlocking(userRepositoryMock) {
            login("foo@bar.baz", "foo")
        }
    }

    @Test
    fun testLoginForwardsToOtherPage() {
        val userRepositoryMock = mock<UserRepository> {
            onBlocking { login(any(), any()) } doReturn "token"
        }
        val carRepositoryMock = mock<CarRepository> {
            onBlocking { getAllCars(any()) } doReturn listOf(generateDummyCar())
        }
        val appContainer = DummyAppContainer(userRepositoryMock, carRepositoryMock, mock())

        composeTestRule.setContent {
            RentACarTheme {
                MainComposition(appContainer, "login")
            }
        }

        composeTestRule.onNodeWithTag("email").performTextInput("foo@bar.baz")
        composeTestRule.onNodeWithTag("password").performTextInput("foo")
        composeTestRule.onNode(
            hasText("Inloggen") and
                    hasClickAction() and
                    hasTestTag("login-button")
        ).performClick()

        composeTestRule.onNodeWithText(
            text = "Succesvol ingelogd"
        ).assertExists()

        verifyBlocking(userRepositoryMock) {
            login("foo@bar.baz", "foo")

            storeToken("token")
        }

        composeTestRule.onNodeWithTag("home-topbar").assertExists()
    }
}