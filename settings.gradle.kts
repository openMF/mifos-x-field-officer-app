pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
    id("org.ajoberstar.reckon.settings") version("0.19.2")
}

buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
    }
}

extensions.configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setDefaultInferredScope("patch")
    stages("beta", "rc", "final")
    setScopeCalc { java.util.Optional.of(org.ajoberstar.reckon.core.Scope.PATCH) }
    setScopeCalc(calcScopeFromProp().or(calcScopeFromCommitMessages()))
    setStageCalc(calcStageFromProp())
    setTagWriter { it.toString() }
}

rootProject.name = "AndroidClient"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":cmp-android")
include(":cmp-desktop")
include(":cmp-ios")
include(":cmp-navigation")
include(":cmp-shared")
include(":cmp-web")

include(":core:common")
include(":core:data")
include(":core:designsystem")
include(":core:database")
include(":core:domain")
include(":core:datastore")
include(":core:model")
include(":core:network")
include(":core:ui")

include(":core-base:analytics")
include(":core-base:common")
include(":core-base:database")
include(":core-base:designsystem")
include(":core-base:network")
include(":core-base:platform")
include(":core-base:ui")

// Lint Modules
//include(":lint")

// Library Modules
//include(":libs:country-code-picker")
//include(":libs:pullrefresh")
//include(":libs:mifos-passcode")

include(":feature:about")
include(":feature:activate")
include(":feature:auth")
include(":feature:center")
include(":feature:checker-inbox-task")
include(":feature:client")
include(":feature:collectionSheet")
include(":feature:data-table")
include(":feature:groups")
include(":feature:document")
include(":feature:loan")
include(":feature:note")
include(":feature:offline")
include(":feature:path-tracking")
include(":feature:report")
include(":feature:savings")
include(":feature:recurringDeposit")
include(":feature:search")
include(":feature:settings")
include(":feature:passcode")
include(":feature:search-record")
include(":feature:standing-instruction")
