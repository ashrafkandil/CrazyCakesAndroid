package com.marwacrazycakes.app.assistant

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.Screen
import com.marwacrazycakes.app.data.QuoteDraftStore
import com.marwacrazycakes.app.ui.theme.Background
import com.marwacrazycakes.app.ui.theme.Blush
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.CrimsonText
import com.marwacrazycakes.app.ui.theme.Foreground
import com.marwacrazycakes.app.ui.theme.Inter
import com.marwacrazycakes.app.ui.theme.Muted
import com.marwacrazycakes.app.ui.theme.MutedForeground
import com.marwacrazycakes.app.ui.theme.OverlaySoft
import com.marwacrazycakes.app.ui.theme.Primary
import com.marwacrazycakes.app.ui.theme.PrimaryForeground
import com.marwacrazycakes.app.ui.theme.Secondary
import androidx.core.content.ContextCompat
import java.util.UUID
import kotlin.math.max
import kotlinx.coroutines.launch

private fun uid(): String = UUID.randomUUID().toString().take(8)

private fun screenFor(page: AssistantPage): Screen = when (page) {
    AssistantPage.Home -> Screen.Home
    AssistantPage.About -> Screen.About
    AssistantPage.Gallery -> Screen.Gallery
    AssistantPage.Reviews -> Screen.Reviews
    AssistantPage.Pricing -> Screen.Pricing
    AssistantPage.Contact -> Screen.Contact
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantOverlay(
    page: AssistantPage,
    onNavigate: (Screen, String?) -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp
    val scope = rememberCoroutineScope()

    var open by rememberSaveable { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf(AvatarMood.IDLE) }
    var busy by remember { mutableStateOf(false) }
    var lang by rememberSaveable { mutableStateOf(Lang.EN) }
    var voiceOn by rememberSaveable { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }
    var welcomed by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<VoiceError?>(null) }

    val copy = copyFor(lang)
    val speech = remember { SpeechController(context) }
    var sendInvoker by remember { mutableStateOf<((String?, VoiceClip?) -> Unit)?>(null) }
    val recorder = remember {
        VoiceRecorder(
            context = context,
            onClip = { clip -> sendInvoker?.invoke(null, clip) },
            onError = { error -> voiceError = error },
        )
    }
    val recorderState by recorder.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            recorder.dispose()
            speech.shutdown()
        }
    }

    LaunchedEffect(voiceError) {
        val error = voiceError ?: return@LaunchedEffect
        voiceError = null
        notice = when (error) {
            VoiceError.DENIED -> copy.voiceDenied
            VoiceError.NO_MIC -> copy.voiceNoMic
            VoiceError.UNSUPPORTED -> copy.voiceUnsupported
            VoiceError.NO_SPEECH -> copy.voiceNoSpeech
            VoiceError.FAILED -> copy.voiceFailed
        }
    }

    fun stopSpeakingAll() {
        speech.stop()
        if (mood == AvatarMood.SPEAKING) mood = AvatarMood.IDLE
    }

    fun speakReply(text: String) {
        if (!voiceOn) return
        speech.speak(
            text = text,
            onStart = { mood = AvatarMood.SPEAKING },
            onDone = { if (mood == AvatarMood.SPEAKING) mood = AvatarMood.IDLE },
        )
    }

    fun runActions(actions: List<AssistantAction>): List<String> {
        val notes = mutableListOf<String>()
        var destination: Screen? = null
        var galleryCategory: String? = null
        for (action in actions) {
            when (action) {
                is AssistantAction.Navigate -> {
                    destination = screenFor(action.page)
                    notes += "Opened ${action.page.label}"
                }
                is AssistantAction.ShowGalleryCategory -> {
                    destination = Screen.Gallery
                    galleryCategory = action.category
                    notes += "Showing ${action.title}"
                }
                is AssistantAction.PrefillQuote -> {
                    QuoteDraftStore.write(action.quote)
                    destination = Screen.Contact
                    notes += "Filled in your quote request. Please review it"
                }
            }
        }
        destination?.let { onNavigate(it, galleryCategory) }
        return notes
    }

    fun send(text: String?, clip: VoiceClip?) {
        if (busy) return
        val trimmed = text?.trim().orEmpty()
        if (clip == null && trimmed.isEmpty()) return
        val words = copyFor(lang)
        stopSpeakingAll()
        notice = null
        suggestions = emptyList()
        val history = messages.filter { !it.pending }
        val userMessage = if (clip != null) {
            ChatMessage(id = uid(), role = "user", text = words.voicePending, pending = true)
        } else {
            ChatMessage(id = uid(), role = "user", text = trimmed)
        }
        messages = history + userMessage
        input = ""
        busy = true
        mood = AvatarMood.THINKING

        scope.launch {
            try {
                val turns = (if (clip != null) history else messages)
                    .takeLast(12)
                    .map { ChatTurn(role = it.role, text = it.text) }
                val data = AssistantClient.ask(
                    AssistantClient.Request(
                        messages = turns,
                        page = page,
                        lang = lang,
                        audio = clip,
                    ),
                )
                if (clip != null) {
                    val heard = data.heard?.trim().orEmpty().ifEmpty { words.voiceUnclear }
                    messages = messages.map {
                        if (it.id == userMessage.id) it.copy(text = heard, pending = false) else it
                    }
                }
                val notes = runActions(data.actions)
                val reply = ChatMessage(
                    id = uid(),
                    role = "assistant",
                    text = data.reply,
                    notes = notes,
                )
                messages = messages + reply
                suggestions = data.suggestions
                speakReply(data.reply)
            } catch (error: Exception) {
                if (clip != null) messages = messages.filter { it.id != userMessage.id }
                notice = error.message ?: words.failed
            } finally {
                busy = false
                if (mood == AvatarMood.THINKING) mood = AvatarMood.IDLE
            }
        }
    }

    SideEffect {
        sendInvoker = { text, clip -> send(text, clip) }
    }

    val micPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            recorder.start()
        } else {
            voiceError = VoiceError.DENIED
        }
    }

    fun startListening() {
        stopSpeakingAll()
        notice = null
        open = true
        if (!AssistantClient.isConfigured()) {
            notice = copyFor(lang).failed
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) recorder.start() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun openChat() {
        open = true
        if (!welcomed && messages.isEmpty()) {
            welcomed = true
            speakReply(copy.greeting)
        }
    }

    fun resetChat() {
        stopSpeakingAll()
        recorder.cancel()
        messages = emptyList()
        suggestions = emptyList()
        notice = null
        input = ""
    }

    BackHandler(enabled = open) {
        if (recorderState.recording) recorder.cancel()
        open = false
        stopSpeakingAll()
    }

    val intro = remember { Animatable(0f) }
    var introDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        intro.animateTo(1f, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
        introDone = true
    }

    val panelWidth = minOf(390.dp, widthDp - 24.dp)
    val panelMaxHeight = minOf(620.dp, heightDp - 220.dp)
    val bubbleMaxWidth = panelWidth * 0.86f
    val introTravel = with(density) { max(0f, heightDp.toPx() / 2f - 116.dp.toPx()) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, busy, notice, open) {
        if (!open) return@LaunchedEffect
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 12.dp, bottom = 160.dp)
                        .width(panelWidth)
                        .heightIn(max = panelMaxHeight)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Background)
                        .border(1.dp, Border, RoundedCornerShape(16.dp)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Blush)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Background)
                                .border(
                                    width = if (mood == AvatarMood.LISTENING) 3.dp else 1.dp,
                                    color = if (mood == AvatarMood.SPEAKING) Secondary else Primary,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            AvatarFace(mood = mood, size = 52.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sprinkles",
                                style = TextStyle(
                                    fontFamily = CrimsonText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Primary,
                                ),
                            )
                            Text(
                                text = when (mood) {
                                    AvatarMood.LISTENING -> copy.statusListening
                                    AvatarMood.THINKING -> copy.statusThinking
                                    AvatarMood.SPEAKING -> copy.statusSpeaking
                                    AvatarMood.IDLE -> copy.statusIdle
                                },
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = MutedForeground,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row {
                            HeaderIcon(icon = Icons.Filled.Language, label = "Switch language") {
                                lang = if (lang == Lang.EN) Lang.AR else Lang.EN
                                stopSpeakingAll()
                            }
                            HeaderIcon(
                                icon = if (voiceOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                label = if (voiceOn) "Mute voice" else "Turn voice on",
                            ) {
                                if (voiceOn) stopSpeakingAll()
                                voiceOn = !voiceOn
                            }
                            HeaderIcon(icon = Icons.Filled.Refresh, label = "Start over") { resetChat() }
                        }
                    }
                    HorizontalDivider(color = Border)

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 140.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp,
                            vertical = 14.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val shownMessages = if (messages.isEmpty()) {
                            listOf(ChatMessage(id = "greeting", role = "assistant", text = copy.greeting))
                        } else {
                            messages
                        }
                        items(shownMessages.size) { index ->
                            val message = shownMessages[index]
                            MessageRow(message = message, maxWidth = bubbleMaxWidth)
                        }
                        if (busy) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    ThinkingBubble()
                                }
                            }
                        }
                        notice?.let { text ->
                            item {
                                Text(
                                    text = text,
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Foreground,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Blush)
                                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }

                    val chips = if (messages.isEmpty()) copy.starters else suggestions
                    if (chips.isNotEmpty() && !busy && !recorderState.recording) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            chips.forEach { chip ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Background)
                                        .border(1.dp, Border, RoundedCornerShape(999.dp))
                                        .clickable { send(chip, null) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = chip,
                                        style = TextStyle(
                                            fontFamily = Inter,
                                            fontSize = 12.sp,
                                            color = Foreground,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Border)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (recorderState.recording) Primary else Secondary)
                                .clickable(enabled = !busy) {
                                    if (recorderState.recording) recorder.stop() else startListening()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (recorderState.recording) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = if (recorderState.recording) "Stop and send" else "Talk",
                                tint = if (recorderState.recording) PrimaryForeground else Primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        if (recorderState.recording) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Blush)
                                    .border(1.dp, Primary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                LevelBars(level = recorderState.level)
                                Text(
                                    text = if (recorderState.hearingVoice) copy.hearing else copy.speakNow,
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Foreground,
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${recorderState.seconds}s",
                                    style = TextStyle(
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = MutedForeground,
                                    ),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { recorder.cancel() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancel recording",
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            BasicTextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 38.dp, max = 110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                textStyle = TextStyle(
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = Foreground,
                                ),
                                cursorBrush = SolidColor(Primary),
                                maxLines = 4,
                                decorationBox = { inner ->
                                    Box {
                                        if (input.isEmpty()) {
                                            Text(
                                                text = copy.placeholder,
                                                style = TextStyle(
                                                    fontFamily = Inter,
                                                    fontSize = 14.sp,
                                                    color = MutedForeground,
                                                ),
                                            )
                                        }
                                        inner()
                                    }
                                },
                            )
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (busy || input.isBlank()) Primary.copy(alpha = 0.4f) else Primary)
                                    .clickable(enabled = !busy && input.isNotBlank()) {
                                        send(input, null)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = PrimaryForeground,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    Text(
                        text = copy.disclaimer,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MutedForeground,
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 84.dp)
                .graphicsLayer {
                    alpha = intro.value
                    translationY = introTravel * (1f - intro.value)
                    val faceScale = 1f + 0.6f * (1f - intro.value)
                    scaleX = faceScale
                    scaleY = faceScale
                },
            contentAlignment = Alignment.BottomEnd,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (!introDone) {
                    Text(
                        text = "AI assistance",
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = PrimaryForeground,
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Primary)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Background)
                        .border(2.dp, Primary, CircleShape)
                        .clickable {
                            if (open) {
                                open = false
                                stopSpeakingAll()
                            } else {
                                openChat()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (open) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close assistant",
                            tint = Primary,
                            modifier = Modifier.size(30.dp),
                        )
                    } else {
                        AvatarFace(mood = mood, size = 64.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun MessageRow(message: ChatMessage, maxWidth: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(
                    if (message.role == "user") {
                        RoundedCornerShape(16.dp, 16.dp, 3.dp, 16.dp)
                    } else {
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp)
                    },
                )
                .background(if (message.role == "user") Primary else Muted)
                .alpha(if (message.pending) 0.7f else 1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = message.text,
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = if (message.role == "user") PrimaryForeground else Foreground,
                ),
            )
            message.notes.forEach { note ->
                Text(
                    text = "\u2713 $note",
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Primary,
                    ),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ThinkingBubble() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp))
            .background(Muted)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val transition = rememberInfiniteTransition(label = "thinking")
        val alpha by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
            label = "dot",
        )
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(Primary),
            )
        }
    }
}

@Composable
private fun LevelBars(level: Float) {
    val weights = listOf(0.5f, 0.8f, 1f, 0.8f, 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(24.dp),
    ) {
        weights.forEach { weight ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((4f + level * weight * 20f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Primary),
            )
        }
    }
}
