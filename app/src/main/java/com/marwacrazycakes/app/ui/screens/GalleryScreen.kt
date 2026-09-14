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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.LocalImage
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.OverlayStrong
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalleryScreen(onNavigate: (Screen) -> Unit) {
    val isWide = rememberIsWide()
    var category by rememberSaveable { mutableStateOf("all") }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var lightboxIndex by rememberSaveable { mutableIntStateOf(-1) }

    val gallery = androidx.compose.runtime.remember(category, page) {
        AppData.gallery(category.takeUnless { it == "all" }, page)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 56.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeading(title = "Our Gallery", eyebrow = "Portfolio of custom creations")
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Explore our portfolio of custom creations, each one unique and crafted with " +
                "passion. From intimate celebrations to grand events, see how we bring visions to life.",
            style = TextStyle(fontSize = 15.sp, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 720.dp),
        )
        Spacer(modifier = Modifier.height(32.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            val chips = listOf("all" to "All") + gallery.categories.map { it.slug to it.title }
            chips.forEach { (slug, title) ->
                FilterChip(
                    selected = gallery.activeCategory == slug,
                    onClick = {
                        category = slug
                        page = 1
                        lightboxIndex = -1
                    },
                    label = { Text(title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = PrimaryForeground,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (gallery.images.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No cakes in this category yet — check back soon!",
                    style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        } else {
            gallery.images.chunked(if (isWide) 3 else 2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { image ->
                        val index = gallery.images.indexOf(image)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, Border, RoundedCornerShape(10.dp))
                                .clickable { lightboxIndex = index },
                        ) {
                            LocalImage(
                                src = image.src,
                                contentDescription = image.alt,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (gallery.pageCount > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(
                    onClick = { if (gallery.page > 1) page = gallery.page - 1 },
                    enabled = gallery.page > 1,
                ) {
                    Text("‹ Previous")
                }
                Text(
                    text = "Page ${gallery.page} of ${gallery.pageCount} · ${gallery.total} cakes",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                TextButton(
                    onClick = { if (gallery.page < gallery.pageCount) page = gallery.page + 1 },
                    enabled = gallery.page < gallery.pageCount,
                ) {
                    Text("Next ›")
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))
        Text(
            text = "Ready to create your own masterpiece?",
            style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(Screen.Contact) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = PrimaryForeground),
        ) {
            Text("Start Your Custom Order")
        }
    }

    if (lightboxIndex >= 0 && lightboxIndex < gallery.images.size) {
        val active = gallery.images[lightboxIndex]
        Dialog(
            onDismissRequest = { lightboxIndex = -1 },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Overlay)
                    .clickable { lightboxIndex = -1 },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = if (isWide) 840.dp else 640.dp)
                        .padding(20.dp)
                        .clickable {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OverlayStrong),
                    ) {
                        LocalImage(
                            src = active.src,
                            contentDescription = active.alt,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "${active.alt} · ${active.categoryTitle}",
                        style = TextStyle(fontSize = 13.sp, color = PrimaryForeground, textAlign = TextAlign.Center),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(
                            onClick = {
                                lightboxIndex = (lightboxIndex - 1 + gallery.images.size) % gallery.images.size
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryForeground),
                        ) {
                            Text("‹ Prev")
                        }
                        TextButton(
                            onClick = {
                                lightboxIndex = (lightboxIndex + 1) % gallery.images.size
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryForeground),
                        ) {
                            Text("Next ›")
                        }
                        TextButton(
                            onClick = { lightboxIndex = -1 },
                            colors = ButtonDefaults.textButtonColors(contentColor = PrimaryForeground),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
