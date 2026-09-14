package com.marwacrazycakes.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marwacrazycakes.app.R

val Inter = FontFamily(
    Font(R.font.inter_400, FontWeight.Normal),
    Font(R.font.inter_500, FontWeight.Medium),
    Font(R.font.inter_600, FontWeight.SemiBold),
    Font(R.font.inter_700, FontWeight.Bold),
)

val CrimsonText = FontFamily(
    Font(R.font.crimson_text_400, FontWeight.Normal),
    Font(R.font.crimson_text_400_italic, FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.crimson_text_600, FontWeight.SemiBold),
)

val DancingScript = FontFamily(
    Font(R.font.dancing_script_400, FontWeight.Normal),
    Font(R.font.dancing_script_500, FontWeight.Medium),
    Font(R.font.dancing_script_600, FontWeight.SemiBold),
    Font(R.font.dancing_script_700, FontWeight.Bold),
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Inter, fontSize = 48.sp, fontWeight = FontWeight.Normal),
    titleLarge = TextStyle(fontFamily = Inter, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Medium),
)
