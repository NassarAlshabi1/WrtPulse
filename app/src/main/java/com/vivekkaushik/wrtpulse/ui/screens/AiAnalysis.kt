package com.vivekkaushik.wrtpulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.vivekkaushik.wrtpulse.data.AiReport
import com.vivekkaushik.wrtpulse.data.AiReportPdf
import com.vivekkaushik.wrtpulse.data.AiHistoryJson
import com.vivekkaushik.wrtpulse.data.AiStore
import com.vivekkaushik.wrtpulse.net.AiChat
import com.vivekkaushik.wrtpulse.net.AiError
import com.vivekkaushik.wrtpulse.net.AiSettings
import com.vivekkaushik.wrtpulse.net.FreeModels
import com.vivekkaushik.wrtpulse.ops.AiPrompt
import com.vivekkaushik.wrtpulse.ops.CommandSafety
import com.vivekkaushik.wrtpulse.ops.Finding
import com.vivekkaushik.wrtpulse.ops.Severity
import com.vivekkaushik.wrtpulse.ui.FlexSpacer
import com.vivekkaushik.wrtpulse.ui.GhostButton
import com.vivekkaushik.wrtpulse.ui.MonoTag
import com.vivekkaushik.wrtpulse.ui.PrimaryButton
import com.vivekkaushik.wrtpulse.ui.SectionLabel as BaseSectionLabel
import com.vivekkaushik.wrtpulse.ui.StatusDot
import com.vivekkaushik.wrtpulse.ui.Text
import com.vivekkaushik.wrtpulse.ui.WrtIcons
import com.vivekkaushik.wrtpulse.ui.FilterChip
import com.vivekkaushik.wrtpulse.ui.i18n.AppLanguage
import com.vivekkaushik.wrtpulse.ui.i18n.trC
import com.vivekkaushik.wrtpulse.ui.mono
import com.vivekkaushik.wrtpulse.ui.sans
import com.vivekkaushik.wrtpulse.ui.theme.Wrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Mono section label — same as the shared one; the string localizes inside it. */
@Composable
private fun SectionLabel(
    text: String,
    color: Color = Wrt.TextDim,
    size: Float = 10f,
    tracking: Double = 0.12,
    modifier: Modifier = Modifier,
) {
    BaseSectionLabel(text, color, size, tracking, modifier)
}

/** Field label: a section label at the field's own size. */
@Composable
private fun LocalizedFieldLabel(text: String) {
    SectionLabel(text, size = 10f, tracking = 0.12)
}

@Composable
private fun CheckOutputField(value: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = mono(11f, 500, Wrt.TextSecondary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, autoCorrectEnabled = false),
        cursorBrush = SolidColor(Wrt.Accent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .height(132.dp)
            .border(1.dp, Wrt.BorderInput, RoundedCornerShape(10.dp))
            .background(Wrt.BgDeep, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

/**
 * The AI analysis screen: read the router over SSH, sample its counters across a minute,
 * diagnose locally on the phone, then — only on a press — send the redacted evidence to the
 * chat endpoint the operator configured and stream the answer.
 *
 * The app's four principles carry over literally: the command is shown (the payload is the
 * command's output), nothing runs without a press, nothing is irreversible, and the router
 * stays the source of truth — the provider's answer is advice, not state. The commands the
 * answer recommends are guarded too: read-only ones copy in one tap, changing ones ask twice,
 * destructive ones are never offered.
 */
@Composable
fun AiAnalysisScreen(
    store: AiStore?,
    settings: AiSettings,
    onBack: () -> Unit,
    onSaveSettings: (AiSettings) -> Unit,
    /** The app-wide language, and the one switch that changes it — this screen keeps a
     *  toggle of its own because it is where the reader of either language lands first. */
    language: AppLanguage = AppLanguage.ENGLISH,
    onLanguage: (AppLanguage) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }
    var showPayload by remember { mutableStateOf(false) }
    var savedNote by remember { mutableStateOf<String?>(null) }
    var hideSensitive by remember { mutableStateOf(true) }
    var analysisJob by remember { mutableStateOf<Job?>(null) }
    var verificationOutput by remember { mutableStateOf("") }
    val arabic = language == AppLanguage.ARABIC

    // One read on arrival, like every other screen: a snapshot the user has not asked to
    // refresh is exactly as stale as the last time they looked at it.
    LaunchedEffect(store) {
        if (store?.snapshot == null) store?.collect()
        store?.loadHistory()
    }

    Column(Modifier.fillMaxSize().background(Wrt.BgScreen)) {
        FormTopBar(trC("Router analysis"), onBack)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (arabic) "العربية" else "English", style = sans(10f, 600, Wrt.AccentBody))
            Spacer(Modifier.width(8.dp))
            MonoTag(if (arabic) "ع" else "EN", color = Wrt.Accent, border = Wrt.BorderInput)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.clickable {
                onLanguage(if (arabic) AppLanguage.ENGLISH else AppLanguage.ARABIC)
            }) {
                Text(
                    if (arabic) "التبديل إلى الإنجليزية" else "Switch to Arabic",
                    style = sans(10f, 600, Wrt.Accent),
                )
            }
        }
        if (store == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Connect to a router to analyze it.", style = sans(12f, 500, Wrt.TextDim))
            }
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            item { AnalysisStagesCard(store) }
            item { DataFreshnessCard(store) }
            item { HowItWorksCard() }
            item {
                ProviderCard(
                    settings = settings,
                    savedNote = savedNote,
                    onSave = { saved ->
                        onSaveSettings(saved)
                        savedNote = "Saved — analyses will use ${saved.baseUrl.trim().trimEnd('/')}"
                    },
                )
            }
            item { HealthCard(store) }
            item {
                SampleCard(
                    store = store,
                    busy = store.collecting,
                    onSample = { scope.launch { store.sample() } },
                )
            }
            item { LocalCard(store) }
            item {
                ModesCard(
                    question = question,
                    onPick = { q -> question = q },
                )
            }
            item {
                DataCard(
                    store = store,
                    settings = settings,
                    question = question,
                    showPayload = showPayload,
                    onTogglePayload = { showPayload = !showPayload },
                    onReadAgain = { scope.launch { store.collect() } },
                )
            }
            item {
                AskCard(
                    store = store,
                    settings = settings,
                    question = question,
                    onQuestion = { question = it },
                    onAnalyze = { analysisJob = scope.launch { store.analyze(settings, question) } },
                    onCancel = { analysisJob?.cancel(); analysisJob = null },
                )
            }
            store.answer?.let { text ->
                item { DiagnosisSummaryCard(store) }
                item { RepairPlanCard(store, text) }
                item {
                    VerificationCard(
                        store = store,
                        output = verificationOutput,
                        onOutput = { verificationOutput = it },
                        onReanalyze = {
                            if (settings.configured && verificationOutput.isNotBlank()) {
                                analysisJob = scope.launch {
                                    store.analyze(
                                        settings,
                                        "${question.trim()}\n\nAdditional check output from the user:\n${verificationOutput.trim()}",
                                    )
                                }
                            }
                        },
                    )
                }
                item { AnswerCard(store, text) }
            }
            item {
                AnalysisHistoryCard(
                    store = store,
                    onOpen = { entry ->
                        question = entry.question
                        store.showSavedAnswer(entry.answer)
                    },
                    onDelete = { id -> scope.launch { store.deleteHistory(id) } },
                )
            }
            item {
                ExportCard(
                    store = store,
                    question = question,
                    hideSensitive = hideSensitive,
                    onHideSensitive = { hideSensitive = it },
                )
            }
        }
    }
}

