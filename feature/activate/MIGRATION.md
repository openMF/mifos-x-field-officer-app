# feature/activate — migration ledger

Ported from `6b66e8a43:feature/activate/` (5 `.kt`). One screen, three entity types.

## Why it exists as its own module

Activation is the same interaction for a client, a centre and a group — pick a date, submit — and
only the endpoint differs. The original made it one module reached from all three graphs; that is
kept.

## It fixes a defect this port had shipped

An earlier pass activated a GROUP **inline** from the group detail screen, posting
`ActivatePayload()` with all three fields null and a comment asserting that "the server defaults the
activation date to today". That was an assumption, never a measurement: Fineract's activate command
takes `activationDate` + `dateFormat` + `locale`, and the original has always collected the date
from a date picker. The inline write is gone; `GroupsDetailViewModel` now emits
`GroupsDetailEvent.Activate(groupId)` and this screen does the work.

Side effects of the move, all improvements:

- `GroupsDetailState` emptied out, so the group detail ViewModel is now
  `BaseViewModel<Unit, E, A>` — the same shape `ClientGeneralViewModel` uses.
- `GroupWriteError` / `isActivating` / the write-error dialog deleted from the group screen.
- Two group tests that exercised the inline write were replaced with ones asserting the screen
  NAVIGATES and posts nothing itself; the in-flight guard and offline reporting moved here with the
  write and are tested here.
- **Centre activation now exists.** It was logged as unsurfaced in CAPABILITY_GAPS; `canActivate`
  + an Activate button on centre detail close it, using `active != true` (not `== false`) because
  Fineract leaves `active` NULL on a never-activated centre — the exact defect that reached a device
  on the group screen.
- `GroupCommandRepository.activateGroup` changed from `MutationResult<HttpResponse>` to
  `MutationResult<Unit>` — the same Ktor-in-the-data-contract leak fixed for client's seven commands,
  and the reason the group fake could not return success before.

## Registered as a `@FeatureDestination`, deliberately

Spec §4 says only TOP-LEVEL entries are annotated and drill-downs are registered inside their parent
graph. Activate is the exception the rule implies: it has **two** parents (centre and group), so
registering it in each would declare `ActivateRoute` twice in one NavHost — a duplicate-destination
crash at graph build, the failure `ClientNavigation.clientTabGraph` already warns about. Annotating
it registers it exactly once.

Registration is the usual two halves, both required (see `feature-deps.gradle.kts`): the
`settings.local.gradle.kts` include and the `commonMainImplementation` edge. Codegen derives the
registration from source; it cannot derive the build edge.

## Not wired: CLIENT activation

`feature/activate` supports it and the command exists, but nothing live reaches it. The original
passed `activateClient` into `clientNavGraph`, which handed it to `clientDetailRoute` — the 884-line
screen nothing navigates to. Same finding as the survey screens. Wiring it needs a product decision
about where a client is activated from.

## Changed from the original

- `ActivateUiState.Loading` **replaced the form** while a request was in flight, so the date the
  officer had picked vanished and came back reset on failure. The form stays mounted; the button
  disables (`isSubmitting`).
- The route's `id` and `type` had defaults (`-1`, `""`), so a missing argument produced a screen that
  would try to activate entity `-1` of no known kind. Both are required now.
- `type` was a raw `String` compared against three `Constants` values inside the screen, where a typo
  matched no branch and the submit silently did nothing. It is a closed `ActivateType` enum.
- The date/format pairing is the original's and was already correct here — unlike `clientClosure`
  and `clientTransfer`, which declared `dd MMMM yyyy` while sending `dd-MM-yyyy`.

8 tests.
