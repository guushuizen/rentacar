package tech.guus.rentacar.app.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import tech.guus.rentacar.app.viewmodels.LoginViewModel

@Composable
fun LoginView(
    onClickRegistration: (Int) -> Unit,
    viewModel: LoginViewModel
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.padding(10.dp)) {
        Text(text = "Heb je al een account?", fontSize = 20.sp, fontWeight = FontWeight.Bold)

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
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            style = TextStyle(
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            onClick = onClickRegistration
        )
    }
}