# M1 — Datenkern: Tasks

| ID | Task | Status | Spec |
|---|---|---|---|
| T-101 | `:core:model`: `Person`, `PersonId`, `StateEvent`, `SleepState`, `EventSource`, `Interval`, `Entitlement`. Reine Datenklassen. | completed | {#a-5-1} |
| T-102 | `:core:database`: Room-Entities und Schema v1 für `persons`, `state_events`, `widget_bindings`, `research_consents`. `exportSchema = true`, Schemadatei einchecken. Kein `fallbackToDestructiveMigration`. | completed | {#a-5-2} |
| T-103 | DAOs: `PersonDao`, `StateEventDao` (Insert, Flow-Queries nach Person und Zeitfenster, Void statt Delete), `WidgetBindingDao`. DAO-Tests gegen In-Memory-DB. | completed | {#a-5-2} |
| T-104 | `:core:domain`: Repository-Interfaces `PersonRepository`, `StateEventRepository`, `WidgetBindingRepository`, `EntitlementRepository`. | completed | {#a-3-1} |
| T-105 | `:core:data`: Implementierungen der Repositories, Hilt-Bindings, Mapping Entity↔Domain. | completed | {#a-3-2} |
| T-106 | `DeriveIntervalsUseCase` mit allen sechs Regeln. Reine Funktion. | completed | {#a-5-3} |
| T-107 | Tests 1–11 aus der Pflichtliste für `DeriveIntervalsUseCase`, inklusive der beiden Sommerzeit-Fälle und des Zeitzonenwechsels. Kein Task danach beginnt, bevor diese grün sind. | in_progress | {#a-11-2} |
| T-108 | `DayBucketing`: Zuordnung von Intervallen zu Auswertungstagen mit konfigurierbarer Tagesgrenze (Default 18:00). Tests inkl. Umstellungstage. | pending | {#a-5-4} |
| T-109 | `RecordStateEventUseCase`: schreibt ein Event, rundet auf die Minute, setzt Zone und Quelle. Idempotent gegen Doppeltipps innerhalb von 2 s. | pending | {#a-6-3} |
| T-110 | `:core:datastore`: Proto-DataStore für Einstellungen (Tagesgrenze, Diagrammmodus) und Entitlement-Cache. | pending | {#a-3-3} |

> **Hinweis:** Laut `.ai_state.json` sind T-101 bis T-106 bereits als "abgeschlossen" markiert, jedoch fehlen die Dateien im aktuellen Branch. Sie werden im Rahmen von T-107 wiederhergestellt/korrigiert.
