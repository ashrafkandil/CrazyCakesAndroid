package com.marwacrazycakes.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.components.ScriptTitle
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberContent
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

@Composable
fun AboutScreen(onNavigate: (Screen) -> Unit) {
    val content = rememberContent()
    val isWide = rememberIsWide()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 64.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeading(
            title = AppData.text(content, "about.title"),
            eyebrow = AppData.text(content, "about.eyebrow"),
        )
        Spacer(modifier = Modifier.height(44.dp))
        ScriptTitle(text = AppData.text(content, "about.headline"), size = 34.sp)
        Spacer(modifier = Modifier.height(48.dp))

        val intro = @Composable {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = AppData.text(content, "about.intro"),
                    style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text(
                    text = AppData.text(content, "about.introItalic"),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
        val portrait = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(8.dp, PrimaryForeground, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                LocalImage(
                    src = "/assets/crazy/seema.png",
                    contentDescription = AppData.text(content, "about.imageAlt"),
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
        }
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(36.dp),
            ) {
                Box(modifier = Modifier.weight(1.1f)) { intro() }
                Box(modifier = Modifier.weight(0.9f)) { portrait() }
            }
        } else {
            intro()
            Spacer(modifier = Modifier.height(36.dp))
            portrait()
        }

        Spacer(modifier = Modifier.height(56.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ScriptTitle(text = AppData.text(content, "about.experienceTitle"), size = 30.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = AppData.text(content, "about.experienceP1"),
                    style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = AppData.text(content, "about.experienceP2"),
                    style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                    textAlign = TextAlign.Center,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ScriptTitle(text = AppData.text(content, "about.communityTitle"), size = 30.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = AppData.text(content, "about.communityP1"),
                    style = TextStyle(fontSize = 15.sp, lineHeight = 30.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = AppData.text(content, "about.quote"),
                style = TextStyle(
                    fontFamily = CrimsonText,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    textAlign = TextAlign.Center,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 28.dp, vertical = 26.dp),
            )
        }

        Spacer(modifier = Modifier.height(52.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onNavigate(Screen.Gallery) },
                border = BorderStroke(1.dp, Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            ) {
                Text(AppData.text(content, "about.cta.gallery"))
            }
            Button(
                onClick = { onNavigate(Screen.Contact) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
            ) {
                Text(AppData.text(content, "about.cta.contact"))
            }
        }
    }
}