@Composable
private fun AnalysisStagesCard(store: AiStore) {
    val active = store.phase
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("ANALYSIS STAGES", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag(
                when (active) {
                    "reading_router" -> "Reading router"
                    "sampling_performance" -> "Analyzing metrics"
                    "preparing_solutions" -> "Preparing solutions"
                    "ready" -> "Ready for review"
                    else -> "Waiting to start"
                },
                color = if (active == "idle") Wrt.TextDim else Wrt.AccentBody,
                border = Wrt.BorderInput,
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StageStep("Reading router", active == "reading_router", store.snapshot != null)
            StageConnector(store.snapshot != null)
            StageStep("Analyzing metrics", active == "sampling_performance", store.health != null)
            StageConnector(store.health != null)
            StageStep("Preparing solutions", active == "preparing_solutions", store.answer != null)
        }
        Text(
            "Reading and analysis are view-only. The app never runs change commands on its own, " +
                "and a partial result is kept when a request is stopped.",
            style = sans(10.5f, 450, Wrt.TextDim),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun RowScope.StageStep(label: String, active: Boolean, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        StatusDot(if (active) Wrt.Accent else if (done) Wrt.Green else Wrt.DotOff, 7.dp, active)
        Text(label, style = sans(8.5f, if (active) 650 else 500, if (active) Wrt.Accent else Wrt.TextDim), modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun StageConnector(done: Boolean) {
    Box(Modifier.width(14.dp).height(1.dp).background(if (done) Wrt.Green else Wrt.BorderInput))
}

@Composable
private fun DataFreshnessCard(store: AiStore) {
    val fmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("DATA STATUS", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag(if (store.snapshot == null) "no reading yet" else "local data", color = Wrt.AccentBody, border = Wrt.BorderInput)
        }
        FreshnessRow("Last router read", store.lastSnapshotAtMs?.let { fmt.format(Date(it)) } ?: "not read yet")
        FreshnessRow("Last performance sample", store.lastSampleAtMs?.let { fmt.format(Date(it)) } ?: "not sampled yet")
        if (store.unavailableSections.isNotEmpty()) {
            Text("Unavailable sections: ${store.unavailableSections.joinToString(", ")}", style = sans(10f, 550, Wrt.AmberText), modifier = Modifier.padding(top = 8.dp))
        } else if (store.snapshot != null) {
            Text("All core reading sections are available.", style = sans(10f, 500, Wrt.Green), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun FreshnessRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = sans(10.5f, 500, Wrt.TextSecondary))
        FlexSpacer()
        Text(value, style = mono(10f, 600, if (value.startsWith("not ")) Wrt.TextDim else Wrt.AccentBody))
    }
}

@Composable
private fun HowItWorksCard() {
    Card {
        SectionLabel("HOW IT WORKS", size = 9.5f, tracking = 0.14)
        Text(
            "Three layers, in order. First the local rules: obvious problems — weak signal, " +
                "climbing tx-failed, a full /overlay, two DHCP scopes — are found on the phone " +
                "in milliseconds, with the evidence and the command behind each. Second, the " +
                "60-second sample: counters only mean something when two samples are differenced, " +
                "so a mesh link can be proven weak instead of suspected. Third, the provider: " +
                "the redacted payload, previewed in full, streamed only when you press Analyze.",
            style = sans(11.5f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "The answer includes a diagnosis and a prioritized repair plan. Review every step: " +
                "commands are advice only, are never run by the app, and changes must be checked " +
                "before you choose to copy them.",
            style = sans(10.5f, 500, Wrt.TextDim),
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            "OpenWrt Network Expert is enabled: radio capability, channel survey, client link " +
                "quality, WAN/DNS/DHCP, logs, WDS/802.11s and BATMAN-ADV are collected when " +
                "the router exposes them. Missing tools are reported as unavailable, never as " +
                "proof that a subsystem is absent.",
            style = sans(10f, 500, Wrt.AccentBody),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Endpoint, model and key. Any OpenAI-compatible /chat/completions provider works. */
@Composable
private fun ProviderCard(
    settings: AiSettings,
    savedNote: String?,
    onSave: (AiSettings) -> Unit,
) {
    // remember(settings): the fields follow the saved state — a save recomposes the screen
    // with the new value and the fields settle to it; while editing, the old value holds.
    var endpoint by remember(settings) { mutableStateOf(settings.baseUrl) }
    var model by remember(settings) { mutableStateOf(settings.model) }
    var key by remember(settings) { mutableStateOf(settings.apiKey) }
    var revealKey by remember { mutableStateOf(false) }

    // The model list belongs to the provider, so it is remembered per endpoint+key and left
    // behind when either changes — a list from the wrong provider would be a lie.
    var models by remember(endpoint, key) { mutableStateOf<List<String>>(emptyList()) }
    var modelsLoading by remember(endpoint, key) { mutableStateOf(false) }
    var modelsNote by remember(endpoint, key) { mutableStateOf<String?>(null) }
    var modelsError by remember(endpoint, key) { mutableStateOf<String?>(null) }
    var modelsTick by remember(endpoint, key) { mutableStateOf(0) }

    // The list loads itself: as soon as the provider can answer it — a base URL that parses,
    // and a key unless the endpoint is on this network — the dropdown fills without a press.
    // OpenRouter is the one provider whose /models answers with no key at all, so its free
    // list loads the moment its preset is tapped. The 400ms pause keeps the fetch off every
    // keystroke; a restart cancels the run before it.
    LaunchedEffect(endpoint, key, modelsTick) {
        val trimmed = endpoint.trim()
        val url = runCatching { URL(trimmed) }.getOrNull()
        val local = AiSettings.isLocalHost(url?.host.orEmpty())
        val openRouter = url != null && FreeModels.isOpenRouterHost(trimmed)
        when {
            trimmed.isEmpty() || url == null -> modelsNote = null
            url.protocol.equals("http", true) && !local -> modelsNote = null
            key.isBlank() && !local && !openRouter ->
                modelsNote = "Add the API key and the provider's full model list loads by itself."
            else -> {
                delay(400)
                modelsLoading = true
                modelsError = null
                modelsNote = "loading the model list from ${url?.host}…"
                try {
                    models = AiChat.listModels(
                        AiSettings(baseUrl = trimmed, model = "probe", apiKey = key.trim()),
                        freeOnly = openRouter,
                    )
                    modelsNote = if (openRouter) {
                        "${models.size} free OpenRouter models available — pick one or type any id."
                    } else {
                        "${models.size} models from ${url?.host} — pick one or type any id."
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: AiError) {
                    models = emptyList()
                    modelsError = "The list did not load: ${e.message}"
                    modelsNote = null
                } finally {
                    modelsLoading = false
                }
            }
        }
    }

    Card {
        SectionLabel("AI PROVIDER", size = 9.5f, tracking = 0.14)
        Text(
            "Any OpenAI-compatible chat endpoint: OpenAI, OpenRouter, Gemini, Groq, your own " +
                "gateway, or an Ollama server on your LAN — nothing leaves the network with that " +
                "one. Free models need no payment: OpenRouter marks its own with :free, Groq and " +
                "Gemini answer on a free-tier key, and a local Ollama is free by definition. The " +
                "key is sealed with the phone's keystore, like a router password.",
            style = sans(11f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(Modifier.padding(top = 8.dp)) {
            Box(Modifier.weight(1f).padding(end = 4.dp)) {
                GhostButton("Use OpenAI") {
                    endpoint = AiSettings.DEFAULT_BASE_URL
                    model = AiSettings.DEFAULT_MODEL
                }
            }
            Box(Modifier.weight(1f).padding(start = 4.dp)) {
                GhostButton("Use OpenRouter") {
                    endpoint = AiSettings.OPENROUTER_BASE_URL
                    model = FreeModels.OPENROUTER.first()
                }
            }
        }
        Row(Modifier.padding(top = 8.dp)) {
            Box(Modifier.weight(1f).padding(end = 4.dp)) {
                GhostButton("Use Gemini") {
                    endpoint = AiSettings.GEMINI_BASE_URL
                    model = AiSettings.GEMINI_MODEL
                }
            }
            Box(Modifier.weight(1f).padding(start = 4.dp)) {
                GhostButton("Use Ollama (local)") {
                    endpoint = AiSettings.OLLAMA_BASE_URL
                    model = AiSettings.OLLAMA_MODEL
                    key = ""
                }
            }
        }
        if (AiSettings.isLocalHost(AiSettings.hostOf(endpoint))) {
            Text(
                "Local endpoint — no API key needed, and the analysis never leaves your network. " +
                    "Edit the host to wherever Ollama runs ($endpoint).",
                style = sans(10f, 500, Wrt.AccentBody),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        LocalizedFieldLabel("BASE URL")
        FormTextField(endpoint, { endpoint = it })

        // The dropdown's two sources: the curated free catalog for this provider (always
        // there, no key, no network) and whatever /models fetched. Merged and de-duplicated,
        // with the known-free ids kept apart so the picker can badge and filter them.
        val catalogFree = remember(endpoint) { FreeModels.forBaseUrl(endpoint) }
        val merged = remember(models, catalogFree) {
            (catalogFree + models).distinct().sorted()
        }
        val freeIds = remember(merged, catalogFree) {
            val set = mutableSetOf<String>()
            set.addAll(catalogFree)
            merged.forEach { if (FreeModels.isFreeModelId(it)) set += it }
            set
        }
        val openRouter = FreeModels.isOpenRouterHost(endpoint)
        LocalizedFieldLabel(if (openRouter) "MODEL · FREE ON OPENROUTER" else "MODEL")
        ModelField(
            value = model,
            onValue = { model = it },
            options = merged,
            freeIds = freeIds,
            loading = modelsLoading,
            onRefresh = { modelsTick++ },
        )
        if (modelsError != null) {
            Text(
                modelsError!!,
                style = sans(10f, 500, Wrt.Amber),
                modifier = Modifier.padding(top = 6.dp),
            )
        } else if (modelsNote != null) {
            Text(
                modelsNote!!,
                style = sans(10f, 500, if (modelsLoading) Wrt.Accent else Wrt.TextDim),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        LocalizedFieldLabel("API KEY")
        FormTextField(
            key,
            { key = it },
            password = true,
            masked = !revealKey,
            trailing = {
                Icon(
                    WrtIcons.Eye,
                    if (revealKey) "hide the key" else "show the key",
                    Modifier.size(16.dp).clickable { revealKey = !revealKey },
                    tint = Wrt.TextDim,
                )
            },
        )
        if (savedNote != null) {
            Text(savedNote, style = mono(10f, 500, Wrt.Accent), modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton(
            if (settings.configured) "Save provider" else "Save provider to enable analysis",
            color = if (endpoint.isBlank() || model.isBlank()) Wrt.TextDim else Wrt.Accent,
        ) {
            onSave(
                AiSettings(
                    baseUrl = endpoint.trim(),
                    model = model.trim(),
                    apiKey = key.trim(),
                ),
            )
        }
    }
}

/**
 * The model field: a text field that is also its own dropdown, for real.
 *
 * Two ways down into the same list. The chevron in the field opens the picker; the full-width
 * browse row under it — the one the eye lands on — opens it too, and says how many models are
 * waiting and how many of them are free before it is opened. The picker is a dialog with a
 * search box, an All/Free filter when free ids exist, and a scrollable list — it works before
 * any key is typed and before any fetch succeeds, because the curated free catalog is in the
 * options from the first frame. Typing stays first-class: the field takes a hand-typed id, and
 * the picker's search ends in a “use what you typed” row rather than a dead end.
 */
@Composable
private fun ModelField(
    value: String,
    onValue: (String) -> Unit,
    options: List<String>,
    freeIds: Set<String>,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    Column {
        Box {
            FormTextField(
                value,
                onValue,
                trailing = {
                    Icon(
                        WrtIcons.ChevronDown,
                        if (options.isEmpty()) "load the model list" else "pick a model",
                        Modifier.size(16.dp).clickable {
                            if (options.isEmpty() && !loading) onRefresh()
                            pickerOpen = true
                        },
                        tint = if (options.isEmpty() && !loading) Wrt.TextDim else Wrt.Accent,
                    )
                },
            )
        }
        // The dropdown's own row: the whole width opens the picker, and it never lies about
        // what is inside — the counts are the counts the picker will show.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .border(1.dp, Wrt.BorderInput, RoundedCornerShape(10.dp))
                .background(Wrt.BgDeep, RoundedCornerShape(10.dp))
                .clickable {
                    if (options.isEmpty() && !loading) onRefresh()
                    pickerOpen = true
                }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                WrtIcons.ChevronDown,
                null,
                Modifier.size(14.dp),
                tint = Wrt.Accent,
            )
            Text(
                if (loading) "loading the model list…" else "Pick from the list",
                style = sans(11f, 600, Wrt.Accent),
            )
            FlexSpacer()
            if (!loading) {
                MonoTag("${options.size}", color = Wrt.TextSecondary, border = Wrt.BorderInput)
                if (freeIds.isNotEmpty()) {
                    MonoTag("${freeIds.size} FREE", color = Wrt.Green, border = Wrt.Green.copy(alpha = 0.4f))
                }
            }
        }
    }
    if (pickerOpen) {
        ModelPickerDialog(
            current = value,
            options = options,
            freeIds = freeIds,
            loading = loading,
            onRefresh = onRefresh,
            onDismiss = { pickerOpen = false },
            onPick = {
                onValue(it)
                pickerOpen = false
            },
        )
    }
}

/**
 * The real dropdown: every option in a scrollable, searchable, filterable list. Free ids are
 * badged and can be filtered to; the current id is checked; a search that matches nothing
 * offers what was typed as the id itself — the autocomplete shape, so typing never dead-ends.
 */
@Composable
private fun ModelPickerDialog(
    current: String,
    options: List<String>,
    freeIds: Set<String>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var freeOnly by remember(options, freeIds) { mutableStateOf(false) }
    val needle = query.trim()
    val filtered = remember(options, freeIds, needle, freeOnly) {
        options.filter { id ->
            (!freeOnly || id in freeIds) &&
                (needle.isEmpty() || id.contains(needle, ignoreCase = true))
        }
    }
    val exact = needle.isNotEmpty() && options.any { it.equals(needle, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Wrt.BorderCard, RoundedCornerShape(16.dp))
                .background(Wrt.BgBar, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .heightIn(max = 540.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pick a model", style = sans(15f, 650))
                FlexSpacer()
                if (loading) {
                    Box(Modifier.size(8.dp).background(Wrt.Accent, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("loading…", style = mono(9.5f, 600, Wrt.Accent))
                } else {
                    Icon(
                        WrtIcons.Reboot,
                        "reload the model list",
                        Modifier.size(15.dp).clickable { onRefresh() },
                        tint = Wrt.TextDim,
                    )
                }
            }
            Text(
                if (freeIds.isNotEmpty()) {
                    "${options.size} models · ${freeIds.size} of them free"
                } else {
                    "${options.size} models from this provider"
                },
                style = sans(10f, 500, Wrt.TextDim),
                modifier = Modifier.padding(top = 3.dp),
            )
            Spacer(Modifier.height(10.dp))
            FormTextField(query, { query = it })
            if (freeIds.isNotEmpty()) {
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip("All", !freeOnly) { freeOnly = false }
                    FilterChip("Free · ${freeIds.size}", freeOnly) { freeOnly = true }
                }
            }
            LazyColumn(
                Modifier
                    .weight(1f, fill = false)
                    .padding(top = 8.dp)
                    .heightIn(max = 330.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (needle.isNotEmpty() && !exact) {
                    item {
                        ModelRow(
                            id = needle,
                            free = needle in freeIds,
                            current = false,
                            highlight = true,
                            onClick = { onPick(needle) },
                        )
                    }
                }
                if (filtered.isEmpty() && (needle.isEmpty() || exact)) {
                    item {
                        Text(
                            if (loading) "loading the model list…"
                            else if (options.isEmpty()) "no list yet — type an id by hand or reload"
                            else "nothing matches — type the id by hand",
                            style = sans(11f, 450, Wrt.TextDim),
                            modifier = Modifier.padding(vertical = 10.dp),
                        )
                    }
                }
                items(filtered) { id ->
                    ModelRow(
                        id = id,
                        free = id in freeIds,
                        current = id.equals(current, ignoreCase = true),
                        highlight = false,
                        onClick = { onPick(id) },
                    )
                }
            }
            Text(
                "Typing in the field above filters; picking sets the model. The id can also be typed straight into the main field.",
                style = sans(9f, 450, Wrt.TextFaint),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/** One row of the picker: the id in mono, a FREE badge when known free, a check when current. */
@Composable
private fun ModelRow(
    id: String,
    free: Boolean,
    current: Boolean,
    highlight: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when {
                    current -> Wrt.Accent.copy(alpha = 0.55f)
                    highlight -> Wrt.Accent.copy(alpha = 0.35f)
                    else -> Wrt.BorderHair
                },
                RoundedCornerShape(9.dp),
            )
            .background(if (current) Wrt.BgCode else Wrt.BgCard, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Model ids are Latin whatever the app's language is; the row keeps its own direction
        // so an id reads left-to-right inside an RTL screen.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                if (highlight) "Use \"$id\" as the model id" else id,
                style = mono(10.5f, if (current) 650 else 500, if (highlight) Wrt.Accent else Wrt.TextSecondary),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (free) MonoTag("FREE", color = Wrt.Green, border = Wrt.Green.copy(alpha = 0.4f))
        if (current) Icon(WrtIcons.Check, null, Modifier.size(14.dp), tint = Wrt.Accent)
    }
}

/** The health card: one number, four subscores, every point tied to a measurement. */
@Composable
private fun HealthCard(store: AiStore) {
    val health = store.health ?: return
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("NETWORK HEALTH", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag(health.grade.label, color = scoreColor(health.overall), border = scoreColor(health.overall))
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${health.overall}",
                style = sans(40f, 750, scoreColor(health.overall)),
            )
            Text(
                "/100",
                style = sans(15f, 650, Wrt.TextDim),
                modifier = Modifier.padding(start = 2.dp, top = 12.dp),
            )
            FlexSpacer()
            if (store.temporal == null) {
                Text(
                    "single sweep — run the\n60-second sample below",
                    style = sans(9.5f, 450, Wrt.TextDim),
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
        SubScore("Wi-Fi", health.wifi)
        SubScore("Mesh", health.mesh, note = if (health.mesh == null) "not configured" else null)
        SubScore("Performance", health.performance)
        SubScore("Security", health.security)
    }
}

@Composable
private fun SubScore(name: String, value: Int?, note: String? = null) {
    Column(Modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, style = sans(11f, 600))
            FlexSpacer()
            if (note != null) {
                Text(note, style = sans(9.5f, 450, Wrt.TextDim), modifier = Modifier.padding(end = 8.dp))
            }
            if (value != null) Text("$value", style = mono(11f, 650, scoreColor(value)))
            else Text("—", style = mono(11f, 650, Wrt.TextDim))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(4.dp)
                .background(Wrt.BgCode, RoundedCornerShape(2.dp)),
        ) {
            if (value != null) {
                Box(
                    Modifier
                        .fillMaxWidth((value / 100f).coerceIn(0.02f, 1f))
                        .height(4.dp)
                        .background(scoreColor(value), RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Wrt.Green
    score >= 60 -> Wrt.Amber
    else -> Wrt.Red
}

/** The 60-second performance sample — the difference between a number and a trend. */
@Composable
private fun SampleCard(
    store: AiStore,
    busy: Boolean,
    onSample: () -> Unit,
) {
    val t = store.temporal
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("60-SECOND PERFORMANCE SAMPLE", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            if (store.sampling) {
                Box(Modifier.size(7.dp).background(Wrt.Accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("sampling…", style = mono(9.5f, 600, Wrt.Accent))
            }
        }
        Text(
            "Three samples, thirty seconds apart, differenced on the phone: retries, failed " +
                "frames, packet loss, latency, CPU, RAM and per-interface throughput. This is " +
                "what turns 'tx failed is 21' into 'tx failed grew by 21 this minute, on that link'.",
            style = sans(11f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        if (t != null) {
            Column(Modifier.padding(top = 8.dp)) {
                t.summaryLines().forEach { line ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Box(
                            Modifier
                                .padding(top = 7.dp)
                                .size(3.dp)
                                .background(Wrt.TextTertiary, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(line, style = mono(10.5f, 500, Wrt.TextSecondary))
                    }
                }
            }
        }
        if (store.sampleStatus != null) {
            Text(
                store.sampleStatus!!,
                style = mono(10f, 600, Wrt.Accent),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "Keep this screen open while it samples — leaving stops the run at the next " +
                    "tick and keeps the samples already taken.",
                style = sans(9.5f, 450, Wrt.TextDim),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton(
            when {
                store.sampling -> "Sampling the router…"
                t != null -> "Sample again"
                else -> "Sample for 60 seconds"
            },
            color = if (store.sampling || busy) Wrt.TextDim else Wrt.Accent,
            textColor = if (store.sampling || busy) Wrt.OnAccent else Wrt.BgScreen,
        ) {
            if (!store.sampling && !busy) onSample()
        }
    }
}

/** The phone-side verdict: findings with the evidence, the command and the raw data. */
@Composable
private fun LocalCard(store: AiStore) {
    val snap = store.snapshot ?: return
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("LOCAL DIAGNOSIS", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag("no AI · offline", color = Wrt.AccentBody, border = Wrt.BorderInput)
        }
        if (store.findings.isEmpty()) {
            Text(
                "Nothing obvious — the phone-side rules found no weak signal, retry storm, full " +
                    "overlay, rogue DHCP scope, bridge without STP or mesh package mismatch. A " +
                    "provider can still see what these rules cannot; the sweep is sent on Analyze.",
                style = sans(11f, 450, Wrt.TextSecondary),
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Text(
                "${store.findings.size} finding" + (if (store.findings.size == 1) "" else "s") +
                    " — computed on the phone from the sweep" +
                    (if (store.temporal != null) " and the 60-second sample" else "") + ".",
                style = sans(10.5f, 500, Wrt.TextDim),
                modifier = Modifier.padding(top = 6.dp),
            )
            store.findings.forEach { finding ->
                FindingRow(finding)
            }
        }
        if (snap.ssids.isNotEmpty()) {
            Text(
                snap.ssids.joinToString(" · "),
                style = sans(12f, 600),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun FindingRow(
    finding: Finding,
) {
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(top = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MonoTag(
                finding.severity.name,
                color = severityColor(finding.severity),
                border = Wrt.BorderInput,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                finding.title,
                style = sans(11.5f, 650, Wrt.TextSecondary),
                modifier = Modifier.weight(1f),
            )
            MonoTag(finding.domain.name, color = Wrt.TextDim, border = Wrt.BorderHair)
        }
        Text(
            "Evidence: ${finding.evidence}",
            style = mono(10f, 500, Wrt.TextTertiary),
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (expanded) "Hide the command & raw data" else "Show the command & raw data",
                style = sans(10.5f, 600, Wrt.Accent),
            )
            FlexSpacer()
            Icon(
                if (expanded) WrtIcons.ChevronUp else WrtIcons.ChevronDown,
                null,
                Modifier.size(14.dp),
                tint = Wrt.Accent,
            )
        }
        if (expanded) {
            Row(
                Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .border(1.dp, Wrt.BorderHair, RoundedCornerShape(9.dp))
                    .background(Wrt.BgCode, RoundedCornerShape(9.dp))
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    finding.command,
                    style = mono(10f, 600, Wrt.TextSecondary),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    WrtIcons.Copy,
                    "copy the command",
                    Modifier.size(15.dp).clickable { clipboard.setText(AnnotatedString(finding.command)) },
                    tint = Wrt.TextDim,
                )
            }
            if (finding.raw.isNotBlank()) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(1.dp, Wrt.BorderHair, RoundedCornerShape(9.dp))
                        .background(Wrt.BgCode, RoundedCornerShape(9.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                ) {
                    Text(finding.raw, style = mono(9f, 500, Wrt.TextSecondary))
                }
            }
        }
    }
}

private fun severityColor(severity: Severity): Color = when (severity) {
    Severity.HIGH -> Wrt.Red
    Severity.MED -> Wrt.Amber
    Severity.LOW -> Wrt.TextSecondary
}

/** The ready-made analyses: one tap writes the question, aimed at one subsystem. */
@Composable
private fun ModesCard(question: String, onPick: (String) -> Unit) {
    Card {
        SectionLabel("READY-MADE ANALYSES", size = 9.5f, tracking = 0.14)
        Text(
            "A focused question aims the provider at one subsystem without hiding the rest of " +
                "the snapshot. Pick one, or type your own below.",
            style = sans(11f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        AiPrompt.Mode.entries.chunked(2).forEach { row ->
            Row(Modifier.padding(top = 8.dp)) {
                row.forEach { mode ->
                    val active = question == mode.question
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(end = if (row.size == 2 && mode == row[0]) 4.dp else 0.dp, start = if (row.size == 2 && mode == row[1]) 4.dp else 0.dp)
                            .border(1.dp, if (active) Wrt.Accent else Wrt.BorderCard, RoundedCornerShape(11.dp))
                            .background(if (active) Wrt.BgCode else Wrt.BgCard, RoundedCornerShape(11.dp))
                            .clickable { onPick(mode.question) }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                    ) {
                        Text(
                            mode.label,
                            style = sans(11f, 600, if (active) Wrt.Accent else Wrt.TextSecondary),
                        )
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

/** A privacy-first summary of the exact redacted payload, with the full text on demand. */
@Composable
private fun DataCard(
    store: AiStore,
    settings: AiSettings,
    question: String,
    showPayload: Boolean,
    onTogglePayload: () -> Unit,
    onReadAgain: () -> Unit,
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("DATA & PRIVACY REVIEW", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            if (store.collecting) {
                Box(Modifier.size(7.dp).background(Wrt.Accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("reading…", style = mono(9.5f, 600, Wrt.Accent))
            }
        }
        val snap = store.snapshot
        if (snap == null) {
            Text(
                if (store.error != null) store.error!! else "Nothing collected yet.",
                style = sans(11.5f, 450, if (store.error != null) Wrt.Red else Wrt.TextDim),
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            // Chips scroll in one horizontal line — the payload preview below is where the
            // detail lives, and wrapped chips would push the Read button off small screens.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
            ) {
                snap.chips.forEachIndexed { i, chip ->
                    if (i > 0) Spacer(Modifier.width(6.dp))
                    MonoTag(
                        chip,
                        color = if (chip.startsWith("load") && snap.load1 > 2.0) Wrt.Amber else Wrt.TextSecondary,
                        border = Wrt.BorderInput,
                    )
                }
            }
            val includedSections = snap.sections.filterValues { it.isNotBlank() }.keys
            val redactedSecrets = snap.sections.values.sumOf {
                Regex("<redacted>").findAll(it).count()
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .border(1.dp, Wrt.BorderHair, RoundedCornerShape(10.dp))
                    .background(Wrt.BgCode, RoundedCornerShape(10.dp))
                    .padding(horizontal = 11.dp, vertical = 10.dp),
            ) {
                Text("WHAT WILL BE SENT", style = mono(9f, 650, Wrt.TextDim))
                Text(
                    "${includedSections.size} data groups: ${includedSections.joinToString(", ")}",
                    style = sans(10.5f, 500, Wrt.TextSecondary),
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    if (redactedSecrets > 0) {
                        "$redactedSecrets password/key value(s) masked on this device."
                    } else {
                        "Password/key values are checked and masked on this device."
                    },
                    style = sans(10.5f, 550, Wrt.Green),
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    "Network details such as IP addresses, device names, and Wi-Fi names may " +
                        "still appear in the preview.",
                    style = sans(10f, 450, Wrt.Amber),
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Destination: ${settings.baseUrl.trim().trimEnd('/')}  ·  Model: ${settings.model}",
                    style = mono(9f, 500, Wrt.TextTertiary),
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable(onClick = onTogglePayload),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (showPayload) "Hide exact send preview" else "Review exact data before sending",
                    style = sans(11f, 600, Wrt.Accent),
                )
                FlexSpacer()
                Icon(
                    if (showPayload) WrtIcons.ChevronUp else WrtIcons.ChevronDown,
                    null,
                    Modifier.size(14.dp),
                    tint = Wrt.Accent,
                )
            }
            if (showPayload) {
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(1.dp, Wrt.BorderHair, RoundedCornerShape(9.dp))
                        .background(Wrt.BgCode, RoundedCornerShape(9.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                ) {
                    Text(store.buildPayload(question), style = mono(9f, 500, Wrt.TextSecondary))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        GhostButton(if (store.collecting) "Reading the router…" else "Read the router again") {
            if (!store.collecting) onReadAgain()
        }
    }
}

@Composable
private fun AskCard(
    store: AiStore,
    settings: AiSettings,
    question: String,
    onQuestion: (String) -> Unit,
    onAnalyze: () -> Unit,
    onCancel: () -> Unit,
) {
    val ready = store.snapshot != null && settings.configured
    Card {
        SectionLabel("ASK", size = 9.5f, tracking = 0.14)
        Text(
            "Optional — a question focuses the analysis. Without one you get the full verdict.",
            style = sans(11f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        Box {
            FormTextField(question, onQuestion)
            if (question.isEmpty()) {
                Text(
                    "e.g. why is Wi-Fi slow in the evening?",
                    style = mono(11f, 500, Wrt.TextFaint),
                    modifier = Modifier.padding(start = 12.dp, top = 21.dp),
                )
            }
        }
        if (!settings.configured) {
            Text(
                "Set the provider above first — base URL and model, plus a key unless the " +
                    "endpoint is on your own network.",
                style = sans(10.5f, 500, Wrt.Amber),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (store.error != null) {
            Text(
                store.error!!,
                style = sans(10.5f, 500, Wrt.Red),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        PrimaryButton(
            if (store.analyzing) "Stop analysis" else "Analyze",
            color = if (ready || store.analyzing) Wrt.Accent else Wrt.TextDim,
            textColor = if (ready || store.analyzing) Wrt.OnAccent else Wrt.BgScreen,
        ) {
            if (store.analyzing) onCancel() else if (ready) onAnalyze()
        }
        if (store.analyzing) {
            Text(
                "The partial text that arrived so far is kept, and no command is ever run on the router.",
                style = sans(9.5f, 450, Wrt.TextDim),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun DiagnosisSummaryCard(store: AiStore) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("DIAGNOSIS", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag("from local evidence", color = Wrt.AccentBody, border = Wrt.BorderInput)
        }
        if (store.findings.isEmpty()) {
            Text("No obvious issue in the current reading. This does not replace re-checking after any change.", style = sans(11f, 450, Wrt.TextSecondary), modifier = Modifier.padding(top = 8.dp))
        } else {
            store.findings.forEachIndexed { index, finding ->
                Column(Modifier.padding(top = 9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MonoTag("${index + 1}", color = severityColor(finding.severity), border = Wrt.BorderHair)
                        Spacer(Modifier.width(7.dp))
                        Text(finding.title, style = sans(11.5f, 650, Wrt.TextSecondary), modifier = Modifier.weight(1f))
                        MonoTag(finding.severity.name, color = severityColor(finding.severity), border = Wrt.BorderHair)
                    }
                    Text("Evidence: ${finding.evidence}", style = mono(9.5f, 500, Wrt.TextTertiary), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun RepairPlanCard(store: AiStore, text: String) {
    val commands = remember(text) { SafeCommandsExtract(text) }
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("REPAIR PLAN", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag("manual review", color = Wrt.Amber, border = Wrt.BorderHair)
        }
        Text("Each step stands alone; the app never runs it. Review the evidence and the risk before copying.", style = sans(10.5f, 450, Wrt.TextDim), modifier = Modifier.padding(top = 7.dp))
        if (commands.isEmpty()) {
            Text("The model proposed no copyable command.", style = sans(10.5f, 500, Wrt.TextSecondary), modifier = Modifier.padding(top = 8.dp))
        } else {
            commands.forEachIndexed { index, cmd ->
                val color = when (cmd.safety) {
                    CommandSafety.SAFE -> Wrt.Green
                    CommandSafety.CHANGE -> Wrt.Amber
                    CommandSafety.BLOCKED -> Wrt.Red
                }
                Column(Modifier.padding(top = 8.dp).border(1.dp, Wrt.BorderHair, RoundedCornerShape(8.dp)).padding(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", style = mono(10f, 700, color))
                        Spacer(Modifier.width(7.dp))
                        Text(if (cmd.safety == CommandSafety.SAFE) "read-only check" else if (cmd.safety == CommandSafety.CHANGE) "changes a setting" else "blocked", style = sans(10.5f, 650, color))
                        FlexSpacer()
                        MonoTag(if (cmd.safety == CommandSafety.SAFE) "higher trust" else "needs a second tap", color = color, border = Wrt.BorderHair)
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(cmd.text, style = mono(9.5f, 600, if (cmd.safety == CommandSafety.BLOCKED) Wrt.Red else Wrt.TextSecondary).copy(textDecoration = if (cmd.safety == CommandSafety.BLOCKED) TextDecoration.LineThrough else null), modifier = Modifier.padding(top = 6.dp))
                    }
                    Text("Expected outcome: ${cmd.reason ?: "improves the metric behind the evidence."}", style = sans(9.5f, 450, Wrt.TextDim), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationCard(
    store: AiStore,
    output: String,
    onOutput: (String) -> Unit,
    onReanalyze: () -> Unit,
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("VERIFY AFTER REPAIR", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag(if (store.temporal != null) "sample ready" else "needs a sample", color = if (store.temporal != null) Wrt.Green else Wrt.Amber, border = Wrt.BorderHair)
        }
        Text("Re-read the router, then run the performance sample to compare before and after. Verify with read-only commands only; do not rely on a change command succeeding.", style = sans(10.5f, 450, Wrt.TextSecondary), modifier = Modifier.padding(top = 8.dp))
        store.findings.take(3).forEach { finding ->
            Text("• ${finding.title} — re-run: ${finding.command}", style = mono(9.5f, 500, Wrt.TextTertiary), modifier = Modifier.padding(top = 6.dp))
        }
        LocalizedFieldLabel("CHECK OUTPUT")
        CheckOutputField(output, onOutput)
        Text(
            "Paste the command's output here by hand. The command is never run, and nothing is sent until you choose to re-analyze.",
            style = sans(9.5f, 450, Wrt.TextDim),
            modifier = Modifier.padding(top = 5.dp),
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            if (store.analyzing) "Re-analyzing…" else "Re-analyze with the result",
            color = if (output.isBlank() || store.analyzing) Wrt.TextDim else Wrt.Accent,
            textColor = if (output.isBlank() || store.analyzing) Wrt.OnAccent else Wrt.BgScreen,
        ) {
            if (output.isNotBlank() && !store.analyzing) onReanalyze()
        }
    }
}

@Composable
private fun AnswerCard(store: AiStore, text: String) {
    val clipboard = LocalClipboardManager.current
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("DIAGNOSIS & REPAIR PLAN", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            if (store.analyzing) {
                Box(Modifier.size(7.dp).background(Wrt.Accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("streaming…", style = mono(9.5f, 600, Wrt.Accent))
            } else {
                Icon(
                    WrtIcons.Copy,
                    "copy the answer",
                    Modifier.size(15.dp).clickable { clipboard.setText(AnnotatedString(text)) },
                    tint = Wrt.TextDim,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        AnswerText(text)
        if (!store.analyzing) {
            CommandsSection(text)
        }
    }
}

@Composable
private fun AnalysisHistoryCard(
    store: AiStore,
    onOpen: (com.vivekkaushik.wrtpulse.db.AiAnalysisLog) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("SAVED ANALYSES", size = 9.5f, tracking = 0.14)
            FlexSpacer()
            MonoTag("LOCAL ROOM", color = Wrt.Green, border = Wrt.BorderHair)
        }
        Text(
            "Completed answers are stored in this app's Room database for this router. The raw payload and API key are not saved.",
            style = sans(10.5f, 450, Wrt.TextDim),
            modifier = Modifier.padding(top = 7.dp),
        )
        if (store.historyError != null) {
            Text(
                store.historyError!!,
                style = sans(10.5f, 500, Wrt.Red),
                modifier = Modifier.padding(top = 7.dp),
            )
        }
        if (store.history.isEmpty()) {
            Text(
                "No saved analyses yet. A completed AI response will appear here.",
                style = sans(10.5f, 450, Wrt.TextSecondary),
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            store.history.forEach { entry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, Wrt.BorderHair, RoundedCornerShape(9.dp))
                        .background(Wrt.BgCode, RoundedCornerShape(9.dp))
                        .clickable { onOpen(entry) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.question.ifBlank { "Full router analysis" },
                            style = sans(11f, 600, Wrt.TextSecondary),
                            maxLines = 2,
                        )
                        Text(
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(Date(entry.createdAtEpochMs)) +
                                " · ${entry.model} · score ${entry.healthScore ?: "—"} · " +
                                "${entry.findingCount} finding(s)",
                            style = mono(8.5f, 500, Wrt.TextTertiary),
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 2,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        WrtIcons.Trash,
                        "Delete saved analysis",
                        Modifier
                            .size(17.dp)
                            .clickable { onDelete(entry.id) },
                        tint = Wrt.TextDim,
                    )
                }
            }
        }
    }
}

/**
 * The commands the answer recommends, classified before a clipboard ever sees them: read-only
 * copies in one tap, changing ones confirm on the second tap, destructive ones are shown but
 * never offered.
 */
@Composable
private fun CommandsSection(text: String) {
    val clipboard = LocalClipboardManager.current
    val commands = remember(text) { SafeCommandsExtract(text) }
    if (commands.isEmpty()) return
    var confirmFor by remember(text) { mutableStateOf<String?>(null) }

    Column(Modifier.padding(top = 12.dp)) {
        SectionLabel("COMMANDS IN THE ANSWER", size = 9.5f, tracking = 0.14)
        Text(
            "Read-only commands copy on one tap. Changing ones need a second tap. Destructive " +
                "ones are never offered for copying.",
            style = sans(10f, 450, Wrt.TextDim),
            modifier = Modifier.padding(top = 4.dp),
        )
        commands.forEach { cmd ->
            CommandRow(cmd, confirmFor == cmd.text, { confirmFor = if (confirmFor == cmd.text) null else cmd.text }) { copy ->
                clipboard.setText(AnnotatedString(copy))
                confirmFor = null
            }
        }
    }
}

// A seam so the composable stays trivial and the extraction stays testable elsewhere.
private fun SafeCommandsExtract(text: String) =
    com.vivekkaushik.wrtpulse.ops.SafeCommands.extract(text)

@Composable
private fun CommandRow(
    cmd: com.vivekkaushik.wrtpulse.ops.AnswerCommand,
    confirm: Boolean,
    onToggleConfirm: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val color = when (cmd.safety) {
        CommandSafety.SAFE -> Wrt.Green
        CommandSafety.CHANGE -> Wrt.Amber
        CommandSafety.BLOCKED -> Wrt.Red
    }
    Column(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .border(1.dp, Wrt.BorderHair, RoundedCornerShape(9.dp))
            .background(Wrt.BgCode, RoundedCornerShape(9.dp))
            .padding(horizontal = 11.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    cmd.text,
                    style = mono(10f, 600, if (cmd.safety == CommandSafety.BLOCKED) Wrt.Red else Wrt.TextSecondary)
                        .copy(textDecoration = if (cmd.safety == CommandSafety.BLOCKED) TextDecoration.LineThrough else null),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.width(8.dp))
            MonoTag(cmd.safety.name, color = color, border = Wrt.BorderHair)
            Spacer(Modifier.width(8.dp))
            when (cmd.safety) {
                CommandSafety.SAFE -> Icon(
                    WrtIcons.Copy,
                    "copy the command",
                    Modifier.size(15.dp).clickable { onCopy(cmd.text) },
                    tint = Wrt.TextDim,
                )
                CommandSafety.CHANGE -> Box(
                    Modifier
                        .border(1.dp, if (confirm) Wrt.Amber else Wrt.BorderInput, RoundedCornerShape(7.dp))
                        .clickable {
                            if (confirm) onCopy(cmd.text) else onToggleConfirm()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (confirm) "tap to copy" else "copy",
                        style = mono(8.5f, 650, Wrt.Amber),
                    )
                }
                CommandSafety.BLOCKED -> Icon(
                    WrtIcons.Blocked,
                    "never copied",
                    Modifier.size(15.dp),
                    tint = Wrt.Red,
                )
            }
        }
        if (cmd.reason != null) {
            Text(
                cmd.reason!!,
                style = sans(9.5f, 450, if (cmd.safety == CommandSafety.BLOCKED) Wrt.Red else Wrt.AmberText),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** The export row: Markdown, plain text and PDF, each shareable with the sensitive bits hidden. */
@Composable
private fun ExportCard(
    store: AiStore,
    question: String,
    hideSensitive: Boolean,
    onHideSensitive: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val snap = store.snapshot
    Card {
        SectionLabel("REPORT", size = 9.5f, tracking = 0.14)
        Text(
            "Health, findings, the sample and the answer, in one file: Markdown for trackers, " +
                "plain text for terminals, PDF for people. Export all saved analyses for this " +
                "router as JSON below. The JSON omits the raw payload and API key. The toggle also " +
                "hides MAC and public addresses for sharing.",
            style = sans(11f, 450, Wrt.TextSecondary),
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { onHideSensitive(!hideSensitive) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(16.dp)
                    .border(1.dp, if (hideSensitive) Wrt.Accent else Wrt.BorderInput, RoundedCornerShape(4.dp))
                    .background(if (hideSensitive) Wrt.Accent else Wrt.BgCard, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (hideSensitive) {
                    Icon(WrtIcons.Check, null, Modifier.size(11.dp), tint = Wrt.OnAccent)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("Hide MAC & public addresses", style = sans(11f, 550))
        }
        if (snap != null) {
            Row(Modifier.padding(top = 10.dp)) {
                listOf(
                    "Markdown" to ReportFormat.MD,
                    "Plain text" to ReportFormat.TXT,
                    "PDF" to ReportFormat.PDF,
                ).forEach { (label, format) ->
                    Box(Modifier.weight(1f)) {
                        GhostButton(label) { exportReport(context, store, question, hideSensitive, format) }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        GhostButton(
            if (store.history.isEmpty()) "No saved analyses to export"
            else "Export ${store.history.size} saved analysis(es) as JSON",
        ) {
            if (store.history.isNotEmpty()) exportAiHistory(context, store.history, hideSensitive)
        }
    }
}

private enum class ReportFormat(val extension: String, val mime: String) {
    MD("md", "text/markdown"),
    TXT("txt", "text/plain"),
    PDF("pdf", "application/pdf"),
}

/** Builds the file and hands it to the share sheet — saving is whatever the sheet offers. */
private fun exportReport(
    context: android.content.Context,
    store: AiStore,
    question: String,
    hideSensitive: Boolean,
    format: ReportFormat,
) {
    val now = Date()
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(now)
    val readable = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(now)
    val parts = store.reportParts(question, readable)
    val dir = File(context.filesDir, "reports").apply { mkdirs() }
    val file = when (format) {
        ReportFormat.MD -> File(dir, "${AiReport.FILE_PREFIX}-$stamp.${format.extension}").apply {
            writeText(AiReport.markdown(parts, hideSensitive))
        }
        ReportFormat.TXT -> File(dir, "${AiReport.FILE_PREFIX}-$stamp.${format.extension}").apply {
            writeText(AiReport.text(parts, hideSensitive))
        }
        ReportFormat.PDF -> AiReportPdf.write(
            AiReport.text(parts, hideSensitive),
            File(dir, "${AiReport.FILE_PREFIX}-$stamp.${format.extension}"),
        )
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = format.mime
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(send, "Share the router analysis"))
}

/** Creates a JSON history file and offers it to Android's share sheet for saving or sharing. */
private fun exportAiHistory(
    context: android.content.Context,
    entries: List<com.vivekkaushik.wrtpulse.db.AiAnalysisLog>,
    hideSensitive: Boolean,
) {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val file = File(context.filesDir, "reports/WrtPulse-ai-history-$stamp.json").apply {
        parentFile?.mkdirs()
        writeText(AiHistoryJson.encode(entries, hideSensitive))
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(android.content.Intent.EXTRA_SUBJECT, "WrtPulse AI analysis history")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(send, "Share AI history JSON"))
}

/**
 * The provider's plain-text shape rendered: its own section names (VERDICT:, FINDINGS:,
 * ADVICE:, ANSWER:) become accent headings, dashes become bullets, and everything else is
 * body. No markdown engine — the system prompt asked for none.
 */
@Composable
private fun AnswerText(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        text.lineSequence().forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.isBlank() -> {}
                HEADER.matches(line) -> Text(line, style = sans(11.5f, 700, Wrt.Accent))
                line.length > 2 && (line[0] == '-' || line[0] == '•' || line[0] == '*') && line[1] == ' ' -> Row {
                    Box(
                        Modifier
                            .padding(top = 6.dp)
                            .size(4.dp)
                            .background(Wrt.TextTertiary, CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(line.drop(2), style = sans(12f, 450, Wrt.TextSecondary))
                }
                else -> Text(line, style = sans(12f, 450, Wrt.TextSecondary))
            }
        }
    }
}

private val HEADER = Regex("""^[A-Z][A-Z0-9 &/'()-]{1,40}:$""")

/** The panel every block above sits in. */
@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Wrt.BorderCard, RoundedCornerShape(13.dp))
            .background(Wrt.BgCard, RoundedCornerShape(13.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) { content() }
}
