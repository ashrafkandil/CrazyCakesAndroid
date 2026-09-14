package com.marwacrazycakes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.data.AppData
import com.marwacrazycakes.app.ui.components.SocialPill
import com.marwacrazycakes.app.ui.components.rememberIsWide
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground

private val socials = listOf(
    "Instagram" to "https://www.instagram.com/marwacrazycakes/",
    "TikTok" to "https://www.tiktok.com/@marwacrazycakes",
    "X (Twitter)" to "https://x.com/marwacrazycakes",
)

@Composable
fun ContactScreen() {
    val isWide = rememberIsWide()
    val eventTypes = remember { AppData.eventTypes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 56.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Let\u2019s Create Something Beautiful",
                style = TextStyle(fontFamily = CrimsonText, fontSize = 34.sp, color = Primary),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "READY TO BRING YOUR VISION TO LIFE",
                style = TextStyle(
                    fontFamily = CrimsonText,
                    fontSize = 11.sp,
                    letterSpacing = 2.7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Get in touch to discuss your custom cake needs. We're excited to be part " +
                    "of your special celebration.",
                style = TextStyle(fontSize = 15.sp, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(44.dp))

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1.3f)) { InquiryForm(eventTypes) }
                Box(modifier = Modifier.weight(0.7f)) { ContactAside() }
            }
        } else {
            InquiryForm(eventTypes)
            Spacer(modifier = Modifier.height(40.dp))
            ContactAside()
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
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
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Text(text = label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium))
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun InquiryForm(eventTypes: List<String>) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var eventType by rememberSaveable { mutableStateOf("") }
    var eventDate by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var attachment by rememberSaveable { mutableStateOf<String?>(null) }
    var theme by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var typeMenuOpen by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        attachment = uri?.lastPathSegment?.substringAfterLast('/')
    }

    val valid = name.isNotBlank() && "@" in email && phone.isNotBlank() &&
        eventType.isNotBlank() && eventDate.isNotBlank() && servings.isNotBlank() && theme.isNotBlank()

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
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Thank you!",
                    style = TextStyle(fontFamily = CrimsonText, fontSize = 28.sp, color = Primary),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Your inquiry is ready. We'll be in touch within 24 hours.",
                    style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        } else {
            Spacer(modifier = Modifier.height(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LabeledField("Name *", name) { name = it }
                LabeledField("Email *", email, keyboardType = KeyboardType.Email) { email = it }
                LabeledField("Phone *", phone, keyboardType = KeyboardType.Phone) { phone = it }
                Text(text = "Event Type *", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium))
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = eventType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select event type") },
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { typeMenuOpen = !typeMenuOpen },
                    )
                    DropdownMenu(expanded = typeMenuOpen, onDismissRequest = { typeMenuOpen = false }) {
                        eventTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    eventType = type
                                    typeMenuOpen = false
                                },
                            )
                        }
                    }
                }
                LabeledField("Event Date * (YYYY-MM-DD)", eventDate) { eventDate = it }
                LabeledField("Number of Servings *", servings, keyboardType = KeyboardType.Number) { servings = it }
                LabeledField("Event/Delivery Location", location) { location = it }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Attach image/file (optional)",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .clickable { filePicker.launch("image/*") }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = attachment ?: "Choose file",
                            style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                        )
                        Text(
                            text = "Browse",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary),
                        )
                    }
                }
                LabeledField("Tell us more about your theme! *", theme, singleLine = false) { theme = it }
                Button(
                    onClick = { submitted = true },
                    enabled = valid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = PrimaryForeground,
                        disabledContainerColor = Primary.copy(alpha = 0.4f),
                        disabledContentColor = PrimaryForeground.copy(alpha = 0.7f),
                    ),
                ) {
                    Text("Send Inquiry")
                }
            }
        }
    }
}

@Composable
private fun ContactAside() {
    val context = LocalContext.current
    SectionCard {
        Text(
            text = "Get in Touch",
            style = TextStyle(
                fontFamily = CrimsonText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Primary,
            ),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:fcabakery@gmail.com")),
                    )
                },
            ) {
                Icon(Icons.Filled.Email, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Text(
                    "fcabakery@gmail.com",
                    style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:12165718440")))
                },
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Text(
                    "216.571.8440",
                    style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Chagrin Falls, OH 44022\nServing Bainbridge, Solon, Pepper Pike, " +
                        "Twinsburg, Mayfield and nearby areas",
                    style = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            Text(
                "Follow Us",
                style = TextStyle(fontFamily = CrimsonText, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            socials.forEach { (label, url) ->
                SocialPill(label = label, url = url)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "We'll respond to your inquiry within 24 hours. For urgent requests, " +
                    "please call us directly.",
                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
