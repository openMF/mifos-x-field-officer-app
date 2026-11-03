# :feature:checkerinbox

The drawer's **Checker Inbox & Tasks**: what is waiting for this user's approval.

- **Screen:** `ui/CheckerInboxScreen` — two tabs.
  - **Approvals** — the maker-checker inbox. Search by maker, filter (action · entity · resource id · date
    range), tap a task for approve / reject / delete, long-press to select several and decide them at once.
  - **Reschedules** — pending loan reschedule requests; a tap opens the loan's reschedules screen
    (`feature/loan` `LoanReschedulesRoute`), which already approves and rejects them.
- **ViewModel:** `CheckerInboxViewModel` — `CheckerInboxRepository.checkerTasksStream` /
  `rescheduleRequestsStream` / `searchTemplate`, decisions through `CheckerInboxCommandRepository`. The
  list shown is derived from the stream + search + filter; a decided task leaves it because the command
  clears it through the store.
- **Route:** `CheckerInboxRoute`. `@FeatureDestination checkerInboxDestinations`; the drawer reaches it
  through `ForkBackboneRegistry.navigateToCheckerInbox`.
- **DI:** `CheckerInboxModule`.

Both lists are cached for offline reading; deciding needs a connection (`MutationPolicy.OnlineRequired`)
and says so. Previews render in `desktopTest` (`CheckerInboxPreviewScreenshotTest`).
