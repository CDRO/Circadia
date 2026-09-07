# Implementation Plan: M1 - Datenkern (Fixing T-107)

Der aktuelle Stand des Projekts laut `.ai_state.json` ist Meilenstein M1, Task T-107 (Pflichttests für `DeriveIntervalsUseCase`). Der Task ist als `in_progress` markiert, schlägt aber fehl, da die Dauerberechnung bei Sommerzeitumstellung (DST) fehlerhaft ist.

Da die im `.ai_state.json` als abgeschlossen markierten Tasks T-001 bis T-106 im Dateisystem fehlen, muss die Projektstruktur und die Logik zunächst (wieder-)hergestellt werden, bevor der Bugfix für T-107 verifiziert werden kann.

## User Review Required

> [!IMPORTANT]
> **Fehlende Dateien:** Obwohl `.ai_state.json` angibt, dass M0 und Teile von M1 abgeschlossen sind, fehlen die entsprechenden Gradle-Module und Quellcodedateien im Projektordner. Ich werde diese gemäß der Spezifikation in `BUILD_PLAN.md` und `TASKS.md` neu erstellen, um eine lauffähige Basis für T-107 zu haben.

> [!WARNING]
> **Bugfix T-107:** Der Fehler bei der Zeitumstellung resultiert laut Notiz aus einer falschen Verwendung von `plusDays` in der Bucketing-Logik. Ich werde sicherstellen, dass `DeriveIntervalsUseCase` auf UTC-Millisekunden basiert und `DayBucketing` die IANA-Zeitzonen korrekt berücksichtigt.

## Proposed Changes

### Projektstruktur & M0 (Gerüst)

Wiederherstellung der Modulstruktur und Kern-Utilities, um das Projekt kompilierbar zu machen.

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/tizia/Projekte/Circadia/settings.gradle.kts)
Einbinden der Module `:core:model`, `:core:common`, `:core:domain`, `:core:database`, `:core:data`, `:core:datastore`.

#### [MODIFY] [gradle/libs.versions.toml](file:///C:/Users/tizia/Projekte/Circadia/gradle/libs.versions.toml)
Ergänzen der notwendigen Abhängigkeiten (Hilt, Room, DataStore, JUnit5, etc.).

#### [NEW] `:core:common`
Implementierung von `Clock`, `DispatcherProvider` und `TimeZoneHelper`.

#### [NEW] `:core:model`
Definition von `Person`, `StateEvent`, `Interval` und Enums.

---

### M1 (Datenkern)

Fokus auf die Intervall-Ableitung und den Bugfix für T-107.

#### [MODIFY] [DeriveIntervalsUseCase.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/main/kotlin/ch/circadia/tracker/core/domain/DeriveIntervalsUseCase.kt)
Implementierung der 6 Regeln aus `{#a-5-3}`. Sicherstellen, dass Intervalle rein auf UTC-Millisekunden basieren, um DST-Probleme in der Dauer zu vermeiden.

#### [NEW] [DeriveIntervalsUseCaseTest.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/test/kotlin/ch/circadia/tracker/core/domain/DeriveIntervalsUseCaseTest.kt)
Implementierung der Pflichttestfälle 1–11. Fix der Fälle 8 und 9 (DST).

#### [NEW] [DayBucketing.kt](file:///C:/Users/tizia/Projekte/Circadia/core/domain/src/main/kotlin/ch/circadia/tracker/core/domain/DayBucketing.kt)
Logik zur Zuordnung von Intervallen zu Tagen (Tagesgrenze 18:00 Uhr). Korrekte Handhabung von Zonen-Offsets an Umstellungstagen.

## Verification Plan

### Automated Tests
- `./gradlew.bat :core:domain:testDebugUnitTest`
- Spezieller Fokus auf `DeriveIntervalsUseCaseTest` (Pflichtfälle 8 & 9).
- `./gradlew.bat ktlintCheck detekt lint` (Verification Gate).

### Manual Verification
- Kontrolle der abgeleiteten Intervalle in den Testlogs für die DST-Übergänge (Sommer 23h, Winter 25h).
