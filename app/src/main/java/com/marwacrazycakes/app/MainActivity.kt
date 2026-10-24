package com.marwacrazycakes.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.marwacrazycakes.app.assistant.AssistantOverlay
import com.marwacrazycakes.app.assistant.AssistantPage
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.ui.components.AppImage
import com.marwacrazycakes.app.ui.components.Eyebrow
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.WatermarkPattern
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.screens.AboutScreen
import com.marwacrazycakes.app.ui.screens.ContactScreen
import com.marwacrazycakes.app.ui.screens.GalleryScreen
import com.marwacrazycakes.app.ui.screens.HomeScreen
import com.marwacrazycakes.app.ui.screens.PricingScreen
import com.marwacrazycakes.app.ui.screens.ReviewsScreen
import com.marwacrazycakes.app.ui.theme.Background
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.DancingScript
import com.marwacrazycakes.app.ui.theme.Footer
import com.marwacrazycakes.app.ui.theme.FooterForeground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.MarwaCrazyCakesTheme
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

enum class Screen(val navLabel: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    About("About Us", Icons.Filled.AccountCircle),
    Gallery("Gallery", Icons.Filled.PhotoLibrary),
    Reviews("Reviews", Icons.Filled.Star),
    Pricing("Pricing", Icons.Filled.LocalOffer),
    Contact("Contact", Icons.Filled.Email),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MarwaCrazyCakesTheme {
                AppBootstrap()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContentRepository.isInitialized()) {
            lifecycleScope.launch(Dispatchers.IO) {
                ContentRepository.checkForContentUpdates()
            }
        }
    }
}

@Composable
private fun AppBootstrap() {
    val context = LocalContext.current
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(attempt) {
        ready = false
        error = null
        showSplash = true
        try {
            ContentRepository.init(context)
            ready = true
        } catch (cause: Throwable) {
            error = cause
        }
    }
    LaunchedEffect(attempt) {
        delay(1000)
        showSplash = false
    }

    SetSystemBarAppearance(lightIcons = !ready || showSplash || error != null)

    when {
        error != null -> SplashError(
            message = error?.message ?: "Unknown error",
            onRetry = { attempt += 1 },
        )
        !ready || showSplash -> SplashScreen(loading = !ready)
        else -> AppRoot()
    }
}

@Composable
private fun SetSystemBarAppearance(lightIcons: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = context as? Activity ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !lightIcons
            controller.isAppearanceLightNavigationBars = !lightIcons
        }
    }
}

@Composable
private fun SplashScreen(loading: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().background(Footer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                AppImage(
                    src = "/assets/crazy/logo.png",
                    contentDescription = "Marwa Crazy Cakes",
                    modifier = Modifier.size(120.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
            ScriptTitle(
                text = "Marwa Crazy Cakes",
                color = PrimaryForeground,
                size = 40.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "EDIBLE ART. UNFORGETTABLE MOMENTS.",
                style = TextStyle(
                    fontFamily = CrimsonText,
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    color = FooterForeground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                ),
            )
            if (loading) {
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = PrimaryForeground,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun SplashError(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Footer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScriptTitle(text = "Something went wrong", color = PrimaryForeground, size = 30.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = FooterForeground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                ),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Primary)
                    .clickable { onRetry() }
                    .padding(horizontal = 26.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "Retry",
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
fun AppRoot() {
    var current by rememberSaveable { mutableStateOf(Screen.Home) }
    var galleryCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var galleryRevision by rememberSaveable { mutableIntStateOf(0) }
    val revision by ContentRepository.revision.collectAsState()
    val wide = rememberIsWide()
    val assistantPage = when (current) {
        Screen.Home -> AssistantPage.Home
        Screen.About -> AssistantPage.About
        Screen.Gallery -> AssistantPage.Gallery
        Screen.Reviews -> AssistantPage.Reviews
        Screen.Pricing -> AssistantPage.Pricing
        Screen.Contact -> AssistantPage.Contact
    }

    fun navigate(screen: Screen, category: String? = null) {
        if (screen == Screen.Gallery) {
            galleryCategory = category
            galleryRevision += 1
        }
        current = screen
    }

    BackHandler(enabled = current != Screen.Home) {
        current = Screen.Home
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        WatermarkPattern(modifier = Modifier.fillMaxSize())

        if (wide) {
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(current = current, onSelect = { navigate(it) })
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    KeyedContent(
                        current = current,
                        revision = revision,
                        galleryRevision = galleryRevision,
                        galleryCategory = galleryCategory,
                        onNavigate = { screen -> navigate(screen) },
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    KeyedContent(
                        current = current,
                        revision = revision,
                        galleryRevision = galleryRevision,
                        galleryCategory = galleryCategory,
                        onNavigate = { screen -> navigate(screen) },
                    )
                }
                BottomBar(current = current, onSelect = { navigate(it) })
            }
        }

        AssistantOverlay(
            page = assistantPage,
            onNavigate = { screen, category -> navigate(screen, category) },
        )
    }
}

@Composable
private fun KeyedContent(
    current: Screen,
    revision: Int,
    galleryRevision: Int,
    galleryCategory: String?,
    onNavigate: (Screen) -> Unit,
) {
    androidx.compose.runtime.key(revision) {
        when (current) {
            Screen.Home -> HomeScreen(onNavigate = onNavigate)
            Screen.About -> AboutScreen(onNavigate = onNavigate)
            Screen.Gallery -> androidx.compose.runtime.key(galleryRevision) {
                GalleryScreen(onNavigate = onNavigate, initialCategory = galleryCategory)
            }
            Screen.Reviews -> ReviewsScreen()
            Screen.Pricing -> PricingScreen(onNavigate = onNavigate)
            Screen.Contact -> ContactScreen()
        }
    }
}

@Composable
private fun Sidebar(current: Screen, onSelect: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Card)
            .drawBehind {
                drawLine(Border, Offset(size.width, 0f), Offset(size.width, size.height), 1.dp.toPx())
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AppImage(
                    src = "/assets/crazy/logo.png",
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
            Text(
                text = "Marwa Crazy Cakes",
                style = TextStyle(
                    fontFamily = DancingScript,
                    fontSize = 22.sp,
                    color = Primary,
                ),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                Screen.entries.forEach { screen ->
                    val active = current == screen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) com.marwacrazycakes.app.ui.theme.Secondary else Color.Transparent)
                            .clickable { onSelect(screen) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = null,
                            tint = if (active) Primary else MutedForeground,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = screen.navLabel,
                            style = TextStyle(
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (active) Primary else com.marwacrazycakes.app.ui.theme.Foreground,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            },
        )
    }
}

@Composable
private fun BottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Card)
            .drawBehind {
                drawLine(Border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            }
            .padding(top = 8.dp, bottom = max(bottomInset.value, 8f).dp),
    ) {
        Screen.entries.forEach { screen ->
            val active = current == screen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(screen) }
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = null,
                    tint = if (active) Primary else MutedForeground,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = screen.navLabel,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 10.sp,
                        color = if (active) Primary else MutedForeground,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
            }
        }
    }
}
