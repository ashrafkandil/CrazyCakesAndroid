package com.marwacrazycakes.app.assistant

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marwacrazycakes.app.ui.theme.Blush
import com.marwacrazycakes.app.ui.theme.Border
import com.marwacrazycakes.app.ui.theme.Primary
import kotlinx.coroutines.delay

enum class AvatarMood { IDLE, LISTENING, THINKING, SPEAKING }

private const val BLINK_MS = 150L

/**
 * The 2D baker avatar from the React Native app, drawn with Compose Canvas instead
 * of SVG. Blinks every few seconds and animates its mouth while speaking.
 */
@Composable
fun AvatarFace(mood: AvatarMood, size: Dp = 64.dp) {
    var blinking by remember { mutableStateOf(false) }
    var mouthOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            blinking = true
            delay(BLINK_MS)
            blinking = false
        }
    }

    LaunchedEffect(mood) {
        if (mood != AvatarMood.SPEAKING) {
            mouthOpen = false
            return@LaunchedEffect
        }
        while (true) {
            delay(160)
            mouthOpen = !mouthOpen
        }
    }

    val eyeY = if (mood == AvatarMood.THINKING) 64f else 67f
    val eyeRy = if (blinking) 0.6f else 4f
    val pupilY = if (mood == AvatarMood.THINKING) 62.6f else 65.6f
    val browY = if (mood == AvatarMood.LISTENING) 58f else 59f
    val hair = Color(0xFF6B4130)
    val skin = Color(0xFFF7D5BD)
    val mouth = Color(0xFFA23E4A)

    Canvas(
        modifier = Modifier
            .size(size)
            .semantics { contentDescription = "Sprinkles, the Crazy Cakes assistant" },
    ) {
        val scale = this.size.width / 120f
        withTransform({ scale(scale, scale, Offset.Zero) }) {
            drawCircle(Blush, radius = 57f, center = Offset(60f, 60f))
            drawCircle(Primary, radius = 57f, center = Offset(60f, 60f), style = Stroke(width = 3f))

            val hairPath = Path().apply {
                moveTo(30f, 70f)
                cubicTo(30f, 46f, 43f, 32f, 60f, 32f)
                cubicTo(77f, 32f, 90f, 46f, 90f, 70f)
                lineTo(90f, 86f)
                cubicTo(84f, 82f, 81f, 72f, 81f, 62f)
                cubicTo(70f, 68f, 50f, 69f, 39f, 62f)
                cubicTo(39f, 72f, 36f, 82f, 30f, 86f)
                close()
            }
            drawPath(hairPath, hair)
            drawOval(skin, topLeft = Offset(35f, 43f), size = Size(50f, 54f))

            val hat = Path().apply {
                moveTo(37f, 44f)
                cubicTo(28f, 43f, 25f, 31f, 33f, 26f)
                cubicTo(36f, 17f, 48f, 16f, 52f, 23f)
                cubicTo(57f, 16f, 68f, 16f, 72f, 23f)
                cubicTo(79f, 19f, 88f, 24f, 87f, 32f)
                cubicTo(94f, 36f, 90f, 46f, 82f, 45f)
                lineTo(82f, 52f)
                lineTo(37f, 52f)
                close()
            }
            drawPath(hat, Color.White)
            drawPath(hat, Border, style = Stroke(width = 1.5f))
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(36f, 44f),
                size = Size(48f, 8f),
                cornerRadius = CornerRadius(2.5f, 2.5f),
            )
            drawRoundRect(
                color = Primary,
                topLeft = Offset(36f, 44f),
                size = Size(48f, 8f),
                cornerRadius = CornerRadius(2.5f, 2.5f),
                style = Stroke(width = 1.5f),
            )
            drawCircle(Primary, radius = 2f, center = Offset(60f, 48f))

            drawOval(Color(0xFF3B2A22), Offset(46.7f, eyeY - eyeRy), Size(6.6f, eyeRy * 2f))
            drawOval(Color(0xFF3B2A22), Offset(66.7f, eyeY - eyeRy), Size(6.6f, eyeRy * 2f))
            if (!blinking) {
                drawCircle(Color.White, radius = 1.1f, center = Offset(51.2f, pupilY))
                drawCircle(Color.White, radius = 1.1f, center = Offset(71.2f, pupilY))
            }

            val brows = Path().apply {
                if (mood == AvatarMood.LISTENING) {
                    moveTo(44f, 58f)
                    quadraticTo(50f, 54f, 55f, 58f)
                    moveTo(65f, 58f)
                    quadraticTo(70f, 54f, 76f, 58f)
                } else {
                    moveTo(45f, browY)
                    quadraticTo(50f, browY - 3f, 55f, browY)
                    moveTo(65f, browY)
                    quadraticTo(70f, browY - 3f, 75f, browY)
                }
            }
            drawPath(brows, hair, style = Stroke(width = 2f, cap = StrokeCap.Round))

            drawCircle(Primary.copy(alpha = 0.35f), radius = 4.5f, center = Offset(44f, 78f))
            drawCircle(Primary.copy(alpha = 0.35f), radius = 4.5f, center = Offset(76f, 78f))

            when (mood) {
                AvatarMood.SPEAKING -> drawOval(
                    mouth,
                    Offset(54f, 85f - (if (mouthOpen) 5f else 1.6f)),
                    Size(12f, if (mouthOpen) 10f else 3.2f),
                )
                AvatarMood.LISTENING -> drawOval(mouth, Offset(56.5f, 82f), Size(7f, 6f))
                else -> {
                    val smile = Path().apply {
                        moveTo(51f, 83f)
                        quadraticTo(60f, 91f, 69f, 83f)
                    }
                    drawPath(smile, mouth, style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
            }

            if (mood == AvatarMood.THINKING) {
                drawCircle(Primary, radius = 3f, center = Offset(88f, 30f))
                drawCircle(Primary, radius = 3.5f, center = Offset(97f, 24f))
                drawCircle(Primary, radius = 4f, center = Offset(107f, 17f))
            }
        }
    }
}
