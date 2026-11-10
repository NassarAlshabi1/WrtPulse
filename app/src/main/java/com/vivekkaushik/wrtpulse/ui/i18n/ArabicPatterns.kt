package com.vivekkaushik.wrtpulse.ui.i18n

/**
 * Arabic for the sentences the app builds at runtime — counts, hosts and model ids folded
 * into English words. The static labels live in [ArabicStrings]; these are the shapes a plain
 * key can never match, translated by pattern so the code that builds them stays untouched
 * (its English wording is what the unit tests assert, and what the AI payload carries).
 *
 * Every pattern answers null when it does not match, and [translate] falls through to the
 * static map — so an unknown string is never mangled, only shown as it is.
 */
internal object ArabicPatterns {

    private data class Pattern(val regex: Regex, val arabic: (MatchResult) -> String)

    private val patterns = listOf(
        // The picker's free filter chip: "Free · 16"
        Pattern(
            Regex("""^Free · (\d+)$"""),
        ) { m -> "مجاني · ${m.groupValues[1]}" },
        // The browse row's free badge: "16 FREE"
        Pattern(
            Regex("""^(\d+) FREE$"""),
        ) { m -> "${m.groupValues[1]} مجاني" },
        // The model picker header: "412 models · 16 of them free"
        Pattern(
            Regex("""^(\d+) models · (\d+) of them free$"""),
        ) { m -> "${m.groupValues[1]} نموذجًا · ${m.groupValues[2]} منها مجاني" },
        // "412 models from this provider"
        Pattern(
            Regex("""^(\d+) models from this provider$"""),
        ) { m -> "${m.groupValues[1]} نموذجًا من هذا المزود" },
        // "Unavailable sections: board, survey" (AI data status)
        Pattern(
            Regex("""^Unavailable sections: (.+)$"""),
        ) { m -> "أقسام غير متاحة: ${m.groupValues[1]}" },
        // "• Weak signal on radio0 — re-run: iw dev wlan0 station dump"
        Pattern(
            Regex("""^• (.+) — re-run: (.+)$"""),
        ) { m -> "• ${m.groupValues[1]} — أعد تنفيذ: ${m.groupValues[2]}" },
        // "Expected outcome: improves the metric behind the evidence."
        Pattern(
            Regex("""^Expected outcome: (.+)$"""),
        ) { m -> "النتيجة المتوقعة: ${m.groupValues[1]}" },
        // HoldButton caption: "Hold for 3 s"
        Pattern(
            Regex("""^Hold for (\d+) s$"""),
        ) { m -> "اضغط باستمرار ${m.groupValues[1]} ثوانٍ" },
        // The AI model list: "412 free OpenRouter models available — pick one or type any id."
        Pattern(
            Regex("""^(\d+) free OpenRouter models available — pick one or type any id\.$"""),
        ) { m -> "${m.groupValues[1]} نموذجًا مجانيًا متاحًا على OpenRouter — اختر واحدًا أو اكتب المعرّف يدويًا." },
        // "412 models from openrouter.ai — pick one or type any id."
        Pattern(
            Regex("""^(\d+) models from (.+) — pick one or type any id\.$"""),
        ) { m -> "${m.groupValues[1]} نموذجًا من ${m.groupValues[2]} — اختر واحدًا أو اكتب المعرّف يدويًا." },
        // "loading the model list from openrouter.ai…"
        Pattern(
            Regex("""^loading the model list from (.+)…$"""),
        ) { m -> "جارٍ تحميل قائمة النماذج من ${m.groupValues[1]}…" },
        // "Saved — analyses will use https://api.openai.com/v1"
        Pattern(
            Regex("""^Saved — analyses will use (.+)$"""),
        ) { m -> "تم الحفظ — ستعمل التحليلات عبر ${m.groupValues[1]}" },
        // AiStore sample ticks: "reading sample 2 of 3…"
        Pattern(
            Regex("""^reading sample (\d+) of (\d+)…$"""),
        ) { m -> "جارٍ قراءة العينة ${m.groupValues[1]} من ${m.groupValues[2]}…" },
        // "sample 1/3 taken — next in 27s"
        Pattern(
            Regex("""^sample (\d+)/(\d+) taken — next in (\d+)s$"""),
        ) { m -> "تم أخذ العينة ${m.groupValues[1]}/${m.groupValues[2]} — التالية بعد ${m.groupValues[3]} ثانية" },
        // "sampling stopped — keeping the 2 sample(s) taken"
        Pattern(
            Regex("""^sampling stopped — keeping the (\d+) sample\(s\) taken$"""),
        ) { m -> "تم إيقاف أخذ العينات — سيتم الاحتفاظ بـ ${m.groupValues[1]} عينة مأخوذة" },
        // "Export 12 saved analysis(es) as JSON"
        Pattern(
            Regex("""^Export (\d+) saved analysis\(es\) as JSON$"""),
        ) { m -> "تصدير ${m.groupValues[1]} تحليلًا محفوظًا بصيغة JSON" },
        // "$redactedSecrets password/key value(s) masked on this device."
        Pattern(
            Regex("""^(\d+) password/key value\(s\) masked on this device\.$"""),
        ) { m -> "تم إخفاء ${m.groupValues[1]} من قيم كلمات المرور/المفاتيح على هذا الجهاز." },
        // "3 data groups: board, info, overlay"
        Pattern(
            Regex("""^(\d+) data groups: (.+)$"""),
        ) { m -> "${m.groupValues[1]} مجموعات بيانات: ${m.groupValues[2]}" },
        // "Evidence: <the finding's evidence>"
        Pattern(
            Regex("""^Evidence: (.+)$"""),
        ) { m -> "الدليل: ${m.groupValues[1]}" },
        // "Will be saved as home-gw."  (hostname dialog)
        Pattern(
            Regex("""^Will be saved as (.+)\.$"""),
        ) { m -> "سيُحفظ باسم ${m.groupValues[1]}." },
        // "Reconnecting to home.gw — attempt 2 of 5"
        Pattern(
            Regex("""^Reconnecting to (.+) — attempt (\d+) of (\d+)$"""),
        ) { m -> "إعادة الاتصال بـ ${m.groupValues[1]} — المحاولة ${m.groupValues[2]} من ${m.groupValues[3]}" },
        // "Reconnecting to home.gw…"
        Pattern(
            Regex("""^Reconnecting to (.+)…$"""),
        ) { m -> "إعادة الاتصال بـ ${m.groupValues[1]}…" },
        // "No reply from home.gw for 12 s — retrying"
        Pattern(
            Regex("""^No reply from (.+) for (\d+) s — retrying$"""),
        ) { m -> "لا استجابة من ${m.groupValues[1]} منذ ${m.groupValues[2]} ثانية — جارٍ إعادة المحاولة" },
        // "No reply from home.gw — retrying"
        Pattern(
            Regex("""^No reply from (.+) — retrying$"""),
        ) { m -> "لا استجابة من ${m.groupValues[1]} — جارٍ إعادة المحاولة" },
        // "poll 320 ms" (top bar)
        Pattern(
            Regex("""^poll (\d+) ms$"""),
        ) { m -> "استعلام ${m.groupValues[1]} مللي ثانية" },
        // "Apply 3 changes" / "Apply 1 change"
        Pattern(
            Regex("""^Apply (\d+) changes?$"""),
        ) { m -> "تطبيق ${m.groupValues[1]} تغيير${if (m.groupValues[1] == "1") "" else "ات"}" },
        // "Last backup 6 d ago" style (BackupStore.ageLabel outputs like "6 d ago", "3 h ago")
        Pattern(
            Regex("""^Last backup (.+)$"""),
        ) { m -> "آخر نسخة احتياطية ${trAge(m.groupValues[1])}" },
        // "31 running · 2 stopped" (+ optional " · 1 not up")
        Pattern(
            Regex("""^(\d+) running · (\d+) stopped( · (\d+) not up)?$"""),
        ) { m ->
            val notUp = m.groupValues[3].takeIf { it.isNotBlank() }?.let { " · ${m.groupValues[4]} متعطل" } ?: ""
            "${m.groupValues[1]} يعمل · ${m.groupValues[2]} متوقف$notUp"
        },
        // "4 LEDs · connectivity watch on" / "3 LEDs · connectivity watch stopped"
        Pattern(
            Regex("""^(\d+) LEDs? · connectivity watch (on|stopped)$"""),
        ) { m ->
            val state = if (m.groupValues[2] == "on") "مراقبة الاتصال تعمل" else "مراقبة الاتصال متوقفة"
            "${m.groupValues[1]} مؤشرات · $state"
        },
        // "2 mesh nodes carry this router's Wi-Fi..." — MainActivity's mesh note prefix
        Pattern(
            Regex("""^(\d+) mesh nodes? carr(?:y|ies) this router's Wi-Fi\. (.+)$"""),
        ) { m -> "${m.groupValues[1]} عقد mesh تحمل شبكة Wi-Fi لهذا الراوتر. ${m.groupValues[2]}" },
        // Backup sheet: "Current config is saved to this phone first · wrtpulse-host-<now>.tar.gz"
        Pattern(
            Regex("""^Current config is saved to this phone first · (.+)$"""),
        ) { m -> "تُحفظ الإعدادات الحالية على هذا الهاتف أولًا · ${m.groupValues[1]}" },
        // Factory reset: "Erases every setting in /overlay and reboots. Firmware 23.05.3 stays; ..." / "The firmware stays; ..."
        Pattern(
            Regex("""^Erases every setting in /overlay and reboots\. Firmware (.+) stays; the router comes back as a fresh OpenWrt install\.$"""),
        ) { m -> "يمحو كل الإعدادات في /overlay ويعيد التشغيل. يبقى النظام ${m.groupValues[1]}؛ ويعود الراوتر كتثبيت OpenWrt جديد." },
        Pattern(
            Regex("""^Erases every setting in /overlay and reboots\. The firmware stays; the router comes back as a fresh OpenWrt install\.$"""),
        ) { m -> "يمحو كل الإعدادات في /overlay ويعيد التشغيل. يبقى النظام؛ ويعود الراوتر كتثبيت OpenWrt جديد." },
        // Apply sheet: "Wi-Fi drops for ~15 s while radios reload. 12 clients will reconnect on their own. ..." / "Clients will reconnect..."
        Pattern(
            Regex("""^Wi-Fi drops for ~15 s while radios reload\. (\d+) clients will reconnect on their own\. If this phone is on that Wi-Fi, the app reconnects too\.$"""),
        ) { m -> "تنقطع Wi-Fi لنحو 15 ثانية أثناء إعادة تحميل الراديوهات. سيعيد ${m.groupValues[1]} من الأجهزة الاتصال من تلقاء نفسها. إن كان هذا الهاتف على تلك الشبكة فسيعيد التطبيق الاتصال أيضًا." },
        Pattern(
            Regex("""^Wi-Fi drops for ~15 s while radios reload\. Clients will reconnect on their own\. If this phone is on that Wi-Fi, the app reconnects too\.$"""),
        ) { m -> "تنقطع Wi-Fi لنحو 15 ثانية أثناء إعادة تحميل الراديوهات. ستعيد الأجهزة الاتصال من تلقاء نفسها. إن كان هذا الهاتف على تلك الشبكة فسيعيد التطبيق الاتصال أيضًا." },
        // Lan: "Its mesh nodes follow it: attic to 192.168.1.2, garage to 192.168.1.3. Their saved entries already point there."
        Pattern(
            Regex("""^Its mesh nodes follow it: (.+)\. Their saved entries already point there\.$"""),
        ) { m -> "عقد mesh تتبعه: ${m.groupValues[1]}. مداخلها المحفوظة في التطبيق تشير إلى العنوان الجديد أصلًا." },
    )

