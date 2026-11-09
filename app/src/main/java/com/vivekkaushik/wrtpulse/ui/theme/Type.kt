package com.vivekkaushik.wrtpulse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vivekkaushik.wrtpulse.R

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun sans(weight: FontWeight) = Font(
    R.font.instrument_sans_var,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun mono(weight: FontWeight) = Font(
    R.font.red_hat_mono_var,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/**
 * The Arabic companion face, listed after every Latin weight: a font family tries its fonts
 * in order for each run, so Latin text keeps Instrument Sans / Red Hat Mono exactly as drawn
 * and any Arabic run falls through to Noto Sans Arabic — joined, shaped and hinted — instead
 * of tofu. The weight axis is set the same way so an Arabic label set at 650 renders at 650.
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun arabic(weight: FontWeight) = Font(
    R.font.noto_sans_arabic_var,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val SansFamily = FontFamily(
    sans(FontWeight.Normal),
    sans(FontWeight.Medium),
    sans(FontWeight.SemiBold),
    sans(FontWeight(650)),
    sans(FontWeight.Bold),
    // Arabic fallbacks, after every Latin weight.
    arabic(FontWeight.Normal),
    arabic(FontWeight.Medium),
    arabic(FontWeight.SemiBold),
    arabic(FontWeight(650)),
    arabic(FontWeight.Bold),
)

val MonoFamily = FontFamily(
    mono(FontWeight.Normal),
    mono(FontWeight.Medium),
    mono(FontWeight.SemiBold),
    mono(FontWeight.Bold),
    // Arabic fallbacks: Arabic has no monospace tradition; the sans face reads correctly here.
    arabic(FontWeight.Normal),
    arabic(FontWeight.Medium),
    arabic(FontWeight.SemiBold),
    arabic(FontWeight.Bold),
)

/**
 * The full Material 3 type scale, filled with the app's two variable fonts and the sizes the
 * screens were drawn against, so every Material 3 component that reads a role — buttons
 * (labelLarge), chips (labelLarge), text fields (bodyLarge), bars and dialogs — speaks the
 * design's type instead of the platform default.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.24).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 24.sp,
        letterSpacing = (-0.24).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(650),
        fontSize = 14.5.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.05).sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = SansFamily,
        fontWeight = FontWeight(600),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.1.sp,
    ),
)
