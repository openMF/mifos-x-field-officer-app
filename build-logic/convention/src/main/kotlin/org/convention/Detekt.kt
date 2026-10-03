package org.convention

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named

/**
 * Configures the Detekt plugin with the [extension] configuration.
 * This includes setting the JVM target to 17 and enabling all reports.
 * Additionally, it adds the `detekt-formatting` and `twitter-detekt-compose` plugins.
 * @see DetektExtension
 * @see Detekt
 */
internal fun Project.configureDetekt(extension: DetektExtension) = extension.apply {
    tasks.named<Detekt>("detekt") {
        mustRunAfter(":cmp-android:dependencyGuard")
        jvmTarget = "17"
        source(files(rootDir))
        include("**/*.kt")
        exclude("**/*.kts")
        exclude("**/resources/**")
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude("**/build-logic/**")
        exclude("**/spotless/**")
        // product-health canary fixtures. `source(files(rootDir))` means EVERY module's detekt task
        // scans the whole repo, so these get linted by all of them. They are deliberately-malformed
        // sample code — a RED fixture exists precisely to be wrong, and a GREEN one is a fragment with
        // no package/consumer — so linting them reports defects that are the fixture's whole purpose.
        exclude("scripts/product-health/tests/**")
        // TODO:: Remove this exclusion
        exclude("core-base/designsystem/**")
        exclude("feature/home/**")
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }
    dependencies {
        "detektPlugins"(libs.findLibrary("detekt-formatting").get())
        "detektPlugins"(libs.findLibrary("twitter-detekt-compose").get())
    }
}