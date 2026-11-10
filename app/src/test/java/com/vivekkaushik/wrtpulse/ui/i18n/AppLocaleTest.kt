package com.vivekkaushik.wrtpulse.ui.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The translation layer's own contracts: English is the key, Arabic the override, and a
 * missing entry can never blank a string — it shows English, the language the code was
 * written against.
 */
class AppLocaleTest {

    @Test
    fun `english passes through untouched`() {
        assertEquals("Dashboard", trFor(AppLanguage.ENGLISH, "Dashboard"))
        assertEquals("anything at all", trFor(AppLanguage.ENGLISH, "anything at all"))
    }

    @Test
    fun `a known string translates to arabic`() {
        assertEquals("الرئيسية", trFor(AppLanguage.ARABIC, "Dashboard"))
        assertEquals("النظام", trFor(AppLanguage.ARABIC, "System"))
        assertEquals("اتصل براوتر لتحليله.", trFor(AppLanguage.ARABIC, "Connect to a router to analyze it."))
    }

    @Test
    fun `an unknown string falls back to english in arabic too`() {
        // The fallback is the whole safety argument: a missing entry shows English, never a
        // blank, never a crash.
        assertEquals("not in the table", trFor(AppLanguage.ARABIC, "not in the table"))
        assertEquals("", trFor(AppLanguage.ARABIC, ""))
    }

    @Test
    fun `dynamic sentences translate by pattern`() {
        assertEquals(
            "412 نموذجًا مجانيًا متاحًا على OpenRouter — اختر واحدًا أو اكتب المعرّف يدويًا.",
            trFor(AppLanguage.ARABIC, "412 free OpenRouter models available — pick one or type any id."),
        )
        assertEquals(
            "Saved — analyses will use https://api.openai.com/v1".let {
                trFor(AppLanguage.ARABIC, it)
            },
            "تم الحفظ — ستعمل التحليلات عبر https://api.openai.com/v1",
        )
        assertEquals(
            "جارٍ قراءة العينة 2 من 3…",
            trFor(AppLanguage.ARABIC, "reading sample 2 of 3…"),
        )
        assertEquals(
            "تم أخذ العينة 1/3 — التالية بعد 27 ثانية",
            trFor(AppLanguage.ARABIC, "sample 1/3 taken — next in 27s"),
        )
    }

    @Test
    fun `patterns do not eat strings they do not match`() {
        // A pattern must match the whole sentence or nothing: partial matches would corrupt
        // more than they translate.
        assertEquals(
            "reading sample",
            trFor(AppLanguage.ARABIC, "reading sample"),
        )
    }

    @Test
    fun `the arabic table carries no empty values and no english-only leftovers`() {
        ArabicStrings.map.forEach { (english, arabic) ->
            assertTrue("blank value for: $english", arabic.isNotBlank())
            assertNotEquals("value equals key (untranslated): $english", english, arabic)
        }
    }

    @Test
    fun `the arabic table really is arabic`() {
        // Every value carries at least one Arabic letter — a value that is pure Latin is a
        // translation that forgot to happen.
        ArabicStrings.map.forEach { (english, arabic) ->
            assertTrue("no Arabic letters in: $english -> $arabic", Regex("[\\u0600-\\u06FF]").containsMatchIn(arabic))
        }
    }

    @Test
    fun `the audit pass entries translate`() {
        // The strings the coverage audit found still falling back to English: empty states,
        // warnings and hints across Backup, Firmware, Wan, Services, RouterList, Firewall.
        assertEquals("نسيان هذا الراوتر؟", trFor(AppLanguage.ARABIC, "Forget this router?"))
        assertEquals("جارٍ التحميل…", trFor(AppLanguage.ARABIC, "loading…"))
        assertEquals("اتصل براوتر لإدارة النسخ الاحتياطية.", trFor(AppLanguage.ARABIC, "Connect to a router to manage backups."))
        assertEquals("اتصل براوتر لإدارة النظام.", trFor(AppLanguage.ARABIC, "Connect to a router to manage firmware."))
        assertEquals("اتصل براوتر لإدارة وصلاته الصاعدة.", trFor(AppLanguage.ARABIC, "Connect to a router to manage its uplinks."))
        assertEquals("اتصل براوتر لإدارة الخدمات.", trFor(AppLanguage.ARABIC, "Connect to a router to manage services."))
        assertEquals("من ↓ إلى →", trFor(AppLanguage.ARABIC, "from ↓ to →"))
        assertEquals("كلمة مرور root", trFor(AppLanguage.ARABIC, "ROOT PASSWORD"))
        assertEquals(
            "مسح واحد — شغّل\nعينة الستين ثانية أدناه",
            trFor(AppLanguage.ARABIC, "single sweep — run the\n60-second sample below"),
        )
    }

