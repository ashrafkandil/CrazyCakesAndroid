package com.marwacrazycakes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.screens.AboutScreen
import com.marwacrazycakes.app.ui.screens.ContactScreen
import com.marwacrazycakes.app.ui.screens.GalleryScreen
import com.marwacrazycakes.app.ui.screens.HomeScreen
import com.marwacrazycakes.app.ui.screens.PricingScreen
import com.marwacrazycakes.app.ui.screens.ReviewsScreen
import com.marwacrazycakes.app.ui.theme.MarwaCrazyCakesTheme

enum class Screen(val navLabel: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    About("About", Icons.Filled.PersonOutline),
    Gallery("Gallery", Icons.Filled.PhotoLibrary),
    Reviews("Reviews", Icons.Filled.Star),
    Pricing("Pricing", Icons.Filled.AttachMoney),
    Contact("Contact", Icons.Filled.Email),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppData.init(applicationContext)
        super.onCreate(savedInstanceState)
        setContent {
            MarwaCrazyCakesTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var current by rememberSaveable { mutableStateOf(Screen.Home) }
    val widthDp = LocalConfiguration.current.screenWidthDp
    val useRail = widthDp >= 720

    val content: @Composable () -> Unit = {
        when (current) {
            Screen.Home -> HomeScreen(onNavigate = { current = it })
            Screen.About -> AboutScreen(onNavigate = { current = it })
            Screen.Gallery -> GalleryScreen(onNavigate = { current = it })
            Screen.Reviews -> ReviewsScreen()
            Screen.Pricing -> PricingScreen(onNavigate = { current = it })
            Screen.Contact -> ContactScreen()
        }
    }

    if (useRail) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavigationRail(
                header = {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White, CircleShape),
                        ) {
                            LocalImage(
                                src = "/assets/crazy/logo.png",
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentScale = ContentScale.Inside,
                            )
                        }
                    }
                },
            ) {
                Screen.entries.forEach { screen ->
                    NavigationRailItem(
                        selected = current == screen,
                        onClick = { current = screen },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.navLabel) },
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                content()
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = current == screen,
                            onClick = { current = screen },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.navLabel) },
                            alwaysShowLabel = false,
                        )
                    }
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                content()
            }
        }
    }
}
