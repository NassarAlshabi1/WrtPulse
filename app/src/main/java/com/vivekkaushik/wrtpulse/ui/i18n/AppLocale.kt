package com.vivekkaushik.wrtpulse.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The two languages the app speaks. [code] is what is persisted in the app's preferences and
 * what Android's own per-app language list uses; [displayName] is what the picker row shows in
 * that language's own script, so a reader of either language finds their own word for it.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية");

    companion object {
        fun fromCode(code: String?): AppLanguage? = when (code?.trim()?.lowercase()) {
            "ar" -> ARABIC
            "en" -> ENGLISH
            else -> null
        }
    }
}

/**
 * The app's chosen language, as a plain singleton as well as a CompositionLocal.
 *
 * Compose reads [LocalAppLanguage] — provided once at the app's root, so a change recomposes
 * every Text on every screen. Non-composable code (stores, toasts, background coroutines)
 * cannot read a CompositionLocal, so they read [current] instead; the root updates both
 * together, and a render that happens between the two reads the same value it just recomposed
 * against. Neither is a lie: the singleton is the source of truth, the local is what makes
 * Compose notice.
 */
object AppLocale {
    /** The language in force right now. Updated only by [apply], which keeps the two in step. */
    @Volatile
    var current: AppLanguage = AppLanguage.ENGLISH
        private set

    /** Set once by the app root; persists the choice. Not part of tests. */
    @Volatile
    var persist: ((AppLanguage) -> Unit)? = null

    /** The one way the language changes: update the singleton, then let the root recompose. */
    fun apply(language: AppLanguage) {
        current = language
        persist?.invoke(language)
    }

    fun isArabic(): Boolean = current == AppLanguage.ARABIC
}

/** Provided by the app root; every localized composable subscribes through it. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

/** The language a composable is rendering in — the CompositionLocal, not the singleton. */
@Composable
fun currentLanguage(): AppLanguage = LocalAppLanguage.current

/**
 * One UI string, in the language in force. The English text is the key: it is what the code
 * was written against and what an untranslated string falls back to, so a missing entry can
 * never blank a label or crash a screen — it simply shows English.
 *
 * Dynamic strings (numbers, hosts, model ids inside a sentence) are matched by their template
 * below when they follow one of the known shapes; anything else passes through untouched.
 */
fun trFor(language: AppLanguage, english: String): String {
    if (language == AppLanguage.ENGLISH) return english
    ArabicPatterns.translate(english)?.let { return it }
    return ArabicStrings.map[english] ?: english
}

/** [trFor] against the singleton — for stores, toasts and other non-composable call sites. */
fun tr(english: String): String = trFor(AppLocale.current, english)

/** [trFor] as a composable — subscribes to the language so a toggle recomposes the caller. */
@Composable
fun trC(english: String): String = trFor(LocalAppLanguage.current, english)
