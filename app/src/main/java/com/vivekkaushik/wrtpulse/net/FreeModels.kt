package com.vivekkaushik.wrtpulse.net

import java.net.URL

/**
 * The free models the dropdown can name before the provider has said a word.
 *
 * The live list still comes from GET /models — OpenRouter's even answers without a key — but a
 * live list needs a reachable provider, and the point of a free tier is that it works before
 * anything is configured. This catalog is what the dropdown opens with: the ids that were free
 * when it was written, each one checked against its provider's own pricing or rate-limit page,
 * kept in the provider's own spelling so what the operator picks is what the endpoint takes.
 *
 * It is deliberately a floor, not a ceiling: entries merge with whatever the provider lists,
 * and a stale entry costs one error message from the provider, never a wrong answer.
 */
object FreeModels {

    /**
     * OpenRouter, verified against https://openrouter.ai/api/v1/models — the entries whose
     * prompt and completion pricing are both exactly zero, minus the music-generation ones
     * that cannot answer a chat completion.
     *
     * Curated order, not alphabetical: `openrouter/free` leads because it is OpenRouter's own
     * Free Models Router, which picks from whatever is free at the moment of the request —
     * the one id that cannot go stale, and the one the Use OpenRouter preset pre-fills.
     * Everything after it is a concrete id, stable however the router behind it changes.
     */
    val OPENROUTER = listOf(
        "openrouter/free",
        "cohere/north-mini-code:free",
        "dots-studio/dots-3-note-preview:free",
        "google/gemma-4-26b-a4b-it:free",
        "google/gemma-4-31b-it:free",
        "liquid/lfm-2.5-2.6b:free",
        "liquid/lfm-3-1.7b:free",
        "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free",
        "nvidia/nemotron-3-ultra-550b-a55b:free",
        "nvidia/nemotron-3.5-lightning:free",
        "nvidia/nemotron-3.5-super-vl:free",
        "recursive/flint-v1.0:free",
        "xingchen-agi/xing4.0-29b-a4b:free",
    )

    /**
     * Groq's free tier, per console.groq.com's rate-limits page: every model below answers on
     * a free API key with per-day request budgets. Whisper is left out — it transcribes, it
     * does not chat.
     */
    val GROQ = listOf(
        "llama-3.1-8b-instant",
        "llama-3.3-70b-versatile",
        "meta-llama/llama-4-scout-17b-16e-instruct",
        "moonshotai/kimi-k2-instruct",
        "openai/gpt-oss-120b",
        "openai/gpt-oss-20b",
        "qwen/qwen3-32b",
        "groq/compound",
        "groq/compound-mini",
        "allam-2-7b",
    )

    /**
     * Gemini's free tier on the OpenAI-compatible endpoint, per AI Studio's pricing: the
     * Flash family is free with daily quotas; the premium lines are not. The app's own
     * [AiSettings.GEMINI_MODEL] default is the first entry.
     */
    val GEMINI = listOf(
        AiSettings.GEMINI_MODEL,
        "gemini-3.6-flash",
        "gemini-3.5-flash-lite",
    )

    /** True for a host that serves OpenRouter's API — the one /models that needs no key. */
    fun isOpenRouterHost(baseUrl: String): Boolean {
        val host = AiSettings.hostOf(baseUrl).lowercase()
        return host == "openrouter.ai" || host.endsWith(".openrouter.ai")
    }

    /** True for a host that serves Groq's API. */
    fun isGroqHost(baseUrl: String): Boolean {
        val host = AiSettings.hostOf(baseUrl).lowercase()
        return host == "api.groq.com" || host.endsWith(".groq.com")
    }

    /** True for Google's own OpenAI-compatible Gemini endpoint. */
    fun isGeminiHost(baseUrl: String): Boolean {
        val host = AiSettings.hostOf(baseUrl).lowercase()
        return host == "generativelanguage.googleapis.com"
    }

    /**
     * The catalog entries for the endpoint at [baseUrl], sorted and de-duplicated; empty for
     * a host with no free tier worth naming (OpenAI proper, a custom gateway, Ollama — where
     * every model is local and the provider's own list is the only truth).
     */
    fun forBaseUrl(baseUrl: String): List<String> {
        val host = runCatching { URL(baseUrl.trim()).host.lowercase() }.getOrDefault("")
        val list = when {
            host.isEmpty() -> emptyList()
            host == "openrouter.ai" || host.endsWith(".openrouter.ai") -> OPENROUTER
            host == "api.groq.com" || host.endsWith(".groq.com") -> GROQ
            host == "generativelanguage.googleapis.com" -> GEMINI
            else -> emptyList()
        }
        return list.distinct().sorted()
    }

    /**
     * Whether one model id is known-free: OpenRouter marks its free ids with a `:free`
     * suffix, and the Free Models Router is free by construction. Used to tag the rows the
     * provider's own list contributes, so a fetched free model shows the same FREE badge the
     * catalog's do.
     */
    fun isFreeModelId(id: String): Boolean =
        id.endsWith(":free", ignoreCase = true) || id.equals("openrouter/free", ignoreCase = true)
}
