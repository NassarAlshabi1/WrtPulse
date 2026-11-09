package com.vivekkaushik.wrtpulse.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The free-model catalog: the floor the dropdown stands on before any provider is asked. These
 * hold the invariants the picker's counts and FREE badges depend on.
 */
class FreeModelsTest {

    @Test
    fun `each catalog is non-empty and free of duplicates`() {
        assertTrue(FreeModels.OPENROUTER.isNotEmpty())
        assertEquals(FreeModels.OPENROUTER.distinct().size, FreeModels.OPENROUTER.size)
        assertTrue(FreeModels.GROQ.isNotEmpty())
        assertEquals(FreeModels.GROQ.distinct().size, FreeModels.GROQ.size)
        assertTrue(FreeModels.GEMINI.isNotEmpty())
        assertEquals(FreeModels.GEMINI.distinct().size, FreeModels.GEMINI.size)
    }

    @Test
    fun `the gemini catalog leads with the preset default`() {
        // The first entry is what the Use Gemini preset pre-fills, and it must be the same id
        // the app's own default constant names — two spellings of one model would be a bug.
        assertEquals(AiSettings.GEMINI_MODEL, FreeModels.GEMINI.first())
    }

    @Test
    fun `every openrouter catalog id is known-free`() {
        FreeModels.OPENROUTER.forEach { id ->
            assertTrue("not known-free: $id", FreeModels.isFreeModelId(id))
        }
    }

    @Test
    fun `the free router leads the openrouter catalog`() {
        // The one id that cannot go stale: OpenRouter picks from whatever is free at request
        // time. Curated order or not, it has to be there, and it has to lead.
        assertEquals("openrouter/free", FreeModels.OPENROUTER.first())
    }

    @Test
    fun `forBaseUrl maps each provider to its catalog sorted and unknowns to nothing`() {
        // forBaseUrl answers sorted and de-duplicated — the dropdown's merged list is sorted,
        // and the catalog's entries fold in without re-sorting.
        assertEquals(
            FreeModels.OPENROUTER.sorted(),
            FreeModels.forBaseUrl("https://openrouter.ai/api/v1"),
        )
        assertEquals(
            FreeModels.GROQ.sorted(),
            FreeModels.forBaseUrl("https://api.groq.com/openai/v1"),
        )
        assertEquals(
            FreeModels.GEMINI.sorted(),
            FreeModels.forBaseUrl("https://generativelanguage.googleapis.com/v1beta/openai/"),
        )
        // OpenAI proper has no free tier worth naming, a custom gateway is unknown, and a
        // local Ollama lists its own models — none of them ship a canned catalog.
        assertTrue(FreeModels.forBaseUrl("https://api.openai.com/v1").isEmpty())
        assertTrue(FreeModels.forBaseUrl("http://192.168.1.100:11434/v1").isEmpty())
        assertTrue(FreeModels.forBaseUrl("https://gateway.example.com/v1").isEmpty())
        assertTrue(FreeModels.forBaseUrl("not a url").isEmpty())
    }

    @Test
    fun `host detection tolerates case and rejects lookalikes`() {
        assertTrue(FreeModels.isOpenRouterHost("https://OpenRouter.ai/api/v1"))
        assertTrue(FreeModels.isGroqHost("https://api.groq.com/openai/v1"))
        assertFalse(FreeModels.isGroqHost("https://openrouter.ai/api/v1"))
        assertFalse(FreeModels.isOpenRouterHost("https://api.openai.com/v1"))
    }

    @Test
    fun `isFreeModelId recognises the free suffix and the router and nothing else`() {
        assertTrue(FreeModels.isFreeModelId("meta-llama/llama-3.3-70b-instruct:free"))
        assertTrue(FreeModels.isFreeModelId("openrouter/free"))
        assertFalse(FreeModels.isFreeModelId("openai/gpt-4o-mini"))
        assertFalse(FreeModels.isFreeModelId("llama-3.1-8b-instant"))
    }
}
