/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects

/** A language the app can be shown in. */
enum class LanguageConfig(
    /** The language's own name for itself. */
    val localName: String?,
    /** The language's code, as Fineract expects it. */
    val languageName: String,
) {
    /** Follow the device language. */
    DEFAULT(
        localName = null,
        languageName = "System (Default)",
    ),

    /** English. */
    ENGLISH(
        localName = "en",
        languageName = "English (English)",
    ),

    /** Telugu. */
    TELUGU(
        localName = "te",
        languageName = "Telugu (తెలుగు)",
    ),

    /** Hindi. */
    HINDI(
        localName = "hi",
        languageName = "Hindi (हिंदी)",
    ),

    /** Arabic. */
    ARABIC(
        localName = "ar",
        languageName = "Arabic (عربى)",
    ),

    /** Urdu. */
    URDU(
        localName = "ur",
        languageName = "Urdu (اُردُو)",
    ),

    /** Bengali. */
    BENGALI(
        localName = "bn",
        languageName = "Bengali (বাঙালি)",
    ),

    /** Spanish. */
    SPANISH(
        localName = "es",
        languageName = "Spanish (Español)",
    ),

    /** French. */
    FRENCH(
        localName = "fr",
        languageName = "French (français)",
    ),

    /** Indonesian. */
    INDONESIAN(
        localName = "id",
        languageName = "Indonesian (bahasa Indonesia)",
    ),

    /** Khmer. */
    KHMER(
        localName = "km",
        languageName = "Khmer (ភាសាខ្មែរ)",
    ),

    /** Kannada. */
    KANNADA(
        localName = "kn",
        languageName = "Kannada (ಕನ್ನಡ)",
    ),

    /** Burmese. */
    BURMESE(
        localName = "my",
        languageName = "Burmese (မြန်မာ)",
    ),

    /** Polish. */
    POLISH(
        localName = "pl",
        languageName = "Polish (Polski)",
    ),

    /** Portuguese. */
    PORTUGUESE(
        localName = "pt",
        languageName = "Portuguese (Português)",
    ),

    /** Russian. */
    RUSSIAN(
        localName = "ru",
        languageName = "Russian (русский)",
    ),

    /** Swahili. */
    SWAHILI(
        localName = "sw",
        languageName = "Swahili (Kiswahili)",
    ),

    /** Farsi (Persian). */
    FARSI(
        localName = "fa",
        languageName = "Farsi (فارسی)",
    ),
    ;

    /** Constants and factories of `LanguageConfig`. */
    companion object {
        /** The language with this name, ignoring case, or the default. */
        fun fromString(value: String): LanguageConfig {
            return entries.find { it.languageName.equals(value, ignoreCase = true) } ?: DEFAULT
        }
    }
}
