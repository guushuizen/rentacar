package tech.guus.rentacar.app.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.SnackbarHost
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tech.guus.rentacar.app.ui.theme.RentACar
import tech.guus.rentacar.app.viewmodels.LoginViewModel
import tech.guus.rentacar.app.views.components.ApplicationData
import tech.guus.rentacar.app.views.components.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    onClickRegistration: (Int) -> Unit,
    appData: ApplicationData,
    viewModel: LoginViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = appData.snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.RentACar(),
                title = {
                    Text(text = Screen.Register.title, modifier = Modifier.padding(10.dp))
                },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { appData.drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = "Heb je al een account?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                val email = viewModel.email.collectAsState()
                val password = viewModel.password.collectAsState()

                TextField(
                    value = email.value,
                    onValueChange = { viewModel.updateEmail(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    singleLine = true,
                    label = { Text(text = "E-mailadres") }
                )

                TextField(
                    value = password.value,
                    onValueChange = { viewModel.updatePassword(it) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.attemptLogin() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Wachtwoord") }
                )

                Button(
                    onClick = { viewModel.attemptLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Inloggen", color = MaterialTheme.colorScheme.onPrimary)
                }

                ClickableText(
                    text = AnnotatedString("Nog geen account?"),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    onClick = onClickRegistration
                )
            }
        }
    }
}