/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// feature-deps.gradle.kts â the FORK-OWNED feature-dependency seam (white-label, S7/F4).
//
// `cmp-navigation/build.gradle.kts` (template-owned) applies this script via `apply(from = ...)`. A fork
// adds/removes a feature module dependency by editing ONLY this file â it never touches the template build
// file, so a template sync full-copies `cmp-navigation/build.gradle.kts` while these deps survive. This is
// the build-graph twin of the `cmp-navigation/registry` runtime seams (ForkFeatureRegistry etc.); adding a
// feature is: include it in `settings.gradle.kts`, add its dep here, and register its module + routes in
// `ForkFeatureRegistry`.
//
// String `project(":feature:x")` notation (not the type-safe `projects.feature.x` accessors) is used
// deliberately: type-safe project accessors are NOT generated for `apply(from = ...)` script plugins. The
// `commonMainImplementation` configuration is the KMP-created implementation configuration for the
// commonMain source set; it exists by the time this script is applied (after the `kotlin { }` block).

dependencies {
    // feature/signin â login. Included in settings.local.gradle.kts and picked up by
    // :cmp-navigation:generateFeatureKoinBindings (which emits `includes(SignInModule)`), but without
    // this dependency cmp-navigation cannot resolve the symbol the generated aggregate references:
    //   GeneratedFeatureKoinBindings.kt: Unresolved reference 'signin' / 'SignInModule'
    //
    // The codegen derives the REGISTRATION from source; it cannot derive the BUILD EDGE. Both halves
    // are required for every fork feature.
    "commonMainImplementation"(project(":feature:signin"))

    // feature/client â the client vertical (S3). Registration is three steps: this build
    // edge, the settings.local include, and @FeatureDestination on the graph builder.
    "commonMainImplementation"(project(":feature:client"))

    // feature/center â the centers vertical. A TAB feature like client: its @FeatureTab and
    // @FeatureDestination are collected by codegen, but codegen derives the REGISTRATION, not the
    // build edge â without this line GeneratedFeatureKoinBindings cannot resolve CenterModule.
    "commonMainImplementation"(project(":feature:center"))

    // feature/groups â the groups vertical. Same three-step registration as center.
    "commonMainImplementation"(project(":feature:groups"))

    // feature/note â client notes (S5). A DRILL-DOWN feature: its routes are registered by
    // `ClientNavigation` (which depends on it directly), not by @FeatureDestination. The build edge
    // is still required HERE, because the Koin codegen scans every feature module's `di` package
    // and emitted `includes(NoteModule)` into the aggregate â and cmp-navigation cannot resolve
    // that symbol without the dependency, even though it never navigates to note itself.
    //
    // The codegen derives the REGISTRATION from source; it cannot derive the BUILD EDGE.
    "commonMainImplementation"(project(":feature:note"))

    // feature/activate â the shared activate screen (centre + group; client's only caller is the
    // original's dead `clientDetailRoute`). Unlike note it IS `@FeatureDestination`-annotated, and
    // deliberately so: it is reached from TWO parent graphs, so registering it inside each the way
    // a single-parent drill-down is registered would declare `ActivateRoute` twice in one NavHost.
    // Same two halves as every other fork feature â this edge plus the settings.local include.
    "commonMainImplementation"(project(":feature:activate"))

    // feature/document â the GENERIC document list + upload form, for any Fineract entity type.
    // `@FeatureDestination`-annotated for the same reason activate is: group detail opens it today
    // and loan / savings will, so it has no single parent graph to nest under.
    //
    // Distinct from `feature/client/documents`, which is the CLIENT-specific list the client
    // profile opens â the original ships both and the two have different callers.
    "commonMainImplementation"(project(":feature:document"))

    // feature/charge â the ONE charge module (list / detail / add / edit / delete) for clients,
    // loans and savings, keyed by `resourceType` + `resourceId`. `@FeatureDestination` for the same
    // reason as document: three parent graphs open it, so it has no single parent to nest under.
    "commonMainImplementation"(project(":feature:charge"))

    // feature/datatable â the datatable list + a table's rows, for any entity. `@FeatureDestination`
    // for the same reason as document and activate: group detail opens it today and client will
    // once create-client lands, so it has no single parent graph to nest under.
    "commonMainImplementation"(project(":feature:datatable"))

    // feature/loan â S4. TWO loan detail screens, because the original has two and they have
    // different callers: the loan ACCOUNT SUMMARY (opened by a search hit and by group detail) and
    // the loan PROFILE (opened by the client's loan list). Plus the repayment schedule, the
    // transaction list, the approval form and the repayment form.
    //
    // `@FeatureDestination` for the same reason as the other shared drill-downs: more than one
    // parent graph opens it. The shell ALSO navigates into it directly â `navigateToSearchResult`
    // routes a LOAN search hit to the summary â which is what makes this vertical reachable at
    // all; it was registered but caller-less until S4d.
    "commonMainImplementation"(project(":feature:loan"))

    // feature/savings â S5. The savings vertical: the account SUMMARY (opened by a search hit),
    // then approval / activation / deposit-withdraw and the create wizard as the slice proceeds.
    //
    // `@FeatureDestination` for the same reason as the other shared drill-downs: the client's
    // savings list, group detail and the shell's search all open into it.
    "commonMainImplementation"(project(":feature:savings"))
    "commonMainImplementation"(project(":feature:searchrecord"))
    "commonMainImplementation"(project(":feature:about"))
    "commonMainImplementation"(project(":feature:collectionsheet"))
    "commonMainImplementation"(project(":feature:report"))
    "commonMainImplementation"(project(":feature:checkerinbox"))
    // The top bar's bell → Fineract notifications (2026-10-27).
    "commonMainImplementation"(project(":feature:notification"))
    "commonMainImplementation"(project(":feature:pathtracking"))
    // Settings → Offline sync (2026-10-27).
    "commonMainImplementation"(project(":feature:offlinesync"))
}
