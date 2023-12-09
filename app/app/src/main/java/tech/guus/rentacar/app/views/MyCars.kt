package tech.guus.rentacar.app.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import tech.guus.rentacar.app.repositories.UserRepository

@Composable
fun MyCarsView(userRepository: UserRepository) {
    val user = userRepository.loggedInUser

    if (user == null) {

    }
    return Text("My cars view")
}