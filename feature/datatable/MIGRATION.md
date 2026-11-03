# feature/datatable — migration ledger

Ported from `6b66e8a43:feature/data-table/` — the **live half**: the datatable list and one table's
rows. 20 `.kt` upstream, 7 of them ported.

## Why only half

Per §6a's caller rule, the 20 files split cleanly by who can reach them:

| screens | reachable from | ported |
|---|---|---|
| `DataTableScreen` + `DataTableDataScreen` | group detail (`groupNavGraph(loadGroupDataTables = …)`) | **yes** |
| `DataTableListScreen` + `FormWidget` + `FormSpinnerCompose` + `DataTableRowDialog*` + `SpinnerModel` + `FormWidgetDto` + `DataTableListNavArgs` | `createClientRoute(hasDatatables = …)` only | no |

The second group is the **create-client form flow** — a dynamic form builder that renders a
tenant's datatable columns as input widgets. `createClient` is not ported, so porting its form
would add ~900 lines of screens nothing can open, the same trap as the survey screens. Tracked as
`datatable-form-flow-unported`.

Registered as a `@FeatureDestination` for the same reason as `activate` and `document`: group
detail opens it today, client will when create-client lands, so there is no single parent graph to
nest it under. Registration is the usual two halves (`settings.local.gradle.kts` include +
`feature-deps.gradle.kts` edge).

## New substrate

`dataTableDataStream(tableName, entityId)` on `DataTableRepository`, backed by a new
`datatableGetDataOfDataTable` store keyed on `(tableName, entityId)`. Its output is a raw
`JsonArray` because a datatable's COLUMNS are configured per deployment — there is no DTO to
deserialize into, which is also why the original's screen walked the JSON by hand. It still caches;
the array is `@Serializable`.

## The one place rendered CONTENT changed

Everywhere else in these slices the port keeps the UI and changes only its state plumbing. Here the
original's card showed exactly two labelled values — "Client Id" and "Data Id" — because its
mapping kept `client_id` and `id` and **discarded every other column**. For a datatable, whose
entire purpose is the tenant's own columns, the screen never showed the data. Each card now lists
every configured column, using the original's card/row/spacing treatment.

Recorded as `datatable-rows-now-show-all-columns` rather than slipped in silently.

## Defects fixed

- **Rows accumulated during composition.** The original iterated the `JsonArray` in the composition
  body and appended to a `rememberSaveable` list (`dataList = dataList + item` inside a `while`), so
  every recomposition re-appended the whole table — duplicating rows and re-triggering composition.
  The rows are derived from the array now.
- **`toString()` on a `JsonElement`** rendered text columns with their quotes (`"O+"`). `.content`
  on a primitive.
- **The delete could target the wrong row.** `dataItem.clientId?.toInt() ?: dataItem.id?.toInt() ?: 0`
  fell back to the ENTITY's id, then to `0`. The delete target is the row's own `id`, and a row
  without one offers no delete.
- **`jsonElement.jsonObject` was unguarded** on a tenant-defined shape — anything that is not an
  object threw. Non-object entries are skipped.
- **The nav argument was a JSON blob** decoded with `Json.decodeFromString` in the ViewModel's
  constructor, so a malformed or absent argument threw before the screen could render. Two
  type-safe route fields.
- `ShowEmptyDataTables` / `ShowError` / `ShowProgressbar` were a hand-rolled four-case state machine
  approximating `ScreenState`; `isEmpty` on the stream supplies Empty for free.

11 tests, mostly over the JSON→rows mapping since that is where the data was being lost.