    @Test
    fun `the audit pass dynamic sentences translate by pattern`() {
        // Backup sheet's archive-name line
        assertEquals(
            "تُحفظ الإعدادات الحالية على هذا الهاتف أولًا · wrtpulse-192.168.1.1-<now>.tar.gz",
            trFor(AppLanguage.ARABIC, "Current config is saved to this phone first · wrtpulse-192.168.1.1-<now>.tar.gz"),
        )
        // Factory reset, both firmware sentences
        assertEquals(
            "يمحو كل الإعدادات في /overlay ويعيد التشغيل. يبقى النظام 23.05.3؛ ويعود الراوتر كتثبيت OpenWrt جديد.",
            trFor(AppLanguage.ARABIC, "Erases every setting in /overlay and reboots. Firmware 23.05.3 stays; the router comes back as a fresh OpenWrt install."),
        )
        assertEquals(
            "يمحو كل الإعدادات في /overlay ويعيد التشغيل. يبقى النظام؛ ويعود الراوتر كتثبيت OpenWrt جديد.",
            trFor(AppLanguage.ARABIC, "Erases every setting in /overlay and reboots. The firmware stays; the router comes back as a fresh OpenWrt install."),
        )
        // Apply sheet's Wi-Fi reload warning, with and without a client count
        assertEquals(
            "تنقطع Wi-Fi لنحو 15 ثانية أثناء إعادة تحميل الراديوهات. سيعيد 12 من الأجهزة الاتصال من تلقاء نفسها. إن كان هذا الهاتف على تلك الشبكة فسيعيد التطبيق الاتصال أيضًا.",
            trFor(AppLanguage.ARABIC, "Wi-Fi drops for ~15 s while radios reload. 12 clients will reconnect on their own. If this phone is on that Wi-Fi, the app reconnects too."),
        )
        assertEquals(
            "تنقطع Wi-Fi لنحو 15 ثانية أثناء إعادة تحميل الراديوهات. ستعيد الأجهزة الاتصال من تلقاء نفسها. إن كان هذا الهاتف على تلك الشبكة فسيعيد التطبيق الاتصال أيضًا.",
            trFor(AppLanguage.ARABIC, "Wi-Fi drops for ~15 s while radios reload. Clients will reconnect on their own. If this phone is on that Wi-Fi, the app reconnects too."),
        )
        // Lan's mesh-node follow line
        assertEquals(
            "عقد mesh تتبعه: attic to 192.168.1.2, garage to 192.168.1.3. مداخلها المحفوظة في التطبيق تشير إلى العنوان الجديد أصلًا.",
            trFor(AppLanguage.ARABIC, "Its mesh nodes follow it: attic to 192.168.1.2, garage to 192.168.1.3. Their saved entries already point there."),
        )
    }

