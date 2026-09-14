package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

private val prices = listOf(
    "Custom Cakes" to "$150 & up",
    "Sculpted Cakes" to "$300 & up",
    "Wedding Cakes" to "$500 & up",
    "Cupcakes (per dozen)" to "$40 & up",
)

@Composable
fun PricingScreen(onNavigate: (Screen) -> Unit) {
    val isWide = rememberIsWide()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 56.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeading(
            title = "Cake Pricing",
            eyebrow = "Thoughtfully designed for your special event",
        )
        Spacer(modifier = Modifier.height(48.dp))

        val copy = @Composable {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Our cakes and other goodies are thoughtfully designed for your special " +
                        "event and are individually priced. To give you an idea of what to expect, " +
                        "we have provided starting prices for each of our products.",
                    style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Spacer(modifier = Modifier.height(26.dp))
                Column {
                    HorizontalDivider(color = Border)
                    prices.forEachIndexed { index, (label, price) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(fontFamily = CrimsonText, fontSize = 18.sp),
                            )
                            Text(
                                text = price,
                                style = TextStyle(fontWeight = FontWeight.Bold, color = Primary, fontSize = 15.sp),
                            )
                        }
                        if (index != prices.lastIndex) HorizontalDivider(color = Border)
                    }
                    HorizontalDivider(color = Border)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Have something else in mind?",
                        style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Button(
                        onClick = { onNavigate(Screen.Contact) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
                    ) {
                        Text("Get a quote")
                    }
                }
            }
        }
        val picture = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                LocalImage(
                    src = "/assets/crazy/pricing.jpg",
                    contentDescription = "Elegant wedding cake with multiple tiers and decorative elements",
                    modifier = Modifier.matchParentSize(),
                )
            }
        }

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) { copy() }
                Box(modifier = Modifier.weight(1f)) { picture() }
            }
        } else {
            copy()
            Spacer(modifier = Modifier.height(36.dp))
            picture()
        }
    }
}
