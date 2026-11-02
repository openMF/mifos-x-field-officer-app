/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package org.convention

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

/**
 * Defines the hierarchical structure for source set organization.
 *
 * This template establishes the relationships between different platform targets,
 * creating logical groupings based on platform similarities to facilitate code sharing.
 */
private val hierarchyTemplate = KotlinHierarchyTemplate {
    withSourceSetTree(
        KotlinSourceSetTree.main,
        KotlinSourceSetTree.test,
    )

    common {
        withCompilations { true }

        groupNonAndroid()
        groupJsCommon()
        groupNonJsCommon()
        groupJvmCommon()
        groupNonJvmCommon()
        groupNative()
        groupNonNative()
        groupJvmJsCommon()
        groupMobile()
    }
}

/**
 * Creates a group of non-Android platforms (JVM, JS, and native).
 */
private fun KotlinHierarchyBuilder.groupNonAndroid() {
    group("nonAndroid") {
        withJvm()
        groupJsCommon()
        groupNative()
    }
}

/**
 * Creates a group of JavaScript-related platforms (JS and WebAssembly JS).
 */
private fun KotlinHierarchyBuilder.groupJsCommon() {
    group("jsCommon") {
        withJs()
        withWasmJs()
    }
}

/**
 * Creates a group of non-JavaScript platforms (JVM-based and native).
 */
private fun KotlinHierarchyBuilder.groupNonJsCommon() {
    group("nonJsCommon") {
        groupJvmCommon()
        groupNative()
    }
}

/**
 * Creates a group of JVM-based platforms (Android and JVM).
 */
private fun KotlinHierarchyBuilder.groupJvmCommon() {
    group("jvmCommon") {
        withAndroidTarget()
        withJvm()
    }
}

/**
 * Creates a group of non-JVM platforms (JavaScript and native).
 */
private fun KotlinHierarchyBuilder.groupNonJvmCommon() {
    group("nonJvmCommon") {
        groupJsCommon()
        groupNative()
    }
}

/**
 * Creates a group of JVM, JS platforms (JavaScript and JVM).
 */
private fun KotlinHierarchyBuilder.groupJvmJsCommon() {
    group("jvmJsCommon") {
        groupJsCommon()
        withJvm()
    }
}

/**
 * Creates a hierarchical group of native platforms with subgroups for Apple platforms.
 */
private fun KotlinHierarchyBuilder.groupNative() {
    group("native") {
        withNative()

        group("apple") {
            withApple()

            group("ios") {
                withIos()
            }

            group("macos") {
                withMacos()
            }
        }
    }
}

/**
 * Creates a group of non-native platforms (JavaScript and JVM-based).
 */
private fun KotlinHierarchyBuilder.groupNonNative() {
    group("nonNative") {
        groupJsCommon()
        groupJvmCommon()
    }
}

private fun KotlinHierarchyBuilder.groupMobile() {
    group("mobile") {
        withAndroidTarget()
        withApple()
    }
}

/**
 * Applies the predefined hierarchy template to a Kotlin Multiplatform project.
 *
 * This extension function should be called within the `kotlin` block of a Multiplatform
 * project's build script to establish the source set hierarchy defined in this file.
 *
 * Example usage:
 * ```
 * kotlin {
 *     applyProjectHierarchyTemplate()
 *     // Configure targets...
 * }
 * ```
 */
fun KotlinMultiplatformExtension.applyProjectHierarchyTemplate() {
    applyHierarchyTemplate(hierarchyTemplate)
}
