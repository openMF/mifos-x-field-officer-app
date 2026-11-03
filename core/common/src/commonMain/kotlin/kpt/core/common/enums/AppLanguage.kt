/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.enums

/**
 * The languages the app can be shown in.
 *
 * @property code the language's locale code (`en`, `hi`, …), or `System_Language` to follow the device
 * @property displayName the language's own name for itself, as listed in the picker
 */
enum class AppLanguage(val code: String, val displayName: String) {

    /** Follow the device language. */
    SYSTEM_LANGUAGE("System_Language", "System Language"),

    /** English. */
    ENGLISH("en", "English"),

    /** Hindi. */
    HINDI("hi", "हिंदी"),

    /** Arabic. */
    ARABIC("ar", "عربى"),

    /** Urdu. */
    URDU("ur", "اُردُو"),

    /** Bengali. */
    BENGALI("bn", "বাঙালি"),

    /** Spanish. */
    SPANISH("es", "Español"),

    /** French. */
    FRENCH("fr", "français"),

    /** Indonesian. */
    INDONESIAN("in", "bahasa Indonesia"),

    /** Khmer. */
    KHMER("km", "ភាសាខ្មែរ"),

    /** Kannada. */
    KANNADA("kn", "ಕನ್ನಡ"),

    /** Telugu. */
    TELUGU("te", "తెలుగు"),

    /** Burmese. */
    BURMESE("my", "မြန်မာ"),

    /** Polish. */
    POLISH("pl", "Polski"),

    /** Portuguese. */
    PORTUGUESE("pt", "Português"),

    /** Russian. */
    RUSSIAN("ru", "русский"),

    /** Swahili. */
    SWAHILI("sw", "Kiswahili"),

    /** Farsi (Persian). */
    FARSI("fa", "فارسی"),
    ;

    /** Lookup by locale code. */
    companion object {
        /** The language with [code] (case-insensitive), or [ENGLISH] when no language has it. */
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }

    override fun toString(): String {
        return displayName
    }
}
