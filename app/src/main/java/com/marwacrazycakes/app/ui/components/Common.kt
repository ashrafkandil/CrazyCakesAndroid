package com.marwacrazycakes.app.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.DancingScript
import com.marwacrazycakes.app.ui.theme.Foreground
import com.marwacrazycakes.app.ui.theme.Footer
import com.marwacrazycakes.app.ui.theme.FooterForeground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberIsWide(): Boolean = LocalConfiguration.current.screenWidthDp >= 720

@Composable
fun rememberIsPortrait(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp >= configuration.screenWidthDp
}

private val assetBitmapCache = mutableMapOf<String, ImageBitmap>()

@Composable
fun rememberAssetBitmap(path: String): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = synchronized(assetBitmapCache) { assetBitmapCache[path] }, path) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                context.assets.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }?.also { decoded -> synchronized(assetBitmapCache) { assetBitmapCache[path] = decoded } }
        }
    }.value
}

/**
 * Loads the remote content-pack copy of an image when available and falls back to
 * the bundled asset, matching the React Native `LocalImage` component.
 */
@Composable
fun AppImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val revision by ContentRepository.revision.collectAsState()
    val contentVersion by ContentRepository.contentVersion.collectAsState()
    val remote = remember(src, revision, contentVersion) {
        if (contentVersion == "bundled") null else ContentRepository.contentMediaUrl(src)
    }
    val asset = remember(src) { ContentRepository.assetUri(src) }
    var remoteFailed by remember(src, revision, contentVersion) { mutableStateOf(false) }
    if (asset == null && (remote == null || remoteFailed)) {
        Box(modifier = modifier.background(Muted))
        return
    }
    if (remote == null || remoteFailed) {
        AsyncImage(
            model = asset,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            placeholder = ColorPainter(Muted),
            error = ColorPainter(Muted),
        )
        return
    }
    // Show the bundled asset immediately, then let the validated content-pack copy
    // replace it once it has downloaded. If the remote copy fails, the bundled
    // image simply stays in place.
    Box(modifier = modifier) {
        AsyncImage(
            model = asset,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            placeholder = ColorPainter(Muted),
            error = ColorPainter(Muted),
        )
        AsyncImage(
            model = remote,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            onError = { remoteFailed = true },
        )
    }
}

/** Tiled logo watermark used behind screens and inside gallery tiles. */
@Composable
fun WatermarkPattern(
    modifier: Modifier = Modifier,
    tileSize: Dp = 88.dp,
    opacity: Float = 0.055f,
) {
    val bitmap = rememberAssetBitmap("images/logo.png")
    val tilePx = with(LocalDensity.current) { tileSize.toPx() }
    Canvas(modifier = modifier) {
        val image = bitmap ?: return@Canvas
        if (tilePx <= 0f) return@Canvas
        val columns = ceil(size.width / tilePx).toInt().coerceAtLeast(1)
        val rows = ceil(size.height / tilePx).toInt().coerceAtLeast(1)
        val tile = ceil(tilePx).toInt()
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                drawImage(
                    image = image,
                    dstOffset = IntOffset((column * tilePx).roundToInt(), (row * tilePx).roundToInt()),
                    dstSize = IntSize(tile, tile),
                    alpha = opacity,
                    filterQuality = FilterQuality.Low,
                )
            }
        }
    }
}

@Composable
fun ScriptTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Primary,
    size: TextUnit = 36.sp,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = (size.value * 1.25f).sp,
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = DancingScript,
            fontSize = size,
            lineHeight = lineHeight,
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScriptTitle(text = title, size = 38.sp)
        Eyebrow(text = eyebrow)
    }
}

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outline: Boolean = false,
    wide: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(999.dp)
    val base = if (wide) modifier.fillMaxWidth() else modifier
    Box(
        modifier = base
            .alpha(if (pressed) 0.85f else 1f)
            .clip(shape)
            .background(if (outline) Color.Transparent else Primary)
            .then(if (outline) Modifier.border(1.dp, Primary, shape) else Modifier)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (outline) Primary else com.marwacrazycakes.app.ui.theme.PrimaryForeground,
            ),
        )
    }
}

@Composable
fun SocialPill(
    label: String,
    url: String,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, if (dark) FooterForeground.copy(alpha = 0.35f) else Border, shape)
            .clickable { openUrl(context, url) }
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (dark) FooterForeground else Foreground,
            ),
        )
    }
}

@Composable
fun BrandFooter() {
    val socialLinks = remember { ContentRepository.socialLinks() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Footer)
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AppImage(
                    src = "/assets/crazy/logo.png",
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column {
                ScriptTitle(
                    text = "Marwa Crazy Cakes",
                    color = FooterForeground,
                    size = 28.sp,
                    textAlign = TextAlign.Start,
                )
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
            socialLinks.forEach { social ->
                SocialPill(label = social.label, url = social.href, dark = true)
            }
        }
        Text(
            text = "© 2026 Marwa Crazy Cakes. All rights reserved.",
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 13.sp,
                color = FooterForeground.copy(alpha = 0.75f),
            ),
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    textAreaHeight: Dp? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(if (textAreaHeight != null) Modifier.height(textAreaHeight) else Modifier)
            .border(1.dp, Border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        textStyle = TextStyle(
            fontFamily = Inter,
            fontSize = 15.sp,
            color = Foreground,
        ),
        cursorBrush = SolidColor(Primary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        maxLines = if (singleLine) 1 else 8,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            color = MutedForeground.copy(alpha = 0.7f),
                        ),
                    )
                }
                innerTextField()
            }
        },
    )
}

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        // No activity can handle this URL.
    }
}

fun openEmail(context: Context, email: String) {
    openUrl(context, "mailto:$email")
}

fun openDial(context: Context, phone: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (_: Exception) {
        // No dialer available.
    }
}

fun Modifier.cardShadow(shape: Shape): Modifier = shadow(elevation = 2.dp, shape = shape, clip = false)
