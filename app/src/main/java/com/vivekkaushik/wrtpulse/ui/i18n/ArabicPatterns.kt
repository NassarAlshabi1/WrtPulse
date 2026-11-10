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
        // "1 mesh node carries this router's Wi-Fi. It gets these changes first…" /
        // "2 mesh nodes carry this router's Wi-Fi. They get these changes first…" —
        // MainActivity's mesh apply note, both number shapes in full.
        Pattern(
            Regex("""^(\d+) mesh node carries this router's Wi-Fi\. It gets these changes first, then this router applies them, so a channel change keeps the mesh link\.$"""),
        ) { m -> "عقدة mesh واحدة تحمل شبكة Wi-Fi لهذا الراوتر. تحصل هذه العقدة على التغييرات أولًا ثم يطبقها هذا الراوتر، فتبقى وصلة الميش عند تغيير القناة." },
        Pattern(
            Regex("""^(\d+) mesh nodes carry this router's Wi-Fi\. They get these changes first, then this router applies them, so a channel change keeps the mesh link\.$"""),
        ) { m -> "${m.groupValues[1]} عقد mesh تحمل شبكة Wi-Fi لهذا الراوتر. تحصل هذه العقد على التغييرات أولًا ثم يطبقها هذا الراوتر، فتبقى وصلة الميش عند تغيير القناة." },
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
        // ---------- audit pass 2: the AI screen, its client and its store ----------
        // "The list did not load: connection reset"
        Pattern(
            Regex("""^The list did not load: (.+)$"""),
        ) { m -> "لم تُحمّل القائمة: ${m.groupValues[1]}" },
        // "Local endpoint — no API key needed, and the analysis never leaves your network. Edit the host to wherever Ollama runs (http://…)."
        Pattern(
            Regex("""^Local endpoint — no API key needed, and the analysis never leaves your network\. Edit the host to wherever Ollama runs \((.+)\)\.$"""),
        ) { m -> "نقطة نهاية محلية — لا حاجة لمفتاح API، ولا يغادر التحليل شبكتك أبدًا. عدّل المضيف إلى حيث يعمل Ollama (${m.groupValues[1]})." },
        // Use "llama3" as the model id
        Pattern(
            Regex("""^Use "(.+)" as the model id$"""),
        ) { m -> "استخدم \"${m.groupValues[1]}\" كمعرّف النموذج" },
        // "3 findings — computed on the phone from the sweep." (+ " and the 60-second sample")
        Pattern(
            Regex("""^(\d+) finding — computed on the phone from the sweep\.$"""),
        ) { m -> "نتيجة واحدة — حُسبت على الهاتف من المسح." },
        Pattern(
            Regex("""^(\d+) findings — computed on the phone from the sweep\.$"""),
        ) { m -> "${m.groupValues[1]} نتائج — حُسبت على الهاتف من المسح." },
        Pattern(
            Regex("""^(\d+) finding — computed on the phone from the sweep and the 60-second sample\.$"""),
        ) { m -> "نتيجة واحدة — حُسبت على الهاتف من المسح ومن عينة الستين ثانية." },
        Pattern(
            Regex("""^(\d+) findings — computed on the phone from the sweep and the 60-second sample\.$"""),
        ) { m -> "${m.groupValues[1]} نتائج — حُسبت على الهاتف من المسح ومن عينة الستين ثانية." },
        // AiClient errors: "That endpoint is not a URL this app can open: …"
        Pattern(
            Regex("""^That endpoint is not a URL this app can open: (.+)$"""),
        ) { m -> "تلك النقطة ليست عنوان URL يستطيع هذا التطبيق فتحه: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^The provider streamed no content\. Usually the model name is one it does not know: (.+)$"""),
        ) { m -> "لم يبثّ المزوّد أي محتوى. عادةً يكون اسم النموذج اسمًا لا يعرفه: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^The provider listed no models at (.+)\.$"""),
        ) { m -> "لم يعرض المزوّد أي نماذج عند ${m.groupValues[1]}." },
        Pattern(
            Regex("""^The provider rejected the API key \((\d+)\)\.$"""),
        ) { m -> "رفض المزوّد مفتاح API (${m.groupValues[1]})." },
        Pattern(
            Regex("""^There is no models endpoint at (.+) — type the model id by hand\.$"""),
        ) { m -> "لا توجد نقطة نماذج عند ${m.groupValues[1]} — اكتب معرّف النموذج يدويًا." },
        Pattern(
            Regex("""^There is no chat/completions endpoint at (.+)\. Check the base URL\.$"""),
        ) { m -> "لا توجد نقطة chat/completions عند ${m.groupValues[1]}. تحقق من العنوان الأساسي." },
        Pattern(
            Regex("""^The provider answered (\d+): (.+)$"""),
        ) { m -> "أجاب المزوّد ${m.groupValues[1]}: ${m.groupValues[2]}" },
        Pattern(
            Regex("""^The provider answered (\d+)\.$"""),
        ) { m -> "أجاب المزوّد ${m.groupValues[1]}." },
        Pattern(
            Regex("""^The provider did not answer in time: (.+)$"""),
        ) { m -> "لم يجب المزوّد في الوقت المناسب: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Could not resolve (.+) — the phone is offline or the host is wrong\.$"""),
        ) { m -> "تعذّر تحليل ${m.groupValues[1]} — الهاتف غير متصل أو المضيف خاطئ." },
        // AiStore errors: "Couldn't read the router: …"
        Pattern(
            Regex("""^Couldn't read the router: (.+)$"""),
        ) { m -> "تعذّرت قراءة الراوتر: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Couldn't sample the router: (.+)$"""),
        ) { m -> "تعذّر أخذ عينة من الراوتر: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Couldn't load saved analyses: (.+)$"""),
        ) { m -> "تعذّر تحميل التحليلات المحفوظة: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Couldn't delete this analysis: (.+)$"""),
        ) { m -> "تعذّر حذف هذا التحليل: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Couldn't clear analysis history: (.+)$"""),
        ) { m -> "تعذّر مسح سجل التحليلات: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Analysis completed, but couldn't save it locally: (.+)$"""),
        ) { m -> "اكتمل التحليل، لكن تعذّر حفظه محليًا: ${m.groupValues[1]}" },
        // ---------- audit pass 2: router list, onboarding, first contact ----------
        Pattern(
            Regex("""^Nothing matches “(.+)” — try a name, address or model\.$"""),
        ) { m -> "لا شيء يطابق «${m.groupValues[1]}» — جرّب اسمًا أو عنوانًا أو طرازًا." },
        Pattern(
            Regex("""^Nothing matches “(.+)”$"""),
        ) { m -> "لا شيء يطابق «${m.groupValues[1]}»" },
        Pattern(
            Regex("""^The host key saved for this entry moves with it\. If a different router answers at (.+), you get the changed-key warning, not a first-contact prompt\.$"""),
        ) { m -> "مفتاح المضيف المحفوظ لهذا المدخل ينتقل معه. إن أجاب راوتر آخر عند ${m.groupValues[1]}، ظهر لك تحذير تغيّر المفتاح لا رسالة أول تواصل." },
        Pattern(
            Regex("""^The session open on (.+) right now stays on (.+) until you reconnect\.$"""),
        ) { m -> "الجلسة المفتوحة على ${m.groupValues[1]} الآن تبقى على ${m.groupValues[2]} حتى تعيد الاتصال." },
        Pattern(
            Regex("""^(.+) (?:is|are) also saved at (.+), with a different key\. If this is that router after a reset or reflash, go back and open it from your list instead so the changed key is reviewed rather than saved as a new router\.$"""),
        ) { m ->
            val who = m.groupValues[1].replace("“", "«").replace("”", "»")
            "$who محفوظ أيضًا عند ${m.groupValues[2]} بمفتاح مختلف. إن كان هذا ذاك الراوتر بعد إعادة ضبط أو إعادة وميض، فارجع وافتحه من قائمتك بدلًا من ذلك، لكي يُراجع المفتاح المتغيّر بدل حفظه كراوتر جديد."
        },
        Pattern(
            Regex("""^Can't reach (.+) — check the address and that you're on the router's network\.$"""),
        ) { m -> "تعذّر الوصول إلى ${m.groupValues[1]} — تحقق من العنوان ومن أنك على شبكة الراوتر." },
        Pattern(
            Regex("""^Signed in, but a setup command failed: (.+)$"""),
        ) { m -> "تم تسجيل الدخول، لكن أمر إعداد فشل: ${m.groupValues[1]}" },
        // ---------- audit pass 2: factory reset, host key, dashboard ----------
        Pattern(
            Regex("""^Reset (.+) to factory defaults\?$"""),
        ) { m -> "إعادة ضبط ${m.groupValues[1]} إلى إعدادات المصنع؟" },
        Pattern(
            Regex("""^Backup saved to this phone first · (.+)$"""),
        ) { m -> "حُفظت نسخة احتياطية على هذا الهاتف أولًا · ${m.groupValues[1]}" },
        Pattern(
            Regex("""^If you reach this router over its Wi-Fi, there is no Wi-Fi after the reset — plug in by cable and re-add it at (.+)\.$"""),
        ) { m -> "إن كنت تصل إلى هذا الراوتر عبر Wi-Fi فلن تكون هناك Wi-Fi بعد إعادة الضبط — وصّله بالكابل وأعد إضافته عند ${m.groupValues[1]}." },
        Pattern(
            Regex("""^firstboot is away and the link is gone — that is expected\. The router reboots as a fresh install at (.+), with a new host key, so it comes back as a new entry rather than this one\.$"""),
        ) { m -> "انطلق firstboot وانقطعت الوصلة — وهذا متوقع. يقلع الراوتر كتثبيت جديد عند ${m.groupValues[1]} وبمفتاح مضيف جديد، فيعود مدخلًا جديدًا بدل هذا المدخل." },
        Pattern(
            Regex("""^Your settings are on this phone as (.+) — restore them from Backup once the router is added again\.$"""),
        ) { m -> "إعداداتك على هذا الهاتف باسم ${m.groupValues[1]} — استعدها من النسخ الاحتياطي متى أُضيف الراوتر مجددًا." },
        Pattern(
            Regex("""^(.+) presented a different key than the one saved earlier\. Either the router was reset or reflashed — or something between you and it is intercepting the connection\.$"""),
        ) { m -> "قدّم ${m.groupValues[1]} مفتاحًا مختلفًا عن المحفوظ سابقًا. فإما أن الراوتر أُعيد ضبطه أو أُعيد وميضه — أو أن شيئًا بينك وبينه يعترض الاتصال." },
        Pattern(
            Regex("""^The router did not take it: (.+)$"""),
        ) { m -> "لم يقبل الراوتر ذلك: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^The router did not take it\.$"""),
        ) { m -> "لم يقبل الراوتر ذلك." },
        Pattern(
            Regex("""^The router downloads up to (\d+) MB from (.+) and uploads up to the same back, (\d+) s each at most\. This uses your internet data\.$"""),
        ) { m -> "ينزّل الراوتر حتى ${m.groupValues[1]} ميغابايت من ${m.groupValues[2]} ويرفع المثل، بحد أقصى ${m.groupValues[3]} ثوانٍ لكل اتجاه. هذا يستهلك باقة إنترنتك." },
        // ---------- audit pass 2: country, clients ----------
        Pattern(
            Regex("""^Applied · radios now report (.+)$"""),
        ) { m -> "طُبّق · الراديوهات تُبلغ الآن عن ${m.groupValues[1]}" },
        Pattern(
            Regex("""^There are also (\d+) other pending wireless changes?\. Applying here commits those too — review them under Network first if that is not what you want\.$"""),
        ) { m -> "هناك أيضًا ${m.groupValues[1]} تغييرات لاسلكية معلّقة أخرى. التطبيق هنا يرسلها كلها — راجعها تحت «الشبكة» أولًا إن لم يكن ذلك ما تريده." },
        Pattern(
            Regex("""^Sizes come from (.+); free space from df /overlay\.$"""),
        ) { m -> "الأحجام من ${m.groupValues[1]}؛ والمساحة الحرة من df /overlay." },
        Pattern(
            Regex("""^reserved (.+) · applies on next renewal$"""),
        ) { m -> "محجوز ${m.groupValues[1]} · يسري عند التجديد التالي" },
        // ---------- audit pass 2: the add-node wizard and the mesh join ----------
        Pattern(
            Regex("""^Plug a cable into socket (.+) of (.+)$"""),
        ) { m -> "صِل كابلًا بالمنفذ ${m.groupValues[1]} من ${m.groupValues[2]}" },
        Pattern(
            Regex("""^Nothing answered on (.+)\.$"""),
        ) { m -> "لم يجب شيء على ${m.groupValues[1]}." },
        Pattern(
            Regex("""^It answers on (.+)\. Leave the cable in from here on\.$"""),
        ) { m -> "يجيب على ${m.groupValues[1]}. اترك الكابل موصولًا من الآن فصاعدًا." },
        Pattern(
            Regex("""^Nothing on (.+) yet\. Check both ends are seated; the light on the socket comes on when it is\.$"""),
        ) { m -> "لا شيء على ${m.groupValues[1]} بعد. تأكد أن الطرفين مُثبّتان؛ يضيء ضوء المنفذ متى كان كذلك." },
        Pattern(
            Regex("""^(\d+) devices? answer on that socket; the first is tried\. Unplug anything else on it\.$"""),
        ) { m -> "يجيب ${m.groupValues[1]} من الأجهزة على ذلك المنفذ؛ ويُجرّب الأول. افصل أي شيء آخر عنه." },
        Pattern(
            Regex("""^Resetting wipes every setting on it\. The cable stays in and (.+) finds it again once it is back up\.$"""),
        ) { m -> "إعادة الضبط تمحو كل إعداداته. يبقى الكابل موصولًا ويعثر عليه ${m.groupValues[1]} مجددًا متى عاد للعمل." },
        Pattern(
            Regex("""^Nothing changed on (.+)\. (.+) put its own config back and is exactly as it was\.$"""),
        ) { m -> "لم يتغير شيء على ${m.groupValues[1]}. ردّ ${m.groupValues[2]} إعداداته بنفسه وهو كما كان تمامًا." },
        Pattern(
            Regex("""^Everything runs through (.+) over the cable; this phone stays where it is\.$"""),
        ) { m -> "كل شيء يجري عبر ${m.groupValues[1]} من خلال الكابل؛ ويبقى هذا الهاتف حيث هو." },
        Pattern(
            Regex("""^(.+) is now a node of (.+)$"""),
        ) { m -> "${m.groupValues[1]} صار الآن عقدة من ${m.groupValues[2]}" },
        Pattern(
            Regex("""^Unplug the cable and place it where it should live\. Its mesh point finds (.+) on its own; if the link never comes up, plug the cable back in — it works wired on any socket now\.$"""),
        ) { m -> "افصل الكابل وضع الجهاز حيث يجب أن يعيش. تجد نقطة الميش ${m.groupValues[1]} من تلقاء نفسها؛ فإن لم تنهض الوصلة أبدًا، أعِد وصله — فهو يعمل سلكيًا على أي منفذ الآن." },
        Pattern(
            Regex("""^socket (.+) held · waiting for the cable…$"""),
        ) { m -> "المنفذ ${m.groupValues[1]} محجوز · انتظار الكابل…" },
        Pattern(
            Regex("""^a router answers on (.+)$"""),
        ) { m -> "يجيب راوتر على ${m.groupValues[1]}" },
        Pattern(
            Regex("""^cable in (.+); looking for the router…$"""),
        ) { m -> "الكابل في ${m.groupValues[1]}؛ البحث عن الراوتر…" },
        Pattern(
            Regex("""^resetting through (.+)…$"""),
        ) { m -> "جارٍ إعادة الضبط عبر ${m.groupValues[1]}…" },
        Pattern(
            Regex("""^signing in through (.+)…$"""),
        ) { m -> "جارٍ تسجيل الدخول عبر ${m.groupValues[1]}…" },
        Pattern(
            Regex("""^(.+) · wired · Wi-Fi up to date$"""),
        ) { m -> "${m.groupValues[1]} · سلكي · Wi-Fi محدّثة" },
        Pattern(
            Regex("""^(.+) · wireless · checking the mesh link…$"""),
        ) { m -> "${m.groupValues[1]} · لاسلكي · فحص وصلة الميش…" },
        Pattern(
            Regex("""^(.+) · wireless · no mesh peer yet$"""),
        ) { m -> "${m.groupValues[1]} · لاسلكي · لا نظير ميش بعد" },
        Pattern(
            Regex("""^(.+) · wireless · (-?\d+) dBm, move it closer$"""),
        ) { m -> "${m.groupValues[1]} · لاسلكي · ${m.groupValues[2]} ديسيبل، قرّبه أكثر" },
        Pattern(
            Regex("""^(.+) · wireless · (-?\d+) dBm$"""),
        ) { m -> "${m.groupValues[1]} · لاسلكي · ${m.groupValues[2]} ديسيبل" },
        Pattern(
            Regex("""^A router this app can already reach joins from its own Mesh page: the app connects to it and opens the join for (.+) there\.$"""),
        ) { m -> "الراوتر الذي يصل إليه هذا التطبيق بالفعل ينضم من صفحة الميش الخاصة به: يتصل التطبيق به ويفتح الانضمام لأجل ${m.groupValues[1]} هناك." },
        Pattern(
            Regex("""^(.+) becomes a node of (.+) with the same Wi-Fi everywhere\.$"""),
        ) { m -> "يصبح ${m.groupValues[1]} عقدة من ${m.groupValues[2]} بنفس Wi-Fi في كل مكان." },
        Pattern(
            Regex("""^Joins (.+) on (.+) once the cable comes out at the end\. Wireless nodes share (.+)'s channel\.$"""),
        ) { m -> "ينضم إلى ${m.groupValues[1]} على ${m.groupValues[2]} متى خرج الكابل في النهاية. العقد اللاسلكية تتشارك قناة ${m.groupValues[3]}." },
        Pattern(
            Regex("""^(.+) has no mesh link yet\. Turn it on from its Mesh page first\.$"""),
        ) { m -> "لا وصلة ميش على ${m.groupValues[1]} بعد. شغّلها من صفحة الميش الخاصة به أولًا." },
        Pattern(
            Regex("""^DHCP, DNS and the firewall turn off on (.+); (.+) keeps doing that\. If it can't be reached within (\d+) s it restores itself\.$"""),
        ) { m -> "تُطفأ DHCP وDNS والجدار الناري على ${m.groupValues[1]}؛ ويواصل ${m.groupValues[2]} القيام بذلك. فإن تعذّر الوصول إليه خلال ${m.groupValues[3]} ثوانٍ يستعيد نفسه." },
        Pattern(
            Regex("""^(.+) · held for the new router$"""),
        ) { m -> "${m.groupValues[1]} · محجوز للراوتر الجديد" },
        Pattern(
            Regex("""^(.+) is listening on (.+)\. This takes a few seconds\.$"""),
        ) { m -> "يستمع ${m.groupValues[1]} على ${m.groupValues[2]}. يستغرق هذا بضع ثوانٍ." },
        Pattern(
            Regex("""^Found a (.+)$"""),
        ) { m -> "وُجد ${m.groupValues[1]}" },
        Pattern(
            Regex("""^(\d+) SSIDs? copied$"""),
        ) { m -> "نُسخ ${m.groupValues[1]} SSID" },
        Pattern(
            Regex("""^(wired|wireless) · (.+)$"""),
        ) { m -> "${if (m.groupValues[1] == "wired") "سلكي" else "لاسلكي"} · ${m.groupValues[2]}" },
        // NodeSetup progress and failure notes
        Pattern(
            Regex("""^The cable went into (.+), not (.+)\. Moving the hold there\.$"""),
        ) { m -> "دخل الكابل في ${m.groupValues[1]} لا ${m.groupValues[2]}. نُقل الحجز إلى هناك." },
        Pattern(
            Regex("""^Nothing answers on (.+) yet\. Give the router a minute to boot\.$"""),
        ) { m -> "لا شيء يجيب على ${m.groupValues[1]} بعد. أمهل الراوتر دقيقة ليقلع." },
        Pattern(
            Regex("""^No router answered on (.+)\. Is the cable in one of its LAN sockets, and is it powered\?$"""),
        ) { m -> "لم يجب أي راوتر على ${m.groupValues[1]}. هل الكابل في أحد منافذ LAN لديه، وهل هو موصول بالكهرباء؟" },
        Pattern(
            Regex("""^(.+) is not in the LAN, so it cannot be held apart\.$"""),
        ) { m -> "${m.groupValues[1]} ليس في شبكة LAN، فلا يمكن عزله." },
        Pattern(
            Regex("""^(.+) is in failsafe mode\.$"""),
        ) { m -> "${m.groupValues[1]} في وضع failsafe الآمن." },
        Pattern(
            Regex("""^No answer over the cable: (.+)$"""),
        ) { m -> "لا جواب عبر الكابل: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Could not reset it: (.+)$"""),
        ) { m -> "تعذّرت إعادة ضبطه: ${m.groupValues[1]}" },
        // MeshJoin step lines and errors
        Pattern(
            Regex("""^Plug a cable from this router's WAN socket into a LAN socket of (.+) first\.$"""),
        ) { m -> "صِل كابلًا من منفذ WAN في هذا الراوتر إلى منفذ LAN في ${m.groupValues[1]} أولًا." },
        Pattern(
            Regex("""^The wpad swap downloads a package, so this router needs internet: plug its WAN socket into (.+) for now\.$"""),
        ) { m -> "تبديل wpad ينزّل حزمة، فهذا الراوتر يحتاج إنترنت: صِل منفذ WAN به إلى ${m.groupValues[1]} مؤقتًا." },
        Pattern(
            Regex("""^Return the socket to (.+)'s LAN$"""),
        ) { m -> "أعد المنفذ إلى شبكة LAN في ${m.groupValues[1]}" },
        Pattern(
            Regex("""^Find the node at (.+)$"""),
        ) { m -> "اعثر على العقدة عند ${m.groupValues[1]}" },
        Pattern(
            Regex("""^No backup, no join: (.+)$"""),
        ) { m -> "لا نسخة احتياطية، لا انضمام: ${m.groupValues[1]}" },
        Pattern(
            Regex("""^the router kept (.+)$"""),
        ) { m -> "أبقى الراوتر ${m.groupValues[1]}" },
        Pattern(
            Regex("""^(.+) did not install\. Nothing else was changed\.$"""),
        ) { m -> "لم تُثبَّت ${m.groupValues[1]}. لم يتغير أي شيء آخر." },
        Pattern(
            Regex("""^committed; the node keeps it only if the app finds it within (\d+) s$"""),
        ) { m -> "أُرسلت؛ تحتفظ به العقدة فقط إن وجده التطبيق خلال ${m.groupValues[1]} ثوانٍ" },
        Pattern(
            Regex("""^over the cable, on (.+)'s LAN$"""),
        ) { m -> "عبر الكابل، على شبكة LAN في ${m.groupValues[1]}" },
        Pattern(
            Regex("""^your phone joins (.+) on its own — stay near (.+)$"""),
        ) { m -> "ينضم هاتفك إلى ${m.groupValues[1]} من تلقاء نفسه — ابقَ قرب ${m.groupValues[2]}" },
        Pattern(
            Regex("""^The node was not reachable at (.+) in time, so it put its old config back on its own\. It is as it was, on the cable; unplug it, plug it in again, and retry\.$"""),
        ) { m -> "تعذّر بلوغ العقدة عند ${m.groupValues[1]} في الوقت المحدد، فردّت إعداداتها القديمة بنفسها. هي كما كانت، على الكابل؛ افصله وأعد وصله وحاول مجددًا." },
        Pattern(
            Regex("""^The node was not reachable at (.+) in time, so it put its old config back on its own\. It is still at (.+)\.$"""),
        ) { m -> "تعذّر بلوغ العقدة عند ${m.groupValues[1]} في الوقت المحدد، فردّت إعداداتها القديمة بنفسها. ما تزال عند ${m.groupValues[2]}." },
        Pattern(
            Regex("""^downloading through (.+)$"""),
        ) { m -> "جارٍ التنزيل عبر ${m.groupValues[1]}" },
        Pattern(
            Regex("""^(.+) did not install, so the node has no mesh point yet\. It works wired; retry the swap from its Mesh page\.$"""),
        ) { m -> "لم تُثبَّت ${m.groupValues[1]}، فلا نقطة ميش على العقدة بعد. تعمل سلكيًا؛ أعد محاولة التبديل من صفحة الميش الخاصة بها." },
        Pattern(
            Regex("""^Something else answers at (.+) with a different host key\.$"""),
        ) { m -> "يجيب شيء آخر عند ${m.groupValues[1]} بمفتاح مضيف مختلف." },
        Pattern(
            Regex("""^(\d+)s — waiting for (.+) to answer$"""),
        ) { m -> "${m.groupValues[1]}ث — انتظار إجابة ${m.groupValues[2]}" },
        Pattern(
            Regex("""^(.+) signs in over the cable\. A fresh OpenWrt install has none: leave this empty\. The password never leaves your network\.$"""),
        ) { m -> "يسجّل ${m.groupValues[1]} الدخول عبر الكابل. تثبيت OpenWrt الجديد ليس فيه كلمة مرور: اترك هذا فارغًا. كلمة المرور لا تغادر شبكتك أبدًا." },
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
