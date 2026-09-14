package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.BrandFooter
import com.marwacrazycakes.app.ui.components.Eyebrow
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberContent
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground
import kotlinx.coroutines.delay

private data class HeroSlide(val src: String, val alt: String)

private data class DestinationCard(val screen: Screen, val imageSrc: String, val keyId: String)

private val heroSlides = listOf(
    HeroSlide("/assets/crazy/hero-1.jpg", "Jaw-dropping sculpted cake masterpiece"),
    HeroSlide("/assets/crazy/hero-2.jpg", "Sophisticated multi-tiered wedding cake with edible flowers"),
    HeroSlide("/assets/crazy/hero-3.jpg", "Whimsical sculpted cake with playful design"),
    HeroSlide("/assets/crazy/hero-4.jpg", "Fine edible detailing with sugar flowers and gold leaf"),
)

private val destinationCards = listOf(
    DestinationCard(Screen.About, "/assets/crazy/seema.png", "about"),
    DestinationCard(Screen.Gallery, "/assets/crazy/celebration-1.jpg", "gallery"),
    DestinationCard(Screen.Reviews, "/assets/crazy/awards.jpg", "reviews"),
    DestinationCard(Screen.Pricing, "/assets/crazy/pricing.jpg", "pricing"),
)

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    val content = rememberContent()
    val isWide = rememberIsWide()
    val pagerState = rememberPagerState(pageCount = { heroSlides.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(6500)
            val next = (pagerState.settledPage + 1) % heroSlides.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWide) 560.dp else 480.dp),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.matchParentSize(),
            ) { page ->
                LocalImage(
                    src = heroSlides[page].src,
                    contentDescription = heroSlides[page].alt,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Box(modifier = Modifier.matchParentSize().background(Overlay))
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScriptTitle(
                    text = AppData.text(content, "home.hero.title"),
                    color = PrimaryForeground,
                    size = if (isWide) 54.sp else 40.sp,
                )
                Spacer(modifier = Modifier.height(18.dp))
                Eyebrow(
                    text = AppData.text(content, "home.hero.tagline"),
                    color = PrimaryForeground.copy(alpha = 0.92f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = AppData.text(content, "home.hero.body"),
                    style = TextStyle(fontSize = 15.sp, lineHeight = 24.sp, color = PrimaryForeground),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(if (isWide) 640.dp else 360.dp),
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onNavigate(Screen.Gallery) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
                    ) {
                        Text(AppData.text(content, "home.hero.cta.gallery"))
                    }
                    OutlinedButton(
                        onClick = { onNavigate(Screen.Contact) },
                        border = BorderStroke(1.dp, PrimaryForeground.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryForeground),
                    ) {
                        Text(AppData.text(content, "home.hero.cta.contact"))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 64.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionHeading(
                title = AppData.text(content, "home.teaser.title"),
                eyebrow = AppData.text(content, "home.teaser.eyebrow"),
            )
            Spacer(modifier = Modifier.height(40.dp))
            destinationCards.chunked(if (isWide) 2 else 1).forEach { rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    rowCards.forEach { card ->
                        DestinationCardView(
                            card = card,
                            content = content,
                            onNavigate = onNavigate,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (isWide && rowCards.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = AppData.text(content, "home.cta.text"),
                style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onNavigate(Screen.Contact) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
                modifier = Modifier.height(48.dp).width(if (isWide) 280.dp else 260.dp),
            ) {
                Text(AppData.text(content, "home.cta.label"))
            }
        }

        BrandFooter()
    }
}

@Composable
private fun DestinationCardView(
    card: DestinationCard,
    content: Map<String, String>,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, Border, shape)
            .clip(shape)
            .clickable { onNavigate(card.screen) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        ) {
            LocalImage(
                src = card.imageSrc,
                contentDescription = AppData.text(content, "home.card.${card.keyId}.alt"),
                modifier = Modifier.matchParentSize(),
            )
        }
        Column(modifier = Modifier.padding(24.dp)) {
            ScriptTitle(
                text = AppData.text(content, "home.card.${card.keyId}.title"),
                size = 28.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = AppData.text(content, "home.card.${card.keyId}.blurb"),
                style = TextStyle(fontSize = 14.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppData.text(content, "home.cards.link"),
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Primary),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
