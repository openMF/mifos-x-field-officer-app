# `core/domain`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_DOMAIN.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 5 Kotlin files (2 test) · source sets: `androidMain`, `commonMain`, `commonTest`



## When implementing a feature

**Tier 6** of the codegen chain, written by **`kmp-domain-gen`**.

**Does your feature need this module?** Generate core/domain use-cases (pure calculators) for KMP features whose business_logic.kind ∈ {calculator, processor, transformer, scheduler, planner}. Pure Kotlin only — NO Store5, NO DI of repos, NO platform APIs.

**Where the code goes**

```
core/domain/src/commonMain/kotlin/kpt/core/domain/<your-domain>/
```

One package per domain — the template's own `CalculateEmiUseCase.kt` sits at `kpt/core/domain/emi/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** `feature/{f}` — the Screen + ViewModel.

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core/data`](../core/data.md)
- [`core/model`](../core/model.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core/common`

**Consumed by** 7 module(s): `feature/amortization`, `feature/bills`, `feature/calculators`, `feature/currency-rates`, `feature/emi-calculator`, `feature/loans`, `feature/macro`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_DOMAIN.md`.

## Principal types

- **`AffordabilityResult`** — Output of [maxAffordableLoan].
- **`AmortizationRow`** — A single line in an amortization schedule.

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

2 test file(s) under `core/domain/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Standalone EMI math used by the B2 Wizard, B3 Amortization, B5 Affordability,
 * and B6 Comparison calculators.
 *
 * Mirrors [kpt.core.domain.emi.calculateEmi] in formula but additionally
 * exposes [amortizationSchedule] for installment-by-installment breakdowns.
 * Pure functions — safe to call inline from any ViewModel.
 *
 * **Precision**: monetary values are returned as [Double]. The UI is expected to
 * present them at 2 dp. For full-`BigDecimal` precision, replace the math layer
 * with a multiplatform `BigDecimal` port — overkill for a utility template.
 */

/**
 * Compute the standard reducing-balance EMI.
 *
 * Formula: `EMI = P · r · (1+r)^n / ((1+r)^n − 1)` where `r = annualRate/12/100`.
 *
 * Edge cases:
 * - `principal <= 0` or `tenureMonths <= 0` → returns a zero [EmiResult]
 *   (caller doesn't have to guard before rendering).
 * - `annualRatePercent == 0` → falls back to `principal / tenureMonths`
 *   (no interest accrues).
 */
fun computeEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
    if (principal <= 0.0 || tenureMonths <= 0) {
        return EmiResult(emi = 0.0, totalPayment = 0.0, totalInterest = 0.0)
    }
    val monthlyRate = annualRatePercent / 12.0 / 100.0
    val n = tenureMonths.toDouble()
    val emi = if (monthlyRate == 0.0) {
        principal / n
    } else {
        val factor = (1.0 + monthlyRate).pow(n)
        principal * monthlyRate * factor / (factor - 1.0)
    }
    val totalPayment = emi * tenureMonths
    return EmiResult(
        emi = emi,
        totalPayment = totalPayment,
        totalInterest = totalPayment - principal,
    )
}
```

Source: [`src/commonMain/kotlin/kpt/core/domain/calc/EmiCalculator.kt`](../../../../core/domain/src/commonMain/kotlin/kpt/core/domain/calc/EmiCalculator.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