    @Test
    fun `the audit pass 2 patterns translate runtime sentences`() {
        // The AI screen's findings line, singular and plural, with and without the sample
        assertEquals(
            "3 نتائج — حُسبت على الهاتف من المسح.",
            trFor(AppLanguage.ARABIC, "3 findings — computed on the phone from the sweep."),
        )
        assertEquals(
            "نتيجة واحدة — حُسبت على الهاتف من المسح ومن عينة الستين ثانية.",
            trFor(AppLanguage.ARABIC, "1 finding — computed on the phone from the sweep and the 60-second sample."),
        )
        // The AI client's errors
        assertEquals(
            "رفض المزوّد مفتاح API (401).",
            trFor(AppLanguage.ARABIC, "The provider rejected the API key (401)."),
        )
        assertEquals(
            "أجاب المزوّد 500: upstream error.",
            trFor(AppLanguage.ARABIC, "The provider answered 500: upstream error."),
        )
        assertEquals(
            "تعذّر الوصول إلى 192.168.1.1 — تحقق من العنوان ومن أنك على شبكة الراوتر.",
            trFor(AppLanguage.ARABIC, "Can't reach 192.168.1.1 — check the address and that you're on the router's network."),
        )
        // Country screen's pending-changes line, both plural and singular
        assertEquals(
            "هناك أيضًا 3 تغييرات لاسلكية معلّقة أخرى. التطبيق هنا يرسلها كلها — راجعها تحت «الشبكة» أولًا إن لم يكن ذلك ما تريده.",
            trFor(AppLanguage.ARABIC, "There are also 3 other pending wireless changes. Applying here commits those too — review them under Network first if that is not what you want."),
        )
        assertEquals(
            "هناك أيضًا 1 تغييرات لاسلكية معلّقة أخرى. التطبيق هنا يرسلها كلها — راجعها تحت «الشبكة» أولًا إن لم يكن ذلك ما تريده.",
            trFor(AppLanguage.ARABIC, "There are also 1 other pending wireless change. Applying here commits those too — review them under Network first if that is not what you want."),
        )
        // Factory reset's two firmware shapes
        assertEquals(
            "يمحو كل الإعدادات في /overlay ويعيد التشغيل. يبقى النظام 23.05.3؛ ويعود الراوتر كتثبيت OpenWrt جديد.",
            trFor(AppLanguage.ARABIC, "Erases every setting in /overlay and reboots. Firmware 23.05.3 stays; the router comes back as a fresh OpenWrt install."),
        )
        // MainActivity's apply result, both shapes
        assertEquals(
            "لم يقبل الراوتر ذلك.",
            trFor(AppLanguage.ARABIC, "The router did not take it."),
        )
        assertEquals(
            "لم يقبل الراوتر ذلك: Image check failed",
            trFor(AppLanguage.ARABIC, "The router did not take it: Image check failed"),
        )
        // The mesh apply note (count / verb / subject all interpolated)
        assertEquals(
            "2 عقد mesh تحمل شبكة Wi-Fi لهذا الراوتر. تحصل هذه العقد على التغييرات أولًا ثم يطبقها هذا الراوتر، فتبقى وصلة الميش عند تغيير القناة.",
            trFor(AppLanguage.ARABIC, "2 mesh nodes carry this router's Wi-Fi. They get these changes first, then this router applies them, so a channel change keeps the mesh link."),
        )
        // Onboarding's same-router-different-key warning
        assertEquals(
            "«Casa» محفوظ أيضًا عند 192.168.1.5 بمفتاح مختلف. إن كان هذا ذاك الراوتر بعد إعادة ضبط أو إعادة وميض، فارجع وافتحه من قائمتك بدلًا من ذلك، لكي يُراجع المفتاح المتغيّر بدل حفظه كراوتر جديد.",
            trFor(AppLanguage.ARABIC, "“Casa” is also saved at 192.168.1.5, with a different key. If this is that router after a reset or reflash, go back and open it from your list instead so the changed key is reviewed rather than saved as a new router."),
        )
        // Factory reset's backup note and Wi-Fi-loss note, joined at runtime by StringBuilder
        assertEquals(
            "حُفظت نسخة احتياطية على هذا الهاتف أولًا · wrtpulse-192.168.1.1.tar.gz",
            trFor(AppLanguage.ARABIC, "Backup saved to this phone first · wrtpulse-192.168.1.1.tar.gz"),
        )
        assertEquals(
            "إن كنت تصل إلى هذا الراوتر عبر Wi-Fi فلن تكون هناك Wi-Fi بعد إعادة الضبط — وصّله بالكابل وأعد إضافته عند 192.168.1.1.",
            trFor(AppLanguage.ARABIC, "If you reach this router over its Wi-Fi, there is no Wi-Fi after the reset — plug in by cable and re-add it at 192.168.1.1."),
        )
        // The add-node wizard's headline shapes
        assertEquals(
            "وُجد GL.iNet GL-MT6000",
            trFor(AppLanguage.ARABIC, "Found a GL.iNet GL-MT6000"),
        )
        assertEquals(
            "صِل كابلًا بالمنفذ LAN 2 من Casa",
            trFor(AppLanguage.ARABIC, "Plug a cable into socket LAN 2 of Casa"),
        )
        assertEquals(
            "Attic صار الآن عقدة من Casa",
            trFor(AppLanguage.ARABIC, "Attic is now a node of Casa"),
        )
        // The mesh join step line with a count and host
        assertEquals(
            "12ث — انتظار إجابة 192.168.1.2",
            trFor(AppLanguage.ARABIC, "12s — waiting for 192.168.1.2 to answer"),
        )
    }

    @Test
    fun `language codes round-trip`() {
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("ar"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode(" AR "))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(null, AppLanguage.fromCode("fr"))
        assertEquals(null, AppLanguage.fromCode(null))
    }

    @Test
    fun `applocale apply switches the singleton`() {
        val before = AppLocale.current
        try {
            AppLocale.apply(AppLanguage.ARABIC)
            assertEquals(AppLanguage.ARABIC, AppLocale.current)
            assertTrue(AppLocale.isArabic())
            assertEquals("الرئيسية", tr("Dashboard"))
            AppLocale.apply(AppLanguage.ENGLISH)
            assertFalse(AppLocale.isArabic())
            assertEquals("Dashboard", tr("Dashboard"))
        } finally {
            AppLocale.apply(before)
        }
    }
}
