# feature/notification — ledger

**No original.** The app being ported (`6b66e8a43`) had no notification screen; the template's
`feature/settings` `NotificationScreen` is a static "no notifications" placeholder with a hardcoded
title. Built 2026-10-27 on Fineract's own notifications so the top bar's bell shows real activity.

- `GET notifications?isRead=false` (all unread) + `?isRead=true&limit=50` (recent history) — two
  reads, because Fineract returns only one side of `isRead` per call. Offline-first store, 5 min TTL.
- `PUT notifications` marks every notification read (Fineract has no per-row read). Online only,
  through `MutationGateway`; the list is re-read after it lands; a refusal shows a snackbar.
- A row opens the record it names: `loan` → loan profile, `client`, `group`, `center`/`centre`,
  `savingsAccount` → their screens. Other kinds render but are not links.
- `ForkBackboneRegistry.navigateToNotification` routes the bell here. The template placeholder stays
  registered (template-owned) but nothing routes to it.

Gateway (2026-10-27, `fieldofficer`): 136 read notifications ("Repayment made" on loans), 0 unread.
