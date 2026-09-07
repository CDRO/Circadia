# Milestone M3 — Widget: Basis

## Goal
Finish the basic Glance widget with live time display and event recording capabilities. This includes the zero-state onboarding and correct button logic based on the person's last state.

## User Review Required
> [!NOTE]
> **Live Time:** I will implement `TextClock` inside `AndroidRemoteViews` as per ADR-003. This is the only way to get minute-accurate updates on a widget without hitting battery limits or using exact alarms.

## Proposed Changes

### Widget Component (`:widget`)

#### [MODIFY] [CircadiaWidget.kt](file:///C:/Users/tizia/Projekte/Circadia/widget/src/main/kotlin/ch/circadia/tracker/widget/CircadiaWidget.kt)
- Implement the actual layout: Person name (top-left), decorative wheels, and functional Sun/Moon buttons.
- Integrate `AndroidRemoteViews` to host a native `TextClock`.
- Wire Sun/Moon buttons to `RecordStateEventUseCase` via a Glance `ActionCallback`.
- Implement `lastState` derivation from the database to dim the appropriate button.

#### [MODIFY] [WidgetConfigActivity.kt](file:///C:/Users/tizia/Projekte/Circadia/widget/src/main/kotlin/ch/circadia/tracker/widget/WidgetConfigActivity.kt)
- Implement the person selection list.
- Implement the logic to redirect to `PersonSetupActivity` if no persons exist (Zero-State).
- Correctly handle `RESULT_OK` with `EXTRA_APPWIDGET_ID`.

#### [NEW] `UpdateWidgetReceiver.kt`
- Receiver to handle widget updates and state refreshes when data changes in the database.

---

### Domain Layer (`:core:domain`)

#### [MODIFY] [RecordStateEventUseCase.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/main/kotlin/ch/circadia/tracker/core/domain/RecordStateEventUseCase.kt)
- Ensure the use case is fully robust for widget calls (rounding, idempotency).

---

## Verification Plan

### Automated Tests
- **Widget State:** `WidgetStateTest` for button logic and hold expiration (Tests 12, 13, 16, 17).
- **Configuration:** `WidgetConfigActivityTest` for the onboarding detour (Tests 18, 19, 20).
- **Screenshots:** `verifyRoborazziDebug` to ensure layout consistency across sizes and themes.
- **Verification Gate:** `./gradlew ktlintCheck detekt lint testDebugUnitTest`.

### Manual Verification
- Deploy to a device/emulator.
- Place a widget: verify it forces onboarding if empty.
- Verify the `TextClock` updates every minute.
- Click Sun/Moon: verify the toast and the database entry via the app's person list.

---

## Review & Repair Loop Plan
I will execute the mandatory Review Loop (Iterations 1, 2, and 9) before merging:
1. **Iteration 1 (Correctness):** Verify against `MASTER_PLAN.md`.
2. **Iteration 2 (Mandatory Tests):** Verify all cases `{#a-11-2}` 12-20.
3. **Iteration 9 (Privacy):** Ensure no personal names or timestamps are logged.
