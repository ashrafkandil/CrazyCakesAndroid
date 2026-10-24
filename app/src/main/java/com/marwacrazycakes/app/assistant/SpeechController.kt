package com.marwacrazycakes.app.assistant

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

/** Device text-to-speech, mirroring the React Native `expo-speech` helper. */
class SpeechController(context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null
    private var ready = false
    private var voices: List<Voice> = emptyList()
    private var utterance = 0

    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                voices = try {
                    engine?.voices?.toList().orEmpty()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }

    private fun pickVoice(arabic: Boolean): Voice? {
        val prefix = if (arabic) "ar" else "en"
        val matching = voices.filter { it.locale.language.lowercase(Locale.US).startsWith(prefix) }
        if (matching.isEmpty()) return null
        val preferred = matching.firstOrNull {
            Regex("natural|aria|jenny|samantha|zira|female|google us english", RegexOption.IGNORE_CASE)
                .containsMatchIn(it.name)
        }
        return preferred
            ?: matching.firstOrNull { it.locale.language.equals("en", true) && it.locale.country.equals("US", true) }
            ?: matching.first()
    }

    fun speak(text: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        val speech = engine
        if (!ready || speech == null || text.isBlank()) {
            onDone()
            return
        }
        speech.stop()
        val arabic = ARABIC.containsMatchIn(text)
        val voice = pickVoice(arabic)
        if (voice != null) speech.voice = voice
        speech.language = if (arabic) Locale("ar", "SA") else Locale.US
        if (voice != null) speech.voice = voice
        speech.setSpeechRate(1.03f)
        speech.setPitch(1.1f)

        val id = "sprinkles-${++utterance}"
        speech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                handler.post { onStart() }
            }

            override fun onDone(utteranceId: String?) {
                handler.post { onDone() }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                handler.post { onDone() }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                handler.post { onDone() }
            }
        })
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id)
        speech.speak(cleanForSpeech(text), TextToSpeech.QUEUE_FLUSH, params, id)
    }

    fun stop() {
        try {
            engine?.stop()
        } catch (_: Exception) {
            // Engine not ready.
        }
    }

    fun shutdown() {
        try {
            engine?.stop()
            engine?.shutdown()
        } catch (_: Exception) {
            // Engine already gone.
        } finally {
            engine = null
        }
    }

    private fun cleanForSpeech(text: String): String =
        text.replace(
            Regex("[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{2B00}-\\x{2BFF}]"),
            "",
        ).trim()
}
