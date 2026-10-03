# feature/profile — migration ledger

> Written at Phase 9 T3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Consumed by Phase 12 (S2).
> Original reference: `6b66e8a43`.

**Status:** audit complete. **There is no original to port.** This is a deliberate new product surface
required by the template backbone, and it is recorded as such rather than left as an unexplained gap.

Module resolution: `fs_path=feature/profile  shape=flat  via=disk-unique`, exit 0.

---

## 1. Original inventory

**None.** `6b66e8a43:feature/` contains no `profile` module, and the original bottom bar had no Profile
tab. The template's `AuthenticatedNavBarTabItem` declares `ProfileTab` as a backbone tab that is always
rendered, so adopting the template backbone (spec §6) means this surface must exist.

This is the honest framing the §1 audit mandate requires: not "migrated", not "missing" — **new**, with
a named reason. If a Profile tab is unwanted, the alternative is the shell decision in spec §6, not a
change here.

The nearest original analogue is the drawer header (`6b66e8a43:cmp-navigation/.../drawer_profile_header.jpg`
+ `ic_dp_placeholder.png`), which displayed the signed-in officer. Those two drawables are the only
original assets with a claim on this screen.

## 2. Current shell inventory — 6 files, `owner: template`

`ProfileScreen.kt` · `ProfileScreenPreview.kt` · `ProfileRoute.kt` · `TestTags.kt` ·
`di/ProfileModule.kt` · `commonTest/ProfileScreenUiTest.kt`.

Not annotated `@FeatureDestination` or `@FeatureTab` — `ProfileTab` is a backbone tab hand-listed in
`TabRegistry.tabs`. S2 fills the content; registration already exists.

## 3. Substrate mapping

| concern | new |
|---|---|
| signed-in user identity | `kpt.core.datastore.prefs.UserPreferencesRepository` → `kpt.core.model.user.UserData` |
| officer/staff detail | `kpt.core.data.staff.StaffRepository` |
| office name | already on `User` (`officeId`, `officeName`) — no extra read needed |
| logout | `kpt.core.datastore` clear-user path; `core/datastore` ships `UserPreferencesClearUserDataTest`, so the behaviour is already covered by a test |

No new API surface. Everything this screen shows is already persisted at login, which is why it needs
no `core/data` addition — unlike `feature/home`.

## 4. Write policy

| mutation | policy | why |
|---|---|---|
| logout | **local write**, not a `MutationGateway` mutation | clears device session state. Must succeed offline — a user who cannot log out while offline is stranded. |

No server-side profile editing. Fineract user mutation is an admin concern and is **out of scope**;
adding it would be new product, not migration.

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | n/a — a signed-in user always exists on this screen |
| `NoNetwork` | the screen renders fully offline from persisted `UserData`; staff detail, if absent from cache, shows a quiet placeholder rather than an error — the identity is known even when the server is not reachable |
| `Conflicted` | n/a |
| `Failed(rolledBack)` | n/a — logout is local and cannot partially fail server-side |

## 6. Nav registration

- Backbone tab, position 2. No `@FeatureTab`, no `@FeatureDestination`.
- Build edge: `cmp-navigation/build.gradle.kts:44` already declares
  `implementation(projects.feature.profile)` — template-owned.
- Logout must land the user back at `RootNavState.Auth` → `LoginRoute`, which only works once Phase 10
  T1 wires `authNavGraph`. **Until S1 lands, logout has nowhere to go.** Ordering already satisfies
  this (S1 precedes S2).

## 7. Deviations from the original

| deviation | reason |
|---|---|
| the whole surface is new | template backbone always renders `ProfileTab` (spec §6) |
| scope limited to identity + logout | anything more is new product. Keeping it minimal is what makes "new surface" defensible inside a migration. |
| drawer header assets not reused by default | the original showed them in a drawer the template does not have. Reuse is a design choice for S2, not a migration requirement — record the decision either way. |

---

## Audit verdict

**New surface, deliberately minimal.** No original exists, so there is nothing to lose; the risk is the
opposite one — scope creep into new product under cover of a migration. Bounded to identity display +
logout, both served by data login already persists.
