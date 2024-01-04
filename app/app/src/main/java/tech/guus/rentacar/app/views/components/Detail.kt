package tech.guus.rentacar.app.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Detail(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Text(text = value, fontSize = 14.sp)
    }
}