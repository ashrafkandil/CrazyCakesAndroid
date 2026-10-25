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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.AutoFitScriptTitle
import com.marwacrazycakes.app.ui.components.BrandFooter
import com.marwacrazycakes.app.ui.components.Eyebrow
import com.marwacrazycakes.app.ui.components.PrimaryButton
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.cardShadow
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Background
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary
import kotlin.math.min
import kotlinx.coroutines.delay

private data class HeroSlide(val src: String, val alt: String)

private data class DestinationCard(val screen: Screen, val image: String, val keyId: String)

private val heroSlides = listOf(
    HeroSlide("/assets/crazy/hero-1.jpg", "Jaw-dropping sculpted cake masterpiece"),
    HeroSlide("/assets/crazy/hero-2.jpg", "Sophisticated multi-tiered wedding cake with edible flowers"),
    HeroSlide("/assets/crazy/hero-3.jpg", "Whimsical sculpted cake with playful design"),
    HeroSlide("/assets/crazy/hero-4.jpg", "Fine edible detailing with sugar flowers and gold leaf"),
)

private val cards = listOf(
    DestinationCard(Screen.About, "/assets/crazy/Marwa.png", "about"),
    DestinationCard(Screen.Gallery, "/assets/crazy/celebration-1.JPEG", "gallery"),
    DestinationCard(Screen.Reviews, "/assets/crazy/Testimony.png", "reviews"),
    DestinationCard(Screen.Pricing, "/assets/crazy/pricing.jpeg", "pricing"),
)

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    val isWide = rememberIsWide()
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.toFloat()
    val height = configuration.screenHeightDp.toFloat()
    val isPortrait = height >= width
    val heroHeight = min(
        min(
            if (isPortrait) width * 0.9f else height * 0.86f,
            if (isPortrait) height * 0.56f else width * 0.45f,
        ),
        560f,
    ).dp
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
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
                .padding(top = statusBar)
                .height(heroHeight),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AppImage(
                    src = heroSlides[page].src,
                    contentDescription = heroSlides[page].alt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(horizontal = 28.dp, vertical = 0.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AutoFitScriptTitle(
                text = ContentRepository.text("home.hero.title"),
                maxSize = if (isWide) 52.sp else 38.sp,
            )
            Eyebrow(text = ContentRepository.text("home.hero.tagline"))
            Text(
                text = ContentRepository.text("home.hero.body"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MutedForeground,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.widthIn(max = 640.dp).padding(top = 12.dp),
            )
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryButton(
                    label = ContentRepository.text("home.hero.cta.gallery"),
                    onClick = { onNavigate(Screen.Gallery) },
                )
                PrimaryButton(
                    label = ContentRepository.text("home.hero.cta.contact"),
                    onClick = { onNavigate(Screen.Contact) },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionHeading(
                title = ContentRepository.text("home.teaser.title"),
                eyebrow = ContentRepository.text("home.teaser.eyebrow"),
            )
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                cards.chunked(if (isWide) 2 else 1).forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        rowCards.forEach { card ->
                            TeaserCard(
                                card = card,
                                wide = isWide,
                                modifier = Modifier.weight(1f),
                                onNavigate = onNavigate,
                            )
                        }
                        if (isWide && rowCards.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = ContentRepository.text("home.cta.text"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = MutedForeground,
                ),
            )
            Spacer(modifier = Modifier.height(18.dp))
            PrimaryButton(
                label = ContentRepository.text("home.cta.label"),
                onClick = { onNavigate(Screen.Contact) },
            )
        }

        BrandFooter()
    }
}

@Composable
private fun TeaserCard(
    card: DestinationCard,
    wide: Boolean,
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .cardShadow(shape)
            .background(Card, shape)
            .border(1.dp, Border, shape)
            .clip(shape)
            .clickable { onNavigate(card.screen) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        ) {
            AppImage(
                src = card.image,
                contentDescription = ContentRepository.text("home.card.${card.keyId}.alt"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        }
        Column(modifier = Modifier.padding(24.dp)) {
            ScriptTitle(
                text = ContentRepository.text("home.card.${card.keyId}.title"),
                size = 28.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = ContentRepository.text("home.card.${card.keyId}.blurb"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = MutedForeground,
                ),
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = ContentRepository.text("home.cards.link") + " \u2192",
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Primary,
                ),
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
