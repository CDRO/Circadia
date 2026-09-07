---
doc_id: task_backlog
project: circadia
lang: de
version: 1.0.0
spec: BUILD_PLAN.md
state: .ai_state.json
translatable: true
translation_notes: >
  Task-IDs (T-xxx), Meilenstein-IDs (M0–M10), Anker-Verweise ({#a-...}),
  Dateipfade, Klassennamen und Shell-Befehle NICHT übersetzen.
---

# Task-Backlog {#t-0}

**Für den Agenten:** Genau ein Task ist gleichzeitig `in_progress`. Tasks werden in der angegebenen Reihenfolge abgearbeitet, außer `blocked_by` verlangt etwas anderes. Nach jedem abgeschlossenen Task: Verifikations-Gate ({#a-11-1} in `BUILD_PLAN.md`) durchlaufen, dann `.ai_state.json` aktualisieren, dann Commit.

**Definition of Done (gilt für jeden Task):**

1. Code implementiert, kein `TODO`, kein auskommentierter Code.
2. Tests geschrieben und grün. Kein `@Ignore`, kein abgeschaltetes Lint.
3. `./gradlew ktlintCheck detekt lint testDebugUnitTest` grün.
4. Keine nutzersichtbaren Strings im Code (siehe `{#a-10}`).
5. `.ai_state.json` aktualisiert.
6. Ein Commit pro Task, Format: `<typ>(<modul>): <beschreibung> [T-xxx]`, z. B. `feat(widget): Zahlenrad-Overlay mit Snap-Fling [T-402]`.

**Aufwandsschätzung `size`:** `S` ≈ 1 Sitzung, `M` ≈ 2–3 Sitzungen, `L` ≈ Aufteilen, sobald es konkret wird.

---

## M0 — Gerüst {#t-m0}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-001 | Gradle-Projekt anlegen: `settings.gradle.kts`, Version Catalog `gradle/libs.versions.toml`, `build-logic` mit Convention-Plugins (`circadia.android.library`, `circadia.android.application`, `circadia.jvm.library`, `circadia.hilt`, `circadia.compose`). | M | – | {#a-3-3} |
| T-002 | Alle Module aus der Tabelle anlegen, leer, mit korrekten Abhängigkeitsrichtungen. Ein Test, der die Abhängigkeitsregeln prüft (z. B. `:core:domain` hat keine Android-Klassen im Klassenpfad). | M | T-001 | {#a-3-2} |
| T-003 | Statische Analyse einrichten: ktlint, detekt, Android Lint mit `HardcodedText` auf `error`, `MissingTranslation` auf `error`. | S | T-002 | {#a-10} |
| T-004 | Test-Infrastruktur: JUnit5, Turbine, Truth/Kotest-Assertions, Robolectric, Roborazzi. Ein Beispieltest pro Modultyp, der beweist, dass die Runner funktionieren. | M | T-003 | {#a-11} |
| T-005 | CI-Workflow (`.github/workflows/ci.yml`): Build + Verifikations-Gate bei jedem Push. | S | T-004 | {#a-11-1} |
| T-006 | `:core:designsystem`: Material-3-Theme hell/dunkel, dynamische Farben ab Android 12, Typografie, Farbrollen für Personen (kategoriale Palette, in beiden Themes geprüft), Icon-Set Sonne/Mond. | M | T-002 | {#a-7-2} |
| T-007 | `:core:common`: `Clock`-Interface (`SystemClock`, `FakeClock`), `DispatcherProvider`, Zeitzonen-Helfer, `Result`-Typ. Vollständig getestet. | S | T-002 | {#a-4-4} |
| T-008 | `:app`: `Application` mit Hilt, leere `MainActivity` mit Compose-Navigation-Gerüst, Splash. | S | T-006, T-007 | {#a-3-2} |
| T-010 | Feature-Schalter-Infrastruktur: `FeatureFlags` in `:core:common`, `buildConfigField` aus Gradle, Gradle-Property `-PresearchMode` nur für `debug`. Standard in allen Buildtypen `false`. Ein Test je Zustand. | S | T-002 | {#a-8-4} |
| T-009 | Benchmark-Infrastruktur: `androidx.benchmark` (Microbenchmark + Macrobenchmark), ein Beispielbenchmark, CI-Schritt der die Ergebnisse als Artefakt ablegt. **Voraussetzung für jede spätere Portierung nach C++** – ohne Messung keine Portierung. | M | T-004 | {#a-3-4} |

**Abnahme M0:** `./gradlew build` grün, App startet, CI läuft.

---

## M1 — Datenkern {#t-m1}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-101 | `:core:model`: `Person`, `PersonId`, `StateEvent`, `SleepState`, `EventSource`, `Interval`, `Entitlement`. Reine Datenklassen. | S | T-002 | {#a-5-1} |
| T-102 | `:core:database`: Room-Entities und Schema v1 für `persons`, `state_events`, `widget_bindings`, `research_consents`. `exportSchema = true`, Schemadatei einchecken. Kein `fallbackToDestructiveMigration`. | M | T-101 | {#a-5-2} |
| T-103 | DAOs: `PersonDao`, `StateEventDao` (Insert, Flow-Queries nach Person und Zeitfenster, Void statt Delete), `WidgetBindingDao`. DAO-Tests gegen In-Memory-DB. | M | T-102 | {#a-5-2} |
| T-104 | `:core:domain`: Repository-Interfaces `PersonRepository`, `StateEventRepository`, `WidgetBindingRepository`, `EntitlementRepository`. | S | T-101 | {#a-3-1} |
| T-105 | `:core:data`: Implementierungen der Repositories, Hilt-Bindings, Mapping Entity↔Domain. | M | T-103, T-104 | {#a-3-2} |
| T-106 | **`DeriveIntervalsUseCase`** mit allen sechs Regeln. Reine Funktion. | M | T-104 | {#a-5-3} |
| T-107 | Tests 1–11 aus der Pflichtliste für `DeriveIntervalsUseCase`, inklusive der beiden Sommerzeit-Fälle und des Zeitzonenwechsels. Kein Task danach beginnt, bevor diese grün sind. | M | T-106 | {#a-11-2} |
| T-108 | `DayBucketing`: Zuordnung von Intervallen zu Auswertungstagen mit konfigurierbarer Tagesgrenze (Default 18:00). Tests inkl. Umstellungstage. | M | T-106 | {#a-5-4} |
| T-109 | `RecordStateEventUseCase`: schreibt ein Event, rundet auf die Minute, setzt Zone und Quelle. Idempotent gegen Doppeltipps innerhalb von 2 s. | S | T-105 | {#a-6-3} |
| T-110 | `:core:datastore`: Proto-DataStore für Einstellungen (Tagesgrenze, Diagrammmodus) und Entitlement-Cache. | S | T-101 | {#a-3-3} |

**Abnahme M1:** Ereignisse lassen sich schreiben und zu Intervallen ableiten; Testfälle 1–11 grün.

---

## M2 — Personen & Onboarding {#t-m2}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-201 | `:feature:persons`: Personenliste (Compose), leerer Zustand, Sortierung per Drag. | M | T-105, T-006 | {#a-3-2} |
| T-202 | Person anlegen/bearbeiten: Name (Pflicht, 1–40 Zeichen, Duplikate erlaubt aber Warnhinweis), Farbe automatisch aus `colorSeed`, manuell überschreibbar. | M | T-201 | {#a-5-1} |
| T-203 | Person archivieren (Soft-Delete) und endgültig löschen (mit Bestätigung und Hinweis auf Datenverlust; kaskadiert auf Events und Widget-Bindungen). | S | T-202 | {#a-5-1} |
| T-204 | `PersonSetupActivity` im Modus `FIRST_RUN`: startet direkt im Namensfeld mit offener Tastatur, ein Feld, ein Button, kein Menü. Liefert die neue `PersonId` als Ergebnis zurück. | M | T-202 | {#a-6-4} |
| T-205 | `strings.xml` (de) für M2 vollständig, `values-en/fr/it` mit identischem Key-Satz angelegt. | S | T-204 | {#a-10} |

**Abnahme M2:** Personen vollständig verwaltbar; `PersonSetupActivity` liefert ein Ergebnis an einen Aufrufer.

---

## M3 — Widget: Basis {#t-m3}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-301 | `:widget`: `CircadiaWidget : GlanceAppWidget`, `CircadiaWidgetReceiver`, `appwidget-provider`-XML mit `SizeMode.Responsive`, `targetCell*`, `configure`-Attribut, Vorschaubild. | M | T-006, T-105 | {#a-6-1} |
| T-302 | `WidgetUiState` + `GlanceStateDefinition` (Proto-DataStore pro `GlanceId`). | S | T-301 | {#a-6-2} |
| T-303 | Widget-Layout: Personenname oben links, zwei Rad-Attrappen mit Rastfenster und Verlaufsmaske, Sonne/Mond-Buttons. Drei Größenvarianten. | L | T-301 | {#a-6-1} |
| T-304 | **Live-Zeit über `AndroidRemoteViews` + `TextClock`** im Modus `LIVE`. 12h/24h aus `DateFormat.is24HourFormat`. | M | T-303 | {#a-4-3} |
| T-305 | `WidgetConfigActivity`: Personenauswahl, Zero-State-Weiche nach `PersonSetupActivity`, `RESULT_OK`/`RESULT_CANCELED`-Semantik korrekt. | M | T-204, T-301 | {#a-6-4} |
| T-306 | Sonne/Mond als `ActionCallback` → `RecordStateEventUseCase`, danach `updateAll` für alle Widgets derselben Person. Haptik `CONFIRM`. Kein App-Start. | M | T-109, T-303 | {#a-6-3} |
| T-307 | Buttonzustände aus dem letzten Event der Person ableiten (nicht aus dem Widget-Zustand). Ausgegraut = klickbar mit Hinweis. | S | T-306 | {#a-6-2} |
| T-308 | Fehlerzustand: zugeordnete Person existiert nicht mehr → Widget zeigt „neu zuordnen" und öffnet bei Tap die Konfiguration. | S | T-305 | {#a-11-2} |
| T-309 | Tests 12, 13, 16, 17, 18, 19, 20. | M | T-307, T-308 | {#a-11-2} |
| T-310 | Roborazzi-Screenshottests für alle drei Widgetgrößen × hell/dunkel × drei Buttonzustände. | M | T-303 | {#a-11-1} |

**Abnahme M3:** Widget platzierbar, zeigt die laufende Systemzeit, erfasst Ereignisse, Zero-State funktioniert. **Ab hier ist das Produkt benutzbar.**

---

## M4 — Widget: Zahlenräder {#t-m4}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-401 | `WheelPickerActivity` + `Theme.Circadia.Transparent`, öffnet ohne sichtbaren Aktivitätswechsel, Tap außerhalb verwirft. | M | T-303 | {#a-6-5} |
| T-402 | Zwei Compose-Räder mit `rememberSnapFlingBehavior`, virtuell endlos, initial auf den angezeigten Wert gescrollt, Verlaufsmaske, Haptik `CLOCK_TICK` pro Raste. | L | T-401 | {#a-6-5} |
| T-403 | 12h-Modus mit AM/PM-Element. | S | T-402 | {#a-10} |
| T-404 | Automatische Bestätigung 400 ms nach Stillstand + expliziter Bestätigungsbereich. Ergebnis zurück an das Widget. | M | T-402 | {#a-6-5} |
| T-405 | `HELD`-Modus im Widget: statischer Wert statt `TextClock`, sichtbarer Halte-Indikator, `holdExpiresAtUtcMillis`. | M | T-404, T-304 | {#a-6-2} |
| T-406 | Einmaliger `AlarmManager.set()` auf das Hold-Ende **plus** Ablaufprüfung bei jedem Render (Alarm ist nur Optimierung). Kein `SCHEDULE_EXACT_ALARM`. | M | T-405 | {#a-2-2} |
| T-407 | Hold endet sofort beim Drücken von Sonne/Mond; das Event bekommt die gehaltene Zeit als `occurredAt`. | S | T-405, T-306 | {#a-6-3} |
| T-408 | Barrierefreiheit: `contentDescription`, TalkBack-Inkremente auf den Rädern, Langdruck-Fallback auf System-`TimePicker`. | M | T-402 | {#a-6-5} |
| T-409 | Tests 14 und 15 (60-s-Regel, verspäteter Alarm). | S | T-406 | {#a-11-2} |

**Abnahme M4:** Zeit lässt sich per Fling einstellen, hält exakt 60 s, Ereignis wird zur eingestellten Zeit gespeichert.

---

## M5 — Auswertung {#t-m5}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-501 | **`dataviz`-Skill lesen**, bevor die erste Zeile Diagrammcode entsteht. Palette, Achsen, Legenden, Zustände „keine Daten" festlegen und in `:core:designsystem` ablegen. | S | T-006 | {#a-7-2} |
| T-502 | Aktogramm-Renderer (Compose `Canvas`): eine Zeile pro Tag, Single-Plot. Zeitachse, Tagesbeschriftung, Schlafbalken. | L | T-108, T-501 | {#a-7-1} |
| T-503 | Double-Plot-Modus (48 h pro Zeile, versetzt). Umschalter. | M | T-502 | {#a-7-1} |
| T-504 | 7-Tage-Ansicht als Standard, Scrollen und Zoomen der Zeitachse vorbereitet (auch wenn `FREE` bei 7 Tagen begrenzt). | M | T-502 | {#a-8-3} |
| T-505 | Personenauswahl für die Auswertung (Mehrfachauswahl) + Modus „übereinandergelegt" mit halbtransparenten Balken und `BlendMode.Multiply`. | L | T-503 | {#a-7-2} |
| T-506 | Modus „nebeneinander" mit synchronisierter Zeitachse. | M | T-505 | {#a-7-2} |
| T-507 | Kennzahlen der freien Stufe: Gesamtschlafzeit, Anzahl Episoden, längste Wachphase, Einschlaf-/Aufwachzeit. Je mit Mindestdatenprüfung. | M | T-108 | {#a-7-3} |
| T-508 | Detailblatt bei Tap auf einen Balken: Zeitpunkt korrigieren, ungültig markieren, Notiz. Erzeugt `CORRECTION`-Events mit `supersedesEventId`. | L | T-105 | {#a-7-4} |
| T-509 | Verlaufsansicht eines Eintrags (welche Korrekturen es gab). | S | T-508 | {#a-7-4} |
| T-510 | Leerzustände und Ladezustände für alle Ansichten. Screenshottests. | M | T-506 | {#a-11-1} |
| T-511 | **Messung:** Macrobenchmark des Aktogramm-Renderers mit synthetischen Daten über 1 Monat, 1 Jahr und 3 Jahre, auf einem Mittelklassegerät. Frame-Budget 16 ms. Ergebnis in `.ai_state.json` protokollieren. Reißt es das Budget → T-911 wird verbindlich. | M | T-505, T-009 | {#a-3-4} |

**Abnahme M5:** Verläufe pro Person und im Vergleich sichtbar; Einträge korrigierbar.

---

## M6 — Export {#t-m6}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-601 | `:feature:export`: CSV-Export der Roh-Events (alle Spalten aus `state_events`, RFC-4180-konform maskiert, UTF-8 mit BOM für Excel). | M | T-105 | {#a-8-3} |
| T-602 | CSV-Export der abgeleiteten Intervalle als zweite Datei. | S | T-601 | {#a-5-3} |
| T-603 | JSON-Export (`kotlinx.serialization`), versioniertes Schema mit `schemaVersion`. | M | T-601 | {#a-8-3} |
| T-604 | Speichern über `ACTION_CREATE_DOCUMENT` (SAF), kein Schreiben in fremde Verzeichnisse, keine Speicher-Berechtigung. Fortschrittsanzeige bei großen Datenmengen. | M | T-603 | {#a-9-2} |
| T-605 | JSON-Import (Roundtrip), Events werden mit `source = IMPORT` übernommen, Duplikate über `id` erkannt. | M | T-603 | {#a-11-2} |
| T-606 | Tests 26–28. | S | T-605 | {#a-11-2} |

**Abnahme M6:** Vollständiger Rohdatenexport in der kostenlosen Version, Roundtrip verlustfrei.

---

## M7 — Monetarisierung {#t-m7}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-701 | `:core:billing`: Play Billing 9.x einbinden, `BillingClient`-Lebenszyklus, Wiederverbindung mit Backoff. | M | T-104 | {#a-8-1} |
| T-702 | Abo-Produkt abfragen (`queryProductDetailsAsync`), Kaufablauf, `acknowledgePurchase` innerhalb von 3 Tagen. | M | T-701 | {#a-8-1} |
| T-703 | `EntitlementRepository`-Implementierung: Quellen `SUBSCRIPTION`, `RESEARCH`, `FREE`; `lastVerifiedAt`; 7-Tage-Kulanz bei fehlender Verbindung. | M | T-702, T-110 | {#a-8-1} |
| T-704 | `FeatureAccess` mit `Feature.UNLIMITED_HISTORY` und `Feature.ADVANCED_ANALYTICS`. **Einzige** Entscheidungsstelle im Projekt. | S | T-703 | {#a-8-3} |
| T-705 | 7-Tage-Grenze in der Auswertung durchsetzen; Export bleibt bewusst unbegrenzt. | S | T-704, T-504 | {#a-8-3} |
| T-706 | `:feature:paywall`: Abo-Screen mit Preis, Laufzeit, Kündigungsweg, Vergleichstabelle Frei/Pro, Link zum Forschungsweg. Preis immer aus `ProductDetails`, nie hart kodiert. | M | T-702 | {#a-9-2} |
| T-707 | Kaufwiederherstellung, Ablauf-, Kulanz- und Fehlerzustände in der UI. | M | T-703 | {#a-8-1} |
| T-708 | Tests 21–25 mit gefälschtem `BillingClient`. | M | T-704 | {#a-11-2} |

**Abnahme M7:** Abo kaufbar (Testspur), Freischaltung wirkt, Ablauf und Offline-Fall korrekt.

---

## M8 — Forschungsmodus: Technik & Logik {#t-m8}

> **Nicht blockiert.** Der gesamte Meilenstein wird gegen Platzhalter-Inhalte gebaut und liegt hinter `FEATURE_RESEARCH_MODE = false`. In der App ist nichts davon sichtbar. Die Inhalte kommen in M11, siehe ADR-007 `{#a-4-7}` und `{#a-8-5}`.
>
> **Der Schalter bleibt in diesem gesamten Meilenstein aus.** Getestet wird über die Testkonfiguration und `-PresearchMode=true`, nicht durch Umlegen des Standardwerts.

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-801 | Aufklärungsseite: Volltext scrollbar, „Weiter" erst nach vollständigem Scrollen, Text aus `res/raw/consent_draft_0.md` mit `status: draft` im Kopf. Struktur und Versionierung sind fertig, der Text ist Platzhalter. | M | T-706, T-010 | {#a-8-5} |
| T-802 | Umfrage-Motor, **inhaltsagnostisch**: liest den Fragebogen aus `assets/surveys/*.json`. Fragetypen Einfachauswahl, Mehrfachauswahl, Spanne, Freitext; überspringbar außer bei Pflichtmarkierung. Ausgeliefert mit `survey-draft-0.json` als Platzhalter. | L | T-801 | {#a-8-5} |
| T-803 | Gestufte Einwilligung: getrennte Schalter für Umfrageantworten und Schlafdaten. Speicherung von Zeitpunkt, Textversion und `consent_text_hash` in `research_consents`. Reine Logik, vollständig testbar. | M | T-802 | {#a-8-2} |
| T-804 | `AnonymizeUseCase`: entfernt Namen, `PersonId`, Geräte- und Account-Kennungen; rundet Zeitstempel auf 5 Minuten; ersetzt Datumsangaben durch Wochentag plus Tagesoffset; reduziert die Zone auf eine UTC-Offset-Gruppe. Inklusive eines Tests, der beweist, dass kein Klarname im Ergebnis vorkommt. **Unabhängig von Inhalten und Endpunkt.** | L | T-803 | {#a-8-2} |
| T-805 | `ResearchUploadEndpoint`-Interface plus `NoopUploadEndpoint` als Standardimplementierung. Upload-Worker (WorkManager): Bündelung, exponentieller Backoff, Abbruch, Nachweis der Reihenfolge — vollständig gegen den Noop-Endpunkt getestet, ohne dass ein Server existiert. | L | T-804 | {#a-8-5} |
| T-806 | Freischaltung `RESEARCH` für 365 Tage, Erinnerung 14 Tage vor Ablauf, Verlängerung über eine neue Umfrage. | M | T-803, T-703 | {#a-8-2} |
| T-807 | Widerruf: jederzeit erreichbar, funktioniert offline über eine Warteschlange, löscht das lokale Pseudonym, stößt eine Löschanfrage an. Statusanzeige in der UI. | M | T-805 | {#a-9-1} |
| T-808 | „Alle meine Daten löschen" in den Einstellungen. **Steht ausdrücklich nicht hinter dem Feature-Schalter** — DSGVO-Pflicht unabhängig vom Forschungsmodus. | M | T-105 | {#a-9-1} |
| T-809 | Verdrahtung des Schalters: bei `false` wird kein Einstiegspunkt gerendert (Menü, Abo-Screen), kein Upload-Worker registriert, keine Einwilligung speicherbar, kein Pseudonym erzeugt. Pflichttestfälle 33, 34, 36. | M | T-807, T-010 | {#a-8-4} |
| T-810 | **Platzhalter-Sicherung:** Test schlägt fehl, sobald der Schalter an ist und ein Inhalt noch `status: draft` trägt oder der Endpunkt `NoopUploadEndpoint` ist. Pflichttestfall 35. Diese Sicherung wird erst in T-1105 entschärft. | M | T-809 | {#a-8-5} |

**Abnahme M8:** Der Forschungsweg ist mit `-PresearchMode=true` vollständig durchlaufbar bis in die Warteschlange, die Anonymisierung ist testbar bewiesen, der Widerruf funktioniert. Im Standardbuild ist nichts davon sichtbar oder aktiv, und die Platzhalter-Sicherung greift.

---

## M9 — Pro-Analytik {#t-m9}

Jeder Task hier liefert: Algorithmus + Tests mit veröffentlichten Referenzwerten + Mindestdatenprüfung + UI-Karte mit Definition, Quelle und Genauigkeitsvorbehalt.

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-901 | Hauptschlafepisode je Auswertungstag bestimmen (längste `ASLEEP`-Episode im Bucket, Nickerchen getrennt ausweisen). | M | T-108 | {#a-7-3} |
| T-902 | Schlafmitte (Mid-Sleep) und Verteilung über die Woche. | S | T-901 | {#a-7-3} |
| T-903 | MSF<sub>sc</sub> (Chronotyp) mit Schlafschuld-Korrektur; braucht die Unterscheidung Arbeits-/freier Tag → Einstellung „Arbeitstage" pro Person. | M | T-902 | {#a-7-3} |
| T-904 | Sozialer Jetlag. | S | T-903 | {#a-7-3} |
| T-905 | Sleep Regularity Index (SRI), 0–100, mindestens 7 vollständige Tage. | M | T-901 | {#a-7-3} |
| T-906 | Interdaily Stability (IS) und Intradaily Variability (IV) auf stündlich gerasterten Zuständen. | M | T-901 | {#a-7-3} |
| T-907 | Trendlinien und gleitende Mittel (7 und 28 Tage) für Schlafdauer und Schlafmitte. | M | T-902 | {#a-7-3} |
| T-908 | Analytik-Screen: Kennzahlkarten mit Ausklapp-Erklärung, Quellenangabe, „zu wenig Daten"-Zustand. Keine wertenden oder medizinischen Formulierungen. | L | T-907 | {#a-9-3} |
| T-909 | **Messung:** Microbenchmark der Kotlin-Implementierungen von SRI, IS und IV über 1 Jahr und über 5 Jahre Daten, zusätzlich für die Vergleichsansicht mit vier Personen. Schwellen aus Regel 1 in `{#a-3-4}`. Ergebnis protokollieren. | M | T-905, T-906, T-009 | {#a-3-4} |
| T-910 | `:core:native` anlegen: CMake, gepinnte `ndkVersion`, C++20, `-Wall -Wextra -Werror`, ASan/UBSan im Debug, clang-tidy, GoogleTest auf dem Host, `nativeHostTest`-Gradle-Task, `tools/check_so_alignment.sh` für die 16-KB-Seitenausrichtung. Noch ohne Fachlogik. | L | T-909 | {#a-3-4} |
| T-911 | Rhythmus-Analytik in C++: SRI, IS, IV, gleitende Mittel, Autokorrelation auf einem minutengerasterten Zustandsvektor. JNI-Fassade in `jni_bridge.cpp`, ein Aufruf pro Auswertung, Übergabe als primitive Arrays oder direkter `ByteBuffer`. Kern ohne JNI- und Android-Header. | L | T-910 | {#a-3-4} |
| T-912 | Aktogramm-Rasterisierung und Downsampling in C++, sofern T-511 das Budget gerissen hat. Sonst als `skipped` mit Verweis auf die Messung schließen. | L | T-910, T-511 | {#a-3-4} |
| T-913 | Kotlin-Referenzimplementierungen bleiben erhalten und werden als Fallback bei `UnsatisfiedLinkError` verdrahtet. Pflichttestfälle 29–32 (Parität, Randfälle, Fallback-Pfad). | M | T-911 | {#a-11-2} |
| T-914 | Auslieferung: Android App Bundle mit ABI-Splits (`arm64-v8a`, `x86_64`), Größenbudget dokumentiert, CI prüft die Seitenausrichtung aller `.so`. | M | T-911 | {#a-3-4} |

**Abnahme M9:** Alle Pro-Kennzahlen berechnet, belegt und mit Vorbehalt beschriftet. Der native Kern ist gemessen begründet, paritätsgetestet, sanitizer-sauber und 16-KB-ausgerichtet – oder nachweislich nicht nötig.

---

## M10 — Release-Härtung {#t-m10}

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-1001 | i18n-Audit: kein Key ohne Übersetzung, kein String im Code, alle Plurale korrekt, `MissingTranslation` als Fehler durchgesetzt. | M | alle | {#a-10} |
| T-1002 | Barrierefreiheits-Audit: TalkBack durch alle Kernabläufe, Kontrastprüfung, Touchziele ≥ 48 dp, größte Systemschrift. | M | alle | {#a-6-5} |
| T-1003 | R8/ProGuard-Regeln, `minifyEnabled`, Release-Build testen (Room, Hilt, kotlinx.serialization, Glance brauchen Regeln; JNI-Einstiegspunkte dürfen nicht wegoptimiert werden). | M | alle | {#a-3-3} |
| T-1008 | Nativer Release-Build: LTO, `-O2`, Symbole für die Play Console hochladbar ablegen (`.so` mit Debug-Symbolen getrennt), Absturzberichte lesbar. | M | T-914 | {#a-3-4} |
| T-1004 | Data-Safety-Formular ausfüllen, Datenschutzerklärung verlinken, Health-Apps-Policy prüfen. | M | T-808 | {#a-9-2} |
| T-1005 | Store-Assets: Beschreibung (de/en), Screenshots inkl. Widget auf dem Homescreen, Feature-Grafik. | M | alle | – |
| T-1006 | Manuelle Prüfliste `{#a-11-3}` vollständig abarbeiten und das Ergebnis in `.ai_state.json` protokollieren. | M | alle | {#a-11-3} |
| T-1007 | Interner Test-Track, Abo im Testkauf prüfen, Upgrade-/Kündigungspfad durchspielen. | M | T-708 | {#a-8-1} |

**Abnahme M10:** Release-Kandidat auf dem internen Track, Prüfliste dokumentiert abgehakt.

---

---

## M11 — Forschungsmodus: Inhalte & Scharfschaltung {#t-m11}

> **Blockiert durch Q1, Q2, Q3, Q6** aus `{#a-15}`. Läuft nach dem v1.0-Release und mündet in v1.1. Kann vorgezogen werden, sobald die Fragen beantwortet sind — die Technik steht seit M8 bereit.

| ID | Task | size | blocked_by | Spec |
|---|---|---|---|---|
| T-1101 | Endgültigen Fragebogen als `assets/surveys/survey-1.json` einpflegen, `status: final`, Version festgeschrieben. Kein Codeumbau nötig — der Motor ist inhaltsagnostisch. | M | Q6 | {#a-8-5} |
| T-1102 | Endgültigen Aufklärungs- und Einwilligungstext einpflegen, `status: final`, `consent_text_hash` neu berechnet. Der Text ist juristisch freigegeben; der Agent pflegt ihn nur ein, er verfasst ihn nicht. | M | Q1, Q3 | {#a-9-1} |
| T-1103 | `HttpUploadEndpoint` gegen den echten Endpunkt: TLS, Authentisierung, Fehlerbehandlung, Zeitüberschreitungen. Ersetzt `NoopUploadEndpoint` per Dependency Injection — die Warteschlangenlogik bleibt unverändert. | L | Q2 | {#a-8-2} |
| T-1104 | Data-Safety-Formular aktualisieren (jetzt **mit** optionaler Weitergabe von Gesundheitsdaten), Datenschutzerklärung ergänzen, Store-Beschreibung anpassen. | M | T-1102 | {#a-9-2} |
| T-1105 | Schalter auf `true`, Platzhalter-Sicherung entschärfen, `FEATURE_RESEARCH_MODE` und alle zugehörigen Verzweigungen **entfernen** (Schalter-Hygiene, Regel 5 in `{#a-8-4}`). | M | T-1103, T-1104 | {#a-8-4} |
| T-1106 | End-to-End-Durchlauf gegen den echten Endpunkt in einer Testumgebung: Umfrage, Einwilligung, Upload, Widerruf, Löschbestätigung. Danach Release v1.1. | M | T-1105 | {#a-11-3} |

**Abnahme M11:** Der Forschungsweg ist in der Produktivversion sichtbar, funktioniert gegen den echten Endpunkt, der Widerruf ist bestätigt, das Data-Safety-Formular stimmt, und der Feature-Schalter existiert nicht mehr.

---

## Reihenfolge auf einen Blick {#t-order}

```
M0 Gerüst ──► M1 Datenkern ──► M2 Personen ──► M3 Widget Basis ──► M4 Räder
                   │                                  │
                   └──────────► M5 Auswertung ────────┘
                                     │
                                     ├──► M6 Export
                                     ├──► M7 Monetarisierung ──► M8 Forschung (Logik, Schalter AUS)
                                     └──► M9 Pro-Analytik
                                                  │
                                                  ▼
                                          M10 Release v1.0
                                          (Forschungsmodus unsichtbar)
                                                  │
                                                  ▼
                                    M11 Inhalte + Schalter AN ──► v1.1
                                    (wartet auf Q1, Q2, Q3, Q6)
```

M6 kann jederzeit nach M1 gebaut werden und ist ein guter Füller, wenn M5 auf eine Designentscheidung wartet. M11 ist der einzige Meilenstein, der auf Antworten von außen wartet — alles davor läuft ohne.
