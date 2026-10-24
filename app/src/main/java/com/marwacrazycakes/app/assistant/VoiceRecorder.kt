package com.marwacrazycakes.app.assistant

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class VoiceError { DENIED, NO_MIC, UNSUPPORTED, NO_SPEECH, FAILED }

data class RecorderState(
    val recording: Boolean = false,
    val level: Float = 0f,
    val seconds: Int = 0,
    val hearingVoice: Boolean = false,
    val busy: Boolean = false,
)

/**
 * Records a compact mono AAC clip for Gemini, matching the React Native
 * `src/assistant/voiceRecorder.ts` flow (record, auto/manual stop, base64 encode).
 */
class VoiceRecorder(
    private val context: Context,
    private val onClip: (VoiceClip) -> Unit,
    private val onError: (VoiceError) -> Unit,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(RecorderState())
    val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var ticker: Job? = null
    private var startedAt = 0L
    private var heardVoice = false
    private var finishing = false

    fun start() {
        if (recorder != null || finishing) return
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            onError(VoiceError.NO_MIC)
            return
        }
        val output = File(context.cacheDir, "sprinkles-voice-${System.currentTimeMillis()}.m4a")
        _state.value = RecorderState(busy = true)
        try {
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioSamplingRate(16_000)
            mediaRecorder.setAudioChannels(1)
            mediaRecorder.setAudioEncodingBitRate(32_000)
            mediaRecorder.setOutputFile(output.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()

            recorder = mediaRecorder
            outputFile = output
            startedAt = System.currentTimeMillis()
            heardVoice = false
            finishing = false
            _state.value = RecorderState(recording = true)
            ticker = scope.launch {
                while (isActive) {
                    delay(120)
                    val active = recorder ?: break
                    val elapsed = System.currentTimeMillis() - startedAt
                    val amplitude = try {
                        active.maxAmplitude
                    } catch (_: Exception) {
                        0
                    }
                    val level = (amplitude / 32767f).coerceIn(0f, 1f)
                    if (level > 0.08f) heardVoice = true
                    _state.value = RecorderState(
                        recording = true,
                        level = level,
                        seconds = (elapsed / 1000).toInt(),
                        hearingVoice = heardVoice,
                    )
                    if (elapsed >= 20_000) {
                        finish(true)
                        break
                    }
                }
            }
        } catch (error: Exception) {
            releaseRecorder()
            output.delete()
            _state.value = RecorderState()
            onError(
                if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                    VoiceError.FAILED
                } else {
                    VoiceError.NO_MIC
                },
            )
        }
    }

    fun stop() = finish(true)

    fun cancel() = finish(false)

    private fun finish(send: Boolean) {
        if (finishing || recorder == null) return
        finishing = true
        ticker?.cancel()
        ticker = null
        val elapsed = System.currentTimeMillis() - startedAt
        val capturedVoice = heardVoice
        val file = outputFile
        try {
            try {
                recorder?.stop()
            } catch (_: Exception) {
                // Too short to finalize.
            }
            releaseRecorder()
            _state.value = RecorderState()
            if (!send) {
                file?.delete()
                return
            }
            if (elapsed < 400 || !capturedVoice) {
                file?.delete()
                onError(VoiceError.NO_SPEECH)
                return
            }
            if (file == null || !file.exists() || file.length() == 0L) {
                onError(VoiceError.FAILED)
                return
            }
            val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
            onClip(VoiceClip(mimeType = "audio/m4a", data = base64))
        } catch (_: Exception) {
            if (send) onError(VoiceError.FAILED)
        } finally {
            file?.delete()
            outputFile = null
            finishing = false
        }
    }

    private fun releaseRecorder() {
        try {
            recorder?.reset()
        } catch (_: Exception) {
            // Ignore cleanup failures.
        }
        try {
            recorder?.release()
        } catch (_: Exception) {
            // Ignore cleanup failures.
        }
        recorder = null
    }

    fun dispose() {
        ticker?.cancel()
        ticker = null
        try {
            recorder?.stop()
        } catch (_: Exception) {
            // Not recording.
        }
        releaseRecorder()
        outputFile?.delete()
        outputFile = null
        scope.cancel()
    }
}
