package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.data.Testimonial
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.cardShadow
import com.marwacrazycakes.app.ui.components.openUrl
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Gold
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

private const val GOOGLE_REVIEWS_URL = "https://www.google.com/search?q=Marwa+Crazy+Cakes+Reviews"

@Composable
fun ReviewsScreen() {
    val isWide = rememberIsWide()
    val testimonials = ContentRepository.testimonials()
    val context = LocalContext.current
    val bannerHeight = if (isWide) 320.dp else 260.dp
    val columns = if (isWide) 3 else 1
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBar)
                .height(bannerHeight)
                .background(Muted),
        ) {
            AppImage(
                src = "/assets/crazy/Testimony.png",
                contentDescription = ContentRepository.text("reviews.hero.title"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(modifier = Modifier.fillMaxSize().background(Overlay))
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ScriptTitle(
                    text = ContentRepository.text("reviews.hero.title"),
                    color = Primary,
                    size = if (isWide) 44.sp else 36.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ContentRepository.text("reviews.hero.subtitle").uppercase(),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontSize = 11.sp,
                        letterSpacing = 2.9.sp,
                        color = Primary,
                        textAlign = TextAlign.Center,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ContentRepository.text("reviews.hero.body"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = Primary,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = ContentRepository.text("reviews.intro"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 30.sp,
                    color = MutedForeground,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.widthIn(max = 720.dp),
            )
            Spacer(modifier = Modifier.height(44.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = ContentRepository.text("reviews.sectionTitle"),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontStyle = FontStyle.Italic,
                        fontSize = 26.sp,
                        color = Primary,
                    ),
                )
                Text(
                    text = ContentRepository.text("reviews.sectionSubtitle"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                testimonials.chunked(columns).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        rowItems.forEach { review ->
                            TestimonialCard(review = review, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size < columns) {
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Primary)
                    .clickable { openUrl(context, GOOGLE_REVIEWS_URL) }
                    .padding(horizontal = 26.dp, vertical = 14.dp),
            ) {
                Text(
                    text = ContentRepository.text("reviews.googleLinkText"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = PrimaryForeground,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TestimonialCard(review: Testimonial, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .cardShadow(shape)
            .background(Card, shape)
            .border(1.dp, Border, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = review.initials,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = PrimaryForeground,
                    ),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Primary,
                    ),
                )
                Text(
                    text = review.date,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = MutedForeground,
                    ),
                )
            }
            Text(
                text = "\u2605".repeat(review.rating),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Gold,
                ),
            )
        }
        Text(
            text = "\u201C${review.quote}\u201D",
            style = TextStyle(
                fontFamily = Inter,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                lineHeight = 25.sp,
                color = MutedForeground,
            ),
        )
    }
}
