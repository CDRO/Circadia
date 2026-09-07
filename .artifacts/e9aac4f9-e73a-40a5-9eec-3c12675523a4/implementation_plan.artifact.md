# Implementation Plan: T-109 - RecordStateEventUseCase

Ich werde den `RecordStateEventUseCase` implementieren, der für das Erfassen von Schlaf-Wach-Ereignissen zuständig ist. Da die Repository-Interfaces (`T-104`) im aktuellen Stand der `core:domain` noch fehlen, werde ich diese im ersten Schritt (wieder-)herstellen.

## User Review Required

> [!NOTE]
> **Repository-Interfaces:** Ich werde die Interfaces `PersonRepository`, `StateEventRepository`, `WidgetBindingRepository` und `EntitlementRepository` definieren, da diese für die Use-Cases in der Domain-Schicht benötigt werden. Die tatsächliche Implementierung (Room/DataStore) folgt in späteren Tasks.

## Proposed Changes

### Domain-Schicht (`:core:domain`)

#### [NEW] Repository-Interfaces
Definition der Kern-Schnittstellen für den Datenzugriff.

#### [NEW] [RecordStateEventUseCase.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/main/kotlin/ch/circadia/tracker/core/domain/RecordStateEventUseCase.kt)
Implementierung der Logik gemäß `{#a-6-3}`:
- Abrunden des Zeitstempels auf die volle Minute.
- Idempotenz-Prüfung: Ignorieren von Doppeltipps (gleicher Status, gleiche Person) innerhalb eines Fensters von 2 Sekunden.
- Speichern des Ereignisses über das `StateEventRepository`.

#### [NEW] [RecordStateEventUseCaseTest.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/test/kotlin/ch/circadia/tracker/core/domain/RecordStateEventUseCaseTest.kt)
Verifikation der Anforderungen:
- Korrektes Runden der Zeit.
- Funktion der Idempotenz-Sperre.
- Korrekte Parameterweitergabe an das Repository.

## Verification Plan

### Automated Tests
- `./gradlew.bat :core:domain:testDebugUnitTest --tests RecordStateEventUseCaseTest`
- `./gradlew.bat ktlintCheck detekt lint` (Verification Gate).

### Manual Verification
- Da es sich um eine reine Logik-Komponente handelt, erfolgt die Verifikation ausschließlich über Unit-Tests mit Mocks/Fakes der Repositories.
