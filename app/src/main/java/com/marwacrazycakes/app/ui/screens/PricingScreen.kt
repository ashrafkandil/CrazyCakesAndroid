package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.PrimaryButton
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Foreground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary

private val priceItems = listOf("custom", "sculpted", "wedding", "cupcakes")

@Composable
fun PricingScreen(onNavigate: (Screen) -> Unit) {
    val isWide = rememberIsWide()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionHeading(
                title = ContentRepository.text("pricing.title"),
                eyebrow = ContentRepository.text("pricing.eyebrow"),
            )
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Muted),
            ) {
                AppImage(
                    src = "/assets/crazy/pricing.jpeg",
                    contentDescription = ContentRepository.text("pricing.imageAlt"),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.widthIn(max = if (isWide) 680.dp else 560.dp).fillMaxWidth(),
            ) {
                Text(
                    text = ContentRepository.text("pricing.intro"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        lineHeight = 30.sp,
                        color = MutedForeground,
                    ),
                )
                Column(modifier = Modifier.padding(top = 26.dp)) {
                    HorizontalDivider(color = Border)
                    priceItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = ContentRepository.text("pricing.item.$item.label"),
                                style = TextStyle(
                                    fontFamily = CrimsonText,
                                    fontSize = 18.sp,
                                    color = Foreground,
                                ),
                            )
                            Text(
                                text = ContentRepository.text("pricing.item.$item.price"),
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Primary,
                                ),
                            )
                        }
                        if (index == priceItems.lastIndex) {
                            HorizontalDivider(color = Border)
                        } else {
                            HorizontalDivider(color = Border)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ContentRepository.text("pricing.quotePrompt"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = MutedForeground,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        label = ContentRepository.text("pricing.quoteButton"),
                        onClick = { onNavigate(Screen.Contact) },
                    )
                }
            }
        }
    }
}
