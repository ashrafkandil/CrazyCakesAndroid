package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.SerifItalic
import com.marwacrazycakes.app.ui.components.openUrl
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Gold
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

@Composable
fun ReviewsScreen() {
    val isWide = rememberIsWide()
    val testimonials = androidx.compose.runtime.remember { AppData.testimonials() }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWide) 320.dp else 260.dp),
        ) {
            LocalImage(
                src = "/assets/crazy/awards.jpg",
                contentDescription = "Celebrating excellence in cake artistry",
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(Overlay))
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScriptTitle(
                    text = "Reviews & Testimonials",
                    color = PrimaryForeground,
                    size = if (isWide) 44.sp else 36.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "CELEBRATING EXCELLENCE IN SUGAR ARTISTRY",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontSize = 11.sp,
                        letterSpacing = 2.7.sp,
                        color = PrimaryForeground.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Our dedication to craft is celebrated across every platform and occasion.",
                    style = TextStyle(fontSize = 16.sp, color = PrimaryForeground, textAlign = TextAlign.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 56.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Founded by Seema Acharya, Marwa Crazy Cakes in Chagrin Falls, OH has been " +
                    "honored with national awards, television appearances, and features in leading " +
                    "publications. Our clients' testimonials are a testament to artistry, innovation, " +
                    "and dedication to crafting unforgettable cakes.",
                style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(if (isWide) 0.85f else 1f),
            )
            Spacer(modifier = Modifier.height(44.dp))
            SerifItalic(text = "Client Reviews")
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Hear what our clients say about their experience with Marwa Crazy Cakes",
                style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Spacer(modifier = Modifier.height(28.dp))
            testimonials.chunked(if (isWide) 3 else 1).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rowItems.forEach { review ->
                        TestimonialCard(review = review, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size < (if (isWide) 3 else 1)) {
                        repeat((if (isWide) 3 else 1) - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { openUrl(context, "https://www.google.com/search?q=Marwa+Crazy+Cakes+Reviews") },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
            ) {
                Text("★ Read More Reviews on Google ★")
            }
        }
    }
}

@Composable
private fun TestimonialCard(
    review: com.marwacrazycakes.app.data.Testimonial,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, Border, shape)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = review.initials,
                    style = TextStyle(fontWeight = FontWeight.Bold, color = PrimaryForeground, fontSize = 15.sp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = TextStyle(fontWeight = FontWeight.SemiBold, color = Primary, fontSize = 14.sp),
                )
                Text(
                    text = review.date,
                    style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            Text(
                text = "★".repeat(review.rating),
                style = TextStyle(color = Gold, fontSize = 13.sp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "\u201C${review.quote}\u201D",
            style = TextStyle(
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                lineHeight = 25.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
