package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.data.ThemeRepository
import com.marwacrazycakes.app.data.Theme
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.cardShadow
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ADMIN_PASSWORD = "crazycakes13"

@Composable
fun AdminThemesScreen() {
    val isWide = rememberIsWide()
    val context = LocalContext.current
    val statusBar = androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var password by remember { mutableStateOf("") }
    var authenticated by remember { mutableStateOf(false) }
    var themes by remember { mutableStateOf<List<Theme>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadThemes() {
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val themeList = ThemeRepository.getThemes()
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    themes = themeList
                    loading = false
                }
            } catch (e: Exception) {
                androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                    error = "Failed to load themes: ${e.message}"
                    loading = false
                }
            }
        }
    }

    fun setDefaultTheme(themeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = ThemeRepository.setDefaultTheme(themeId)
                if (success) {
                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot { snapshot ->
                        themes = ThemeRepository.getThemes()
                    }
                } else {
                    // Could show error
                }
            } catch (e: Exception) {
                // Could show error
            }
        }
    }

    if (!authenticated) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBar)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Theme Administration",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    ),
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (error != null) {
                    Text(
                        text = error!!,
                        style = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = Color.Red),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                androidx.compose.material3.TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
                    label = { Text(text = "Password", style = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = MutedForeground)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                        containerColor = Card,
                        focusedContainerColor = Card,
                        textColor = Primary,
                        labelColor = MutedForeground,
                        cursorColor = Primary,
                    ),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (password == ADMIN_PASSWORD) {
                            authenticated = true
                        } else {
                            error = "Incorrect password"
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text(text = "Access Themes", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = PrimaryForeground))
                }
            }
        }
        return
    }

    // Authenticated view
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBar)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Theme Administration",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    ),
                )
                Button(
                    onClick = { authenticated = false },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Card,
                        contentColor = Primary,
                        borderColor = Border,
                    ),
                ) {
                    Text(text = "Logout", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Primary))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (themes.isEmpty()) {
                Text(
                    text = "No themes found.",
                    style = TextStyle(fontFamily = Inter, fontSize = 16.sp, color = MutedForeground),
                )
            } else {
                Column(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    themes.forEach { theme ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cardShadow(RoundedCornerShape(12.dp))
                                .background(Card, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(theme.primaryColor))
                                            .border(1.dp, Color(theme.border), RoundedCornerShape(12.dp)),
                                    )
                                    Column {
                                        Text(
                                            text = theme.name,
                                            style = TextStyle(
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Primary,
                                            ),
                                        )
                                        Text(
                                            text = if (theme.isDefault) "Active (Default)" else "Click to activate",
                                            style = TextStyle(
                                                fontFamily = Inter,
                                                fontSize = 14.sp,
                                                color = if (theme.isDefault) Color(0xFF4CAF50) else MutedForeground,
                                            ),
                                        )
                                    }
                                }
                                if (!theme.isDefault) {
                                    Button(
                                        onClick = { setDefaultTheme(theme.id) },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary),
                                    ) {
                                        Text(text = "Set as Default", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = PrimaryForeground))
                                    }
                                } else {
                                    Button(
                                        onClick = { },
                                        enabled = false,
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50),
                                            contentColor = Color.White,
                                        ),
                                    ) {
                                        Text(text = "Active", style = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.White))
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ColorSwatch(color = theme.primaryColor, label = "Primary")
                                ColorSwatch(color = theme.secondaryColor, label = "Secondary")
                                ColorSwatch(color = theme.background, label = "Background")
                                ColorSwatch(color = theme.muted, label = "Muted")
                                ColorSwatch(color = theme.gold, label = "Gold")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSwatch(color: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(color))
                .border(1.dp, Border, RoundedCornerShape(8.dp)),
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 10.sp,
                color = MutedForeground,
            ),
        )
    }
}