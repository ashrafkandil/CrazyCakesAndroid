package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.PrimaryButton
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary

@Composable
fun AboutScreen(onNavigate: (Screen) -> Unit) {
    val isWide = rememberIsWide()
    val maxWidth = if (isWide) 900.dp else 640.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SectionHeading(
                title = ContentRepository.text("about.title"),
                eyebrow = ContentRepository.text("about.eyebrow"),
            )
            Spacer(modifier = Modifier.height(36.dp))
            ScriptTitle(
                text = ContentRepository.text("about.headline"),
                size = 34.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(44.dp))

            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(8.dp, Card, RoundedCornerShape(12.dp))
                    .background(Card),
            ) {
                AppImage(
                    src = "/assets/crazy/Marwa.png",
                    contentDescription = ContentRepository.text("about.imageAlt"),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = ContentRepository.text("about.intro"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        lineHeight = 30.sp,
                        color = MutedForeground,
                    ),
                )
                Text(
                    text = ContentRepository.text("about.introItalic"),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        color = MutedForeground,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(56.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ScriptTitle(text = ContentRepository.text("about.experienceTitle"), size = 30.sp)
                    Text(
                        text = ContentRepository.text("about.experienceP1"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            lineHeight = 30.sp,
                            color = MutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    Text(
                        text = ContentRepository.text("about.experienceP2"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            lineHeight = 30.sp,
                            color = MutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ScriptTitle(text = ContentRepository.text("about.communityTitle"), size = 30.sp)
                    Text(
                        text = ContentRepository.text("about.communityP1"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            lineHeight = 30.sp,
                            color = MutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                Text(
                    text = ContentRepository.text("about.quote"),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Normal,
                        color = Primary,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Card.copy(alpha = 0.65f))
                        .padding(horizontal = 28.dp, vertical = 26.dp),
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    label = ContentRepository.text("about.cta.gallery"),
                    onClick = { onNavigate(Screen.Gallery) },
                    outline = true,
                )
                PrimaryButton(
                    label = ContentRepository.text("about.cta.contact"),
                    onClick = { onNavigate(Screen.Contact) },
                )
            }
        }
    }
}
