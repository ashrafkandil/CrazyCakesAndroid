package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.data.GalleryImage
import com.marwacrazycakes.app.data.GalleryVideo
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.PrimaryButton
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.WatermarkPattern
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Foreground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.OverlaySoft
import com.marwacrazycakes.app.ui.theme.OverlayLightbox
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground
import com.marwacrazycakes.app.ui.theme.Secondary
import kotlin.random.Random
import kotlinx.coroutines.launch

private data class LightboxState(val images: List<GalleryImage>, val index: Int)

private data class VideoState(val videos: List<GalleryVideo>, val index: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryScreen(onNavigate: (Screen) -> Unit, initialCategory: String? = null) {
    val isWide = rememberIsWide()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val columns = if (isWide) 3 else 2
    val cellWidth = ((if (isWide) 860 else screenWidth) - 40 - 12 * (columns - 1)) / columns
    var category by rememberSaveable { mutableStateOf(initialCategory ?: "all") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var shuffleSeed by rememberSaveable {
        mutableStateOf("${System.currentTimeMillis()}-${Random.nextDouble()}")
    }
    var lightbox by remember { mutableStateOf<LightboxState?>(null) }
    var videoState by remember { mutableStateOf<VideoState?>(null) }

    val gallery = remember(category, page, shuffleSeed) {
        ContentRepository.getGallery(category.takeUnless { it == "all" }, page, shuffleSeed)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeading(
            title = ContentRepository.text("gallery.title"),
            eyebrow = ContentRepository.text("gallery.eyebrow"),
        )
        Text(
            text = ContentRepository.text("gallery.description"),
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 26.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.widthIn(max = 720.dp).padding(top = 18.dp),
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chips = listOf("all" to ContentRepository.text("gallery.allCategory")) +
                gallery.categories.map { it.slug to it.title }
            chips.forEach { (slug, title) ->
                val active = gallery.activeCategory == slug
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) Primary else Secondary)
                        .clickable {
                            category = slug
                            page = 1
                            lightbox = null
                            if (slug == "all") {
                                shuffleSeed = "${System.currentTimeMillis()}-${Random.nextDouble()}"
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (active) PrimaryForeground else Foreground,
                        ),
                    )
                }
            }
        }

        if (gallery.images.isEmpty()) {
            Text(
                text = ContentRepository.text("gallery.empty"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = MutedForeground,
                ),
                modifier = Modifier.padding(vertical = 48.dp),
            )
        } else {
            Column(
                modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth().padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                gallery.images.chunked(columns).forEach { rowImages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    ) {
                        rowImages.forEach { image ->
                            val index = gallery.images.indexOf(image)
                            Box(
                                modifier = Modifier
                                    .width(cellWidth.dp)
                                    .height((cellWidth * 3 / 4).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Muted)
                                    .border(2.dp, Foreground, RoundedCornerShape(8.dp))
                                    .clickable { lightbox = LightboxState(gallery.images, index) },
                            ) {
                                WatermarkPattern(
                                    modifier = Modifier.fillMaxSize(),
                                    tileSize = 48.dp,
                                    opacity = 0.08f,
                                )
                                AppImage(
                                    src = image.src,
                                    contentDescription = image.alt,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (gallery.pageCount > 1) {
            Row(
                modifier = Modifier.padding(top = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PageButton(
                    label = ContentRepository.text("gallery.previous"),
                    enabled = gallery.page > 1,
                    onClick = { page = (gallery.page - 1).coerceAtLeast(1) },
                )
                Text(
                    text = ContentRepository.text("gallery.pageStatus")
                        .replace("{page}", gallery.page.toString())
                        .replace("{pageCount}", gallery.pageCount.toString())
                        .replace("{total}", gallery.total.toString()),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = MutedForeground,
                    ),
                )
                PageButton(
                    label = ContentRepository.text("gallery.next"),
                    enabled = gallery.page < gallery.pageCount,
                    onClick = { page = (gallery.page + 1).coerceAtMost(gallery.pageCount) },
                )
            }
        }

        if (gallery.videos.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalDivider(color = Border)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SectionHeading(
                        title = ContentRepository.text("gallery.videoTitle"),
                        eyebrow = ContentRepository.text("gallery.videoEyebrow"),
                    )
                    Text(
                        text = ContentRepository.text("gallery.videoDescription"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            color = MutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier
                            .widthIn(max = 620.dp)
                            .padding(top = 18.dp, bottom = 28.dp),
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        gallery.videos.forEach { video ->
                            val index = gallery.videos.indexOf(video)
                            VideoTile(
                                video = video,
                                width = cellWidth.dp,
                                onClick = { videoState = VideoState(gallery.videos, index) },
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = ContentRepository.text("gallery.ctaText"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = MutedForeground,
                ),
            )
            PrimaryButton(
                label = ContentRepository.text("gallery.ctaLabel"),
                onClick = { onNavigate(Screen.Contact) },
            )
        }
    }

    lightbox?.let { state ->
        ImageLightbox(
            images = state.images,
            initialIndex = state.index,
            onClose = { lightbox = null },
        )
    }

    videoState?.let { state ->
        VideoLightbox(
            videos = state.videos,
            initialIndex = state.index,
            onClose = { videoState = null },
        )
    }
}

@Composable
private fun PageButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Primary,
        ),
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun VideoTile(video: GalleryVideo, width: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .height(width * 3 / 4)
            .clip(RoundedCornerShape(8.dp))
            .background(Muted)
            .border(2.dp, Foreground, RoundedCornerShape(8.dp))
            .clickable { onClick() },
    ) {
        WatermarkPattern(modifier = Modifier.fillMaxSize(), tileSize = 48.dp, opacity = 0.08f)
        AppImage(
            src = ContentRepository.posterFor(video),
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.fillMaxSize().background(OverlaySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = PrimaryForeground,
                modifier = Modifier.size(48.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(OverlaySoft)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = video.title,
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PrimaryForeground,
                ),
            )
        }
    }
}

@Composable
private fun ImageLightbox(images: List<GalleryImage>, initialIndex: Int, onClose: () -> Unit) {
    val startIndex = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { images.size })
    val current = images.getOrNull(pagerState.currentPage) ?: images.first()
    val scope = rememberCoroutineScope()

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = statusBar).background(OverlayLightbox)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${current.alt} \u00B7 ${current.categoryTitle}",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = PrimaryForeground,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier = Modifier.clickable { onClose() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = PrimaryForeground,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = ContentRepository.text("gallery.closeImage"),
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = PrimaryForeground,
                            ),
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AppImage(
                            src = images[page].src,
                            contentDescription = images[page].alt,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                ) {
                    PageButton(
                        label = ContentRepository.text("gallery.previousImage"),
                        enabled = true,
                        onClick = {
                            val next = (pagerState.currentPage - 1 + images.size) % images.size
                            scope.launch { pagerState.animateScrollToPage(next) }
                        },
                    )
                    PageButton(
                        label = ContentRepository.text("gallery.nextImage"),
                        enabled = true,
                        onClick = {
                            val next = (pagerState.currentPage + 1) % images.size
                            scope.launch { pagerState.animateScrollToPage(next) }
                        },
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun VideoLightbox(videos: List<GalleryVideo>, initialIndex: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableIntStateOf(initialIndex.coerceIn(0, (videos.size - 1).coerceAtLeast(0))) }
    var useBundled by remember(index) { mutableStateOf(false) }
    val video = videos[index]
    val contentVersion by ContentRepository.contentVersion.collectAsState()
    val asset = remember(video.src) { ContentRepository.mediaAssetUri(video.src) }
    val remote = remember(video.src, useBundled, contentVersion) {
        if (useBundled || contentVersion == "bundled") null else ContentRepository.contentMediaUrl(video.src)
    }
    val source = remote ?: asset
    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    DisposableEffect(player, index) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (!useBundled && asset != null) useBundled = true
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(index, source) {
        if (source == null) return@LaunchedEffect
        player.setMediaItem(MediaItem.fromUri(source))
        player.prepare()
        player.playWhenReady = true
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(OverlayLightbox),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${video.title} \u00B7 ${video.categoryTitle}",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = PrimaryForeground,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier.clickable { onClose() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = PrimaryForeground,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = ContentRepository.text("gallery.closeVideo"),
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = PrimaryForeground,
                        ),
                    )
                }
            }
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        keepScreenOn = true
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            ) {
                PageButton(
                    label = ContentRepository.text("gallery.previousVideo"),
                    enabled = true,
                    onClick = { index = (index - 1 + videos.size) % videos.size },
                )
                PageButton(
                    label = ContentRepository.text("gallery.nextVideo"),
                    enabled = true,
                    onClick = { index = (index + 1) % videos.size },
                )
            }
        }
    }
}
