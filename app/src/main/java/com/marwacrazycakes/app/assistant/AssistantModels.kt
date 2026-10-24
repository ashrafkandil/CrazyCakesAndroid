package com.marwacrazycakes.app.assistant

enum class AssistantPage(val path: String, val label: String) {
    Home("/", "Home"),
    About("/about", "About Us"),
    Gallery("/gallery", "Gallery"),
    Reviews("/reviews", "Reviews"),
    Pricing("/pricing", "Pricing"),
    Contact("/contact", "Quote request form");

    companion object {
        fun fromPath(path: String): AssistantPage? = entries.firstOrNull { it.path == path }
    }
}

enum class Lang { EN, AR }

data class AssistantCopy(
    val greeting: String,
    val starters: List<String>,
    val placeholder: String,
    val disclaimer: String,
    val statusIdle: String,
    val statusListening: String,
    val statusThinking: String,
    val statusSpeaking: String,
    val speakNow: String,
    val hearing: String,
    val voicePending: String,
    val voiceUnclear: String,
    val failed: String,
    val voiceDenied: String,
    val voiceNoMic: String,
    val voiceUnsupported: String,
    val voiceNoSpeech: String,
    val voiceFailed: String,
)

fun copyFor(lang: Lang): AssistantCopy = when (lang) {
    Lang.EN -> AssistantCopy(
        greeting = "Hi! Welcome to Marwa Crazy Cakes. How can I help you today?",
        starters = listOf("Show me wedding cakes", "How much is a custom cake?", "I need a birthday cake"),
        placeholder = "Type a message...",
        disclaimer = "AI assistant. It can make mistakes; final prices are confirmed by quote.",
        statusIdle = "Online \u00B7 tap the mic and just talk",
        statusListening = "Listening...",
        statusThinking = "Thinking...",
        statusSpeaking = "Speaking...",
        speakNow = "Listening... speak now",
        hearing = "I hear you! I'll answer when you pause",
        voicePending = "\uD83C\uDFA4 Voice message...",
        voiceUnclear = "\uD83C\uDFA4 (voice message)",
        failed = "Sorry, something went wrong. Please try again.",
        voiceDenied = "Microphone access is blocked. Enable it for this app in your settings, then tap the mic again.",
        voiceNoMic = "I couldn't find a microphone. Check your sound settings.",
        voiceUnsupported = "Voice isn't available on this device. You can always type instead.",
        voiceNoSpeech = "I didn't hear anything. Check that your mic isn't muted, then tap the mic and speak.",
        voiceFailed = "I couldn't record your voice just now. Please try again or type instead.",
    )
    Lang.AR -> AssistantCopy(
        greeting = "\u0645\u0631\u062D\u0628\u0627\u064B! \u0623\u0647\u0644\u0627\u064B \u0628\u0643 \u0641\u064A Marwa Crazy Cakes. \u0643\u064A\u0641 \u064A\u0645\u0643\u0646\u0646\u064A \u0645\u0633\u0627\u0639\u062F\u062A\u0643 \u0627\u0644\u064A\u0648\u0645\u061F",
        starters = listOf(
            "\u0623\u0631\u0646\u064A \u0643\u0639\u0643\u0627\u062A \u0627\u0644\u0632\u0641\u0627\u0641",
            "\u0643\u0645 \u0633\u0639\u0631 \u0627\u0644\u0643\u0639\u0643\u0629 \u0627\u0644\u0645\u062E\u0635\u0635\u0629\u061F",
            "\u0623\u062D\u062A\u0627\u062C \u0643\u0639\u0643\u0629 \u0639\u064A\u062F \u0645\u064A\u0644\u0627\u062F",
        ),
        placeholder = "\u0627\u0643\u062A\u0628 \u0631\u0633\u0627\u0644\u0629...",
        disclaimer = "\u0645\u0633\u0627\u0639\u062F \u0630\u0643\u064A \u0648\u0642\u062F \u064A\u062E\u0637\u0626\u061B \u0627\u0644\u0633\u0639\u0631 \u0627\u0644\u0646\u0647\u0627\u0626\u064A \u064A\u064F\u0624\u0643\u064E\u0651\u062F \u0639\u0628\u0631 \u0639\u0631\u0636 \u0627\u0644\u0633\u0639\u0631.",
        statusIdle = "\u0645\u062A\u0635\u0644\u0629 \u00B7 \u0627\u0636\u063A\u0637 \u0627\u0644\u0645\u064A\u0643\u0631\u0648\u0641\u0648\u0646 \u0648\u062A\u062D\u062F\u0651\u062B",
        statusListening = "\u0623\u0633\u062A\u0645\u0639...",
        statusThinking = "\u0623\u0641\u0643\u0631...",
        statusSpeaking = "\u0623\u062A\u062D\u062F\u062B...",
        speakNow = "\u0623\u0633\u062A\u0645\u0639... \u062A\u062D\u062F\u0651\u062B \u0627\u0644\u0622\u0646",
        hearing = "\u0623\u0633\u0645\u0639\u0643! \u0633\u0623\u062C\u064A\u0628 \u0639\u0646\u062F\u0645\u0627 \u062A\u062A\u0648\u0642\u0641",
        voicePending = "\uD83C\uDFA4 \u0631\u0633\u0627\u0644\u0629 \u0635\u0648\u062A\u064A\u0629...",
        voiceUnclear = "\uD83C\uDFA4 (\u0631\u0633\u0627\u0644\u0629 \u0635\u0648\u062A\u064A\u0629)",
        failed = "\u0639\u0630\u0631\u0627\u064B\u060C \u062D\u062F\u062B \u062E\u0637\u0623. \u062D\u0627\u0648\u0644 \u0645\u0631\u0629 \u0623\u062E\u0631\u0649.",
        voiceDenied = "\u062A\u0645 \u062D\u0638\u0631 \u0627\u0644\u0645\u064A\u0643\u0631\u0648\u0641\u0648\u0646. \u0641\u0639\u0651\u0644\u0647 \u0645\u0646 \u0625\u0639\u062F\u0627\u062F\u0627\u062A \u0627\u0644\u062A\u0637\u0628\u064A\u0642 \u062B\u0645 \u0627\u0636\u063A\u0637 \u0627\u0644\u0645\u064A\u0643\u0631\u0648\u0641\u0648\u0646 \u0645\u0631\u0629 \u0623\u062E\u0631\u0649.",
        voiceNoMic = "\u0644\u0645 \u0623\u062C\u062F \u0645\u064A\u0643\u0631\u0648\u0641\u0648\u0646\u0627\u064B. \u062A\u062D\u0642\u0642 \u0645\u0646 \u0625\u0639\u062F\u0627\u062F\u0627\u062A \u0627\u0644\u0635\u0648\u062A.",
        voiceUnsupported = "\u0627\u0644\u0635\u0648\u062A \u063A\u064A\u0631 \u0645\u062A\u0627\u062D \u0639\u0644\u0649 \u0647\u0630\u0627 \u0627\u0644\u062C\u0647\u0627\u0632. \u064A\u0645\u0643\u0646\u0643 \u0627\u0644\u0643\u062A\u0627\u0628\u0629 \u0628\u062F\u0644\u0627\u064B \u0645\u0646 \u0630\u0644\u0643.",
        voiceNoSpeech = "\u0644\u0645 \u0623\u0633\u0645\u0639 \u0634\u064A\u0626\u0627\u064B. \u062A\u0623\u0643\u062F \u0623\u0646 \u0627\u0644\u0645\u064A\u0643\u0631\u0648\u0641\u0648\u0646 \u063A\u064A\u0631 \u0645\u0643\u062A\u0648\u0645 \u062B\u0645 \u062D\u0627\u0648\u0644 \u0645\u0631\u0629 \u0623\u062E\u0631\u0649.",
        voiceFailed = "\u062A\u0639\u0630\u0651\u0631 \u062A\u0633\u062C\u064A\u0644 \u0635\u0648\u062A\u0643 \u0627\u0644\u0622\u0646. \u062D\u0627\u0648\u0644 \u0645\u0631\u0629 \u0623\u062E\u0631\u0649 \u0623\u0648 \u0627\u0643\u062A\u0628 \u0631\u0633\u0627\u0644\u062A\u0643.",
    )
}

val ARABIC = Regex("[\u0600-\u06FF]")

sealed interface AssistantAction {
    data class Navigate(val page: AssistantPage) : AssistantAction
    data class ShowGalleryCategory(val category: String, val title: String) : AssistantAction
    data class PrefillQuote(val quote: Map<String, String>) : AssistantAction
}

data class AssistantReply(
    val heard: String?,
    val reply: String,
    val actions: List<AssistantAction>,
    val suggestions: List<String>,
)

data class ChatTurn(val role: String, val text: String)

data class ChatMessage(
    val id: String,
    val role: String,
    val text: String,
    val notes: List<String> = emptyList(),
    val pending: Boolean = false,
)

data class VoiceClip(val mimeType: String, val data: String)

class AssistantException(message: String) : Exception(message)
