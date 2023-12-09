package tech.guus.rentacar.app.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import tech.guus.rentacar.app.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(viewModel: LoginViewModel) {
    Text("Login view")
}