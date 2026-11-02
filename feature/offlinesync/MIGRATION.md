# feature/offlinesync — migration notes

Settings → Offline sync. A **redesign** of `6b66e8a43` `feature/offline` (OfflineDashboard + the
Sync{Client,Group,Center}Payloads screens + SyncLoanRepaymentTransaction / SyncSavingsAccountTransaction),
not a 1:1 port. Design SoT: `idea-layer/screens/offlineSync/` (approved 2026-10-27, `APPROVE_LOG.yaml`).

## What it is now

- **Download-only.** The officer chooses centres / groups / clients and a durable background job downloads
  each one's whole tree, so every screen of those records opens with no network. Work done offline is a
  draft, uploaded by Settings → Sync & Drafts — this screen has no upload section.
- **Three-level dashboard** of everything stored locally (Centres / Groups / Clients), chosen or "via" a
  parent, each with its sync status, plus the running job's live progress.
- **Manual or Automatic** sync of the chosen records (1 / 6 / 12 / 24 h, Wi-Fi only, only while charging).

## Where the pieces live

| Layer | Code |
|---|---|
| model | `core/model/.../offline/OfflineSync.kt` |
| database | `core/database/.../offline/` — `offline_records` + `sync_jobs` (DB v20, ledger `fork-offline-sync`) |
| preferences | `core/datastore/.../offline/OfflineSyncSettingsStore.kt` |
| data | `core/data/.../offline/` — `OfflineSyncRepository`, `OfflineSyncEngine` (the walk), `work/OfflineSyncWorker` |
| worker registration | `cmp-shared/.../ForkWorkerDeclarations.kt` |
| screen | this module; the Settings row lives in `feature/settings` (wired in `ForkBackboneRegistry.settingsBody`) |

The centre / group lists' long-press **Download** now hands the selection to the same background job
(`OfflineDownloadRepository`, which downloaded inside the list's ViewModel and died with it, is removed).

## Not carried over

- The old Sync{Client,Group,Center}Payloads screens: the queues they drained had no writers.
- The repayment / savings upload screens: uploads belong to Sync & Drafts (see
  `idea-layer/screens/offlineSync/docs.yaml#handoff_to_drafts` — offline repayments still need that move).

## Known limits

- Most downstream reads cannot use `POST /batches` on this gateway (it routes only `loans/*` and
  `datatables/*`), so reads go single, 5 in flight.
- Removing a record removes it from the inventory; its cached store entries age out on their TTL or the
  sign-out purge rather than being deleted read by read.
- Retry failed re-runs the records that had failures, not only the individual reads.
- iOS decides when periodic background work runs.
