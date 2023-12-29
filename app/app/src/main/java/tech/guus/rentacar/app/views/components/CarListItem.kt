package tech.guus.rentacar.app.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tech.guus.rentacar.app.models.ListedCar
import java.math.RoundingMode


@Composable
fun CarListItem(car: ListedCar, onClick: () -> Unit) {
    if (car.ratePerHour == null) return

    return Box(
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column {
                AsyncImage(
                    model = car.photos[0],
                    contentDescription = "Photo of a ${car.brandName} ${car.modelName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(200.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column {
                        Text(
                            fontWeight = FontWeight.Bold,
                            text = "${car.brandName} ${car.modelName}",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = car.locationString
                        )
                    }

                    Text(
                        text = "€${car.ratePerHour.toBigDecimal().setScale(2)}/uur",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }

}