    /** The first matching pattern's Arabic, or null when none matches. */
    fun translate(english: String): String? {
        if (english.isEmpty()) return null
        for (p in patterns) {
            val m = p.regex.matchEntire(english) ?: continue
            return p.arabic(m)
        }
        return null
    }

    /** "6 d ago" → "قبل 6 أيام"-style, keeping the unit the store chose. */
    private fun trAge(age: String): String {
        val m = Regex("""^(\d+)\s*([smhd])$""").find(age) ?: return age
        val (n, unit) = m.destructured
        val count = n.toIntOrNull() ?: return age
        return when (unit) {
            "s" -> when {
                count == 1 -> "قبل ثانية واحدة"
                count == 2 -> "قبل ثانيتين"
                else -> "قبل $count ثوانٍ"
            }
            "m" -> when {
                count == 1 -> "قبل دقيقة واحدة"
                count == 2 -> "قبل دقيقتين"
                else -> "قبل $count دقائق"
            }
            "h" -> when {
                count == 1 -> "قبل ساعة واحدة"
                count == 2 -> "قبل ساعتين"
                else -> "قبل $count ساعات"
            }
            else -> when {
                count == 1 -> "قبل يوم واحد"
                count == 2 -> "قبل يومين"
                else -> "قبل $count أيام"
            }
        }
    }
}
