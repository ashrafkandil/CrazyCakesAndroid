package com.marwacrazycakes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.marwacrazycakes.app.data.ContentRepository
import com.marwacrazycakes.app.data.QuoteDraftStore
import com.marwacrazycakes.app.data.SocialLink
import com.marwacrazycakes.app.ui.components.AppTextField
import com.marwacrazycakes.app.ui.components.SectionHeading
import com.marwacrazycakes.app.ui.components.SocialPill
import com.marwacrazycakes.app.ui.components.openDial
import com.marwacrazycakes.app.ui.components.openEmail
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Blush
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Card
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Foreground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.Overlay
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactScreen() {
    val isWide = rememberIsWide()
    val eventTypes = remember { ContentRepository.eventTypes() }
    val socialLinks = remember { ContentRepository.socialLinks() }
    val supportEmail = ContentRepository.text("contact.email")
    val supportPhoneHref = ContentRepository.text("contact.phoneHref")

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var eventType by rememberSaveable { mutableStateOf("") }
    var eventDate by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var theme by rememberSaveable { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var sendError by remember { mutableStateOf<String?>(null) }

    val draft by QuoteDraftStore.draft.collectAsState()
    LaunchedEffect(draft) {
        if (draft.isEmpty()) return@LaunchedEffect
        draft["name"]?.takeIf { it.isNotBlank() }?.let { name = it }
        draft["email"]?.takeIf { it.isNotBlank() }?.let { email = it }
        draft["phone"]?.takeIf { it.isNotBlank() }?.let { phone = it }
        draft["eventType"]?.takeIf { it in eventTypes }?.let { eventType = it }
        draft["eventDate"]?.takeIf { it.isNotBlank() }?.let { eventDate = it }
        draft["servings"]?.takeIf { it.isNotBlank() }?.let { servings = it }
        draft["location"]?.takeIf { it.isNotBlank() }?.let { location = it }
        draft["theme"]?.takeIf { it.isNotBlank() }?.let { theme = it }
        submitted = false
    }

    val isValid = name.trim().isNotEmpty() &&
        email.contains("@") &&
        phone.trim().isNotEmpty() &&
        eventType.isNotEmpty() &&
        eventDate.trim().isNotEmpty() &&
        servings.trim().isNotEmpty() &&
        theme.trim().isNotEmpty()

    val context = LocalContext.current
    fun submit() {
        if (!isValid || sending) return
        sending = true
        sendError = null
        val body = listOf(
            "Name: ${name.trim()}",
            "Email: ${email.trim()}",
            "Phone: ${phone.trim()}",
            "Event type: $eventType",
            "Event date: ${eventDate.trim()}",
            "Servings: ${servings.trim()}",
            "Location: ${location.trim().ifEmpty { "Not specified" }}",
            "",
            theme.trim(),
        ).joinToString("\n")
        val url = "mailto:$supportEmail" +
            "?subject=${Uri.encode("Custom quote request \u2014 ${name.trim()} ($eventType)")}" +
            "&body=${Uri.encode(body)}"
        try {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
            QuoteDraftStore.clear()
            submitted = true
        } catch (_: Exception) {
            sendError = "We could not open your email app. Email $supportEmail or call " +
                "${ContentRepository.text("contact.phone")} to send your request."
        } finally {
            sending = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionHeading(
            title = ContentRepository.text("contact.title"),
            eyebrow = ContentRepository.text("contact.eyebrow"),
        )
        Text(
            text = ContentRepository.text("contact.intro"),
            style = TextStyle(
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 26.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.widthIn(max = 560.dp).padding(top = 18.dp),
        )
        Spacer(modifier = Modifier.height(44.dp))

        val form = @Composable {
            SectionCard {
                Text(
                    text = "Request a Custom Quote",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Primary,
                    ),
                )
                if (submitted) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Blush)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Thank you!",
                            style = TextStyle(
                                fontFamily = com.marwacrazycakes.app.ui.theme.DancingScript,
                                fontSize = 30.sp,
                                color = Primary,
                            ),
                        )
                        Text(
                            text = "Your email draft is on its way to $supportEmail. " +
                                ContentRepository.text("contact.responseNote"),
                            style = TextStyle(
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = MutedForeground,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(top = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        LabeledField("Name *", name) { name = it }
                        LabeledField("Email *", email, keyboardType = KeyboardType.Email) { email = it }
                        LabeledField("Phone *", phone, keyboardType = KeyboardType.Phone) { phone = it }

                        Text(
                            text = "Event Type *",
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Foreground,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Border, RoundedCornerShape(8.dp))
                                .clickable { menuOpen = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = eventType.ifEmpty { "Select event type" },
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontSize = 15.sp,
                                        color = if (eventType.isEmpty()) MutedForeground else Foreground,
                                    ),
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MutedForeground,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        LabeledField(
                            "Event Date * (YYYY-MM-DD)",
                            eventDate,
                            placeholder = "2026-10-15",
                        ) { eventDate = it }
                        LabeledField(
                            "Number of Servings *",
                            servings,
                            keyboardType = KeyboardType.Number,
                        ) { servings = it }
                        LabeledField("Event/Delivery Location", location) { location = it }

                        Text(
                            text = "Tell us more about your theme! *",
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Foreground,
                            ),
                        )
                        AppTextField(
                            value = theme,
                            onValueChange = { theme = it },
                            placeholder = "Describe the dream cake\u2026",
                            singleLine = false,
                            textAreaHeight = 110.dp,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .alpha(if (!isValid || sending) 0.4f else 1f)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Primary)
                                .clickable(enabled = isValid && !sending) { submit() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (sending) "Opening your email app\u2026" else "Send Inquiry",
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = PrimaryForeground,
                                ),
                            )
                        }

                        sendError?.let { message ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Primary, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = message,
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = Foreground,
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }

        val aside = @Composable {
            ContactAside(
                supportEmail = supportEmail,
                supportPhoneHref = supportPhoneHref,
                socialLinks = socialLinks,
            )
        }

        if (isWide) {
            Row(
                modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1.3f)) {
                    Box(modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()) { form() }
                }
                Box(modifier = Modifier.weight(0.7f)) { aside() }
            }
        } else {
            Column(
                modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                form()
                aside()
            }
        }
    }

    if (menuOpen) {
        EventTypeDialog(
            eventTypes = eventTypes,
            onSelect = {
                eventType = it
                menuOpen = false
            },
            onDismiss = { menuOpen = false },
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Card)
            .border(1.dp, Border, shape)
            .padding(24.dp),
    ) {
        content()
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    onValueChange: (String) -> Unit,
) {
    Column {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Foreground,
            ),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardType = keyboardType,
            placeholder = placeholder,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactAside(
    supportEmail: String,
    supportPhoneHref: String,
    socialLinks: List<SocialLink>,
) {
    val context = LocalContext.current
    SectionCard {
        Text(
            text = ContentRepository.text("contact.sidebarHeading"),
            style = TextStyle(
                fontFamily = CrimsonText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Primary,
            ),
        )
        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.clickable { openEmail(context, supportEmail) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = supportEmail,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = MutedForeground,
                    ),
                )
            }
            Row(
                modifier = Modifier.clickable { openDial(context, supportPhoneHref) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = ContentRepository.text("contact.phone"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = MutedForeground,
                    ),
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = ContentRepository.text("contact.address") + "\n" +
                        ContentRepository.text("contact.serviceArea"),
                    style = TextStyle(
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MutedForeground,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier.padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = ContentRepository.text("contact.followUs"),
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Foreground,
                    ),
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                socialLinks.forEach { social ->
                    SocialPill(label = social.label, url = social.href)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Blush)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = ContentRepository.text("contact.responseNote"),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MutedForeground,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EventTypeDialog(
    eventTypes: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Overlay)
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Card)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {}
                    .padding(24.dp)
                    .navigationBarsPadding(),
            ) {
                Text(
                    text = "Event Type",
                    style = TextStyle(
                        fontFamily = CrimsonText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Primary,
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                eventTypes.forEachIndexed { index, type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(type) }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text = type,
                            style = TextStyle(
                                fontFamily = Inter,
                                fontSize = 16.sp,
                                color = Foreground,
                            ),
                        )
                    }
                    if (index != eventTypes.lastIndex) HorizontalDivider(color = Border)
                }
            }
        }
    }
}
