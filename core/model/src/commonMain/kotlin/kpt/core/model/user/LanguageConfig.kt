/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

/**
 * Every language the app can be switched to, in the user's OWN language.
 *
 * GENERATED from core/registries/LOCALE_REGISTRY.yaml by
 * `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT. The selectable set is
 * held EQUAL to the shipped locale set by LC-14 (RULE-IMPL-LOCALE-COVERAGE-001): a locale
 * that has translated strings but no picker entry is unreachable, and a picker entry with
 * no strings shows English. Add or remove languages in the registry, then re-run the sync.
 *
 * Enabled for mifos-x/kmp-project-template: tiers [1]  (20 languages + system default)
 *
 * @property localeName platform locale tag (BCP-47) passed to the platform locale switcher;
 *   `null` means "follow the system".
 * @property text the language endonym — a picker must name a language in that language.
 */
enum class LanguageConfig(
    val localeName: String?,
    val text: String,
) {
    DEFAULT(
        localeName = null,
        text = "System Default",
    ),
    ENGLISH(
        localeName = "en",
        text = "English",
    ),
    SPANISH(
        localeName = "es",
        text = "Español",
    ),
    CHINESE_SIMPLIFIED(
        localeName = "zh-CN",
        text = "简体中文",
    ),
    CHINESE_TRADITIONAL(
        localeName = "zh-TW",
        text = "繁體中文",
    ),
    HINDI(
        localeName = "hi",
        text = "हिन्दी",
    ),
    ARABIC(
        localeName = "ar",
        text = "العربية",
    ),
    PORTUGUESE_BRAZIL(
        localeName = "pt-BR",
        text = "Português (Brasil)",
    ),
    RUSSIAN(
        localeName = "ru",
        text = "Русский",
    ),
    JAPANESE(
        localeName = "ja",
        text = "日本語",
    ),
    GERMAN(
        localeName = "de",
        text = "Deutsch",
    ),
    FRENCH(
        localeName = "fr",
        text = "Français",
    ),
    KOREAN(
        localeName = "ko",
        text = "한국어",
    ),
    ITALIAN(
        localeName = "it",
        text = "Italiano",
    ),
    TURKISH(
        localeName = "tr",
        text = "Türkçe",
    ),
    INDONESIAN(
        localeName = "in",
        text = "Bahasa Indonesia",
    ),
    VIETNAMESE(
        localeName = "vi",
        text = "Tiếng Việt",
    ),
    THAI(
        localeName = "th",
        text = "ไทย",
    ),
    POLISH(
        localeName = "pl",
        text = "Polski",
    ),
    DUTCH(
        localeName = "nl",
        text = "Nederlands",
    ),
    UKRAINIAN(
        localeName = "uk",
        text = "Українська",
    ),
}
