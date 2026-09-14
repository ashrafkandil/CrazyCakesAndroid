package com.marwacrazycakes.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.DancingScript
import com.marwacrazycakes.app.ui.theme.Footer
import com.marwacrazycakes.app.ui.theme.FooterForeground
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary

@Composable
fun rememberContent(): Map<String, String> = remember { AppData.content() }

@Composable
fun rememberIsWide(): Boolean {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return widthDp >= 600
}

fun drawableResFor(context: Context, src: String): Int {
    val name = src.substringAfterLast('/').substringBeforeLast('.').replace("-", "")
    return context.resources.getIdentifier(name, "drawable", context.packageName)
}

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        // no browser available; ignore
    }
}

@Composable
fun LocalImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val resId = remember(src) { drawableResFor(context, src) }
    if (resId == 0) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    } else {
        Image(
            painter = painterResource(resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

@Composable
fun ScriptTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    size: TextUnit = 36.sp,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = DancingScript,
            fontSize = size,
            lineHeight = size * 1.2f,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MutedForeground,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = TextStyle(
            fontFamily = CrimsonText,
            fontSize = 11.sp,
            letterSpacing = 2.9.sp,
            color = color,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
fun SectionHeading(title: String, eyebrow: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScriptTitle(text = title, size = 38.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Eyebrow(text = eyebrow)
    }
}

@Composable
fun SerifItalic(text: String, modifier: Modifier = Modifier, color: Color = Primary) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = CrimsonText,
            fontStyle = FontStyle.Italic,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            color = color,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
fun SocialPill(
    label: String,
    url: String,
    onLight: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val contentColor = if (onLight) MaterialTheme.colorScheme.onSurface else FooterForeground
    val borderColor = if (onLight) Border else FooterForeground.copy(alpha = 0.35f)
    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .clickable { openUrl(context, url) },
    ) {
        Text(
            text = label,
            style = TextStyle(color = contentColor, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
        )
    }
}

@Composable
fun BrandFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Footer).padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White, CircleShape)
                    .padding(4.dp),
            ) {
                LocalImage(
                    src = "/assets/crazy/logo.png",
                    contentDescription = "Marwa Crazy Cakes",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Inside,
                )
            }
            Column(horizontalAlignment = Alignment.Start) {
                ScriptTitle(text = "Marwa Crazy Cakes", color = FooterForeground, size = 28.sp, textAlign = TextAlign.Start)
                Text(
                    text = "EDIBLE ART. UNFORGETTABLE MOMENTS.",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        color = FooterForeground.copy(alpha = 0.7f),
                    ),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SocialPill("Instagram", "https://www.instagram.com/marwacrazycakes/", onLight = false)
            SocialPill("TikTok", "https://www.tiktok.com/@marwacrazycakes", onLight = false)
            SocialPill("X (Twitter)", "https://x.com/marwacrazycakes", onLight = false)
        }
        Text(
            text = "Chagrin Falls, Ohio",
            style = TextStyle(color = FooterForeground.copy(alpha = 0.75f), fontSize = 13.sp),
        )
        Text(
            text = "© 2026 Marwa Crazy Cakes. All rights reserved.",
            style = TextStyle(color = FooterForeground.copy(alpha = 0.75f), fontSize = 13.sp),
        )
    }
}
