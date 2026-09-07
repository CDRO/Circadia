---
doc_id: build_plan
project: circadia
lang: de
version: 1.0.0
target_audience: gemini-coding-agent
human_readable: true
translatable: true
translation_notes: >
  Alle Überschriften haben stabile Anker-IDs ({#...}). Diese IDs, Code-Bezeichner,
  Dateipfade, Gradle-Koordinaten und die Einträge in Abschnitt 13 (Glossar,
  Spalte "Bezeichner") dürfen bei einer Übersetzung NICHT verändert werden.
  Querverweise im Dokument verwenden ausschließlich Anker-IDs, niemals Überschriftentexte.
companion_files:
  - TASKS.md
  - agent/GEMINI_RULES_ADDENDUM.md
  - agent/ai_state.schema.json
  - agent/ai_state.example.json
---

# Bauplan: Schlaf-Wach-Zyklus Tracker (Android) {#a-0}

**Arbeitstitel:** `Circadia`
**Zielplattform:** Android 8.0 (API 26) bis Android 17 (API 37)
**Stack:** Kotlin · Jetpack Compose · Glance (App-Widget) · Room · DataStore · Hilt · Play Billing 9.x

---

## 0. Wie dieses Dokument zu benutzen ist {#a-0-1}

> **An den Coding-Agenten:** Dieses Dokument ist die *Spezifikation*. Es ist nicht die Reihenfolge der Arbeit.
> Die Reihenfolge steht in `TASKS.md`. Der aktuelle Fortschritt steht in `.ai_state.json`.

Ablauf pro Arbeitssitzung:

1. `.geminirules` lesen (Konventionen, Verbote, Definition of Done).
2. `.ai_state.json` lesen → aktuellen Meilenstein und offenen Task bestimmen.
3. Den relevanten Abschnitt **dieses** Bauplans lesen (der Task in `TASKS.md` verweist per Anker-ID, z. B. `→ {#a-5-3}`).
4. Implementieren, Tests schreiben, Verifikations-Gate durchlaufen ({#a-11}).
5. `.ai_state.json` aktualisieren. Erst dann den nächsten Task beginnen.

**Regel:** Ein Task gilt nur als erledigt, wenn `./gradlew check` grün ist. Nichts anderes zählt.

---

## 1. Produktzusammenfassung {#a-1}

Eine Android-App, mit der eine oder mehrere Personen ihre Schlaf- und Wachphasen erfassen. Die Erfassung passiert primär über ein Homescreen-Widget, das die Zeit wie das Zahlenschloss eines Aktenkoffers darstellt: zwei Zahlenräder (Stunde, Minute), die sich mit der Systemzeit mitdrehen und manuell verstellt werden können. Zwei Symbole – Sonne und Mond – markieren den Beginn einer Wach- bzw. Schlafphase zur angezeigten Zeit.

Die App selbst zeigt die Auswertung: Verläufe pro Person, Vergleich mehrerer Personen, Export der Rohdaten. Eine kostenpflichtige Stufe (CHF 5.–/Jahr) öffnet die unbegrenzte Historie und wissenschaftlich fundierte Auswertungen; alternativ lässt sich diese Stufe für ein Jahr gratis freischalten, indem Nutzende an einer Umfrage teilnehmen und der anonymisierten Weitergabe ihrer Daten zu Forschungszwecken zustimmen.

### 1.1 Leitprinzipien für alle Designentscheidungen {#a-1-1}

| # | Prinzip | Konsequenz |
|---|---|---|
| P1 | **Die Erfassung muss in unter 3 Sekunden gehen.** | Kein Dialog, kein App-Start für den Normalfall. Zwei Taps maximal. |
| P2 | **Rohdaten sind heilig.** | Append-only Event-Log. Korrekturen erzeugen neue Events, sie überschreiben nichts. |
| P3 | **Zeit ist schwierig.** | Immer UTC-Zeitstempel **plus** IANA-Zeitzone speichern. Nie lokale Zeit als Wahrheit. |
| P4 | **Offline ist der Normalfall.** | Kein Netzwerk für Kernfunktionen. Netzwerk nur für Billing und (optional) Forschungs-Upload. |
| P5 | **Kein sichtbarer Text im Code.** | Alle nutzersichtbaren Zeichenketten in `strings.xml`. Siehe {#a-10}. |
| P6 | **Gesundheitsdaten.** | Schlafdaten sind besonders schützenswert. Datenschutz ist keine Nachrüstung. Siehe {#a-9}. |

---

## 2. Harte Plattform-Constraints (zuerst lesen) {#a-2}

Diese Punkte bestimmen die Architektur des Widgets. Der Agent darf sie nicht "wegimplementieren".

### 2.1 Android-Widgets können keine Wischgesten erkennen {#a-2-1}

Android-Homescreen-Widgets werden über `RemoteViews` in einen fremden Prozess (den Launcher) gerendert. Glance ist eine Compose-artige Fassade darüber, ist aber durch dieselben Grenzen eingeschränkt.

Was ein Widget **kann**:

- Klicks auf einzelne Views (`onClick` / `PendingIntent`).
- Zustandsänderungen über `CheckBox`, `Switch`, `RadioButton`.
- Nativ scrollende Listen (`LazyColumn` → `RemoteViews`-Collection).

Was ein Widget **nicht kann**:

- Wischgesten, Drag, Fling oder beliebige Touch-Events abfangen.
- Die Scrollposition einer scrollenden Liste auslesen.
- Frei zeichnen (`Canvas`), eigene Schriften, Animationen.

**Folge:** Die PRD-Anforderung „sobald ein Wischgesten-Scrollen auf den Zahlenrädern erfolgt, wird die Zeit manuell angepasst" ist wortwörtlich auf Android nicht umsetzbar. Sie wird über ADR-002 ({#a-4-2}) erfüllt – mit derselben Bediengeste, nur in einer transparenten Overlay-Activity statt direkt in der Widget-Fläche. Der Unterschied ist für Nutzende ein zusätzlicher Tap auf das Rad; danach ist die Geste identisch (echtes Fling-Scrollen mit Rastung).

### 2.2 Widgets aktualisieren sich nicht sekundengenau {#a-2-2}

`updatePeriodMillis` hat ein Minimum von 30 Minuten. Häufigere Updates brauchen `AlarmManager` oder `WorkManager`; exakte Minutenalarme (`SCHEDULE_EXACT_ALARM`) sind seit Android 12 eingeschränkt und für eine Schlaf-App im Play-Review riskant.

**Lösung:** Der sichtbare Zeitwert im Ruhezustand wird von der Plattform selbst aktualisiert, über ein eingebettetes `TextClock` (`AndroidRemoteViews`-Composable in Glance). Das kostet keinen Akku, keine Alarme und keine Berechtigung, und ist immer exakt. Siehe ADR-003 ({#a-4-3}).

### 2.3 Weitere Constraints {#a-2-4}

- Widget-RemoteViews haben ein Größenlimit (praktisch ~1 MB Bitmap-Budget). Keine großen Bitmaps.
- Die Konfigurations-Activity muss `RESULT_OK` mit `EXTRA_APPWIDGET_ID` zurückgeben, sonst entfernt das System das Widget wieder. Das ist der Hebel für das Zero-State-Handling ({#a-6-3}).
- Play Billing: Ab **31. August 2026** verlangt Google Play für neue Apps und Updates mindestens Billing Library **8**. Wir verwenden **9.x** ({#a-8-1}).

---

## 3. Architektur {#a-3}

### 3.1 Überblick {#a-3-1}

Klassische Clean-Architecture in drei Ringen, umgesetzt als Gradle-Multi-Modul-Projekt. Unidirektionaler Datenfluss, `StateFlow` nach oben, Events nach unten.

```
┌──────────────────────────────────────────────────────────────┐
│  PRÄSENTATION                                                │
│  :app  :feature:*  :widget                                   │
│  Compose UI · Glance UI · ViewModels · Navigation            │
└───────────────────────────┬──────────────────────────────────┘
                            │  UseCases (suspend / Flow)
┌───────────────────────────▼──────────────────────────────────┐
│  DOMÄNE                                                      │
│  :core:domain  :core:model                                   │
│  Zyklus-Ableitung · Analytik · Entitlement-Regeln            │
│  Reines Kotlin. Keine Android-Abhängigkeiten. Voll testbar.  │
│                            │  JNI (ein Aufruf je Auswertung) │
│                 ┌──────────▼───────────┐                     │
│                 │  :core:native (C++)  │  Rhythmus-Analytik  │
│                 │  Rasterisierung      │  {#a-3-4}           │
│                 └──────────────────────┘                     │
└───────────────────────────┬──────────────────────────────────┘
                            │  Repository-Interfaces
┌───────────────────────────▼──────────────────────────────────┐
│  DATEN                                                       │
│  :core:database (Room) · :core:datastore · :core:billing     │
└──────────────────────────────────────────────────────────────┘
```

### 3.2 Modulstruktur {#a-3-2}

Der Agent legt exakt diese Module an. Keine zusätzlichen Module ohne ADR.

| Modul | Typ | Inhalt | Darf abhängen von |
|---|---|---|---|
| `:app` | Application | `Application`-Klasse, Hilt-Setup, Navigation-Graph, `MainActivity` | alle |
| `:core:model` | Kotlin-Lib | `Person`, `StateEvent`, `Interval`, `Entitlement`, Enums | – |
| `:core:common` | Kotlin-Lib | `Clock`-Abstraktion, Dispatcher-Provider, `Result`-Typ, Zeitzonen-Helfer | `:core:model` |
| `:core:database` | Android-Lib | Room-Entities, DAOs, Migrationen, Type-Converter | `:core:model`, `:core:common` |
| `:core:datastore` | Android-Lib | Proto-DataStore: Settings, Entitlement-Cache, Widget-Zuordnung | `:core:model`, `:core:common` |
| `:core:domain` | Kotlin-Lib | Repository-Interfaces, UseCases, Analytik-Algorithmen | `:core:model`, `:core:common` |
| `:core:data` | Android-Lib | Repository-Implementierungen | `:core:domain`, `:core:database`, `:core:datastore` |
| `:core:native` | Android-Lib (NDK) | C++-Kern für rechenintensive Auswertung, JNI-Fassade | `:core:model` |
| `:core:designsystem` | Android-Lib | Material-3-Theme, Farbrollen, Typografie, Icons, Glance-Theme | – |
| `:core:billing` | Android-Lib | Play-Billing-Wrapper, Entitlement-Auflösung | `:core:domain` |
| `:feature:persons` | Android-Lib | Personenliste, Anlegen/Bearbeiten, Onboarding | `:core:domain`, `:core:designsystem` |
| `:feature:timeline` | Android-Lib | Aktogramm, Vergleichsansicht, Detailauswertung | `:core:domain`, `:core:designsystem` |
| `:feature:export` | Android-Lib | CSV-/JSON-Export, `ACTION_CREATE_DOCUMENT` | `:core:domain` |
| `:feature:paywall` | Android-Lib | Abo-Screen, Umfrage, Einwilligung | `:core:domain`, `:core:billing` |
| `:widget` | Android-Lib | Glance-Widget, Receiver, `WidgetConfigActivity`, `WheelPickerActivity` | `:core:domain`, `:core:designsystem` |

**Regel:** `:feature:*`-Module kennen einander nicht. Navigation zwischen Features geht über `:app`.

### 3.3 Technologie-Festlegungen {#a-3-3}

| Zweck | Wahl | Anmerkung |
|---|---|---|
| Sprache | Kotlin 2.x, `jvmToolchain(17)` | |
| UI (App) | Jetpack Compose + Material 3 | |
| UI (Widget) | Glance `androidx.glance:glance-appwidget` | neueste stabile Version |
| Datenbank | Room mit KSP | Migrationen ab Version 1 versioniert |
| Einstellungen | DataStore (Proto) | kein `SharedPreferences` |
| DI | Hilt | inkl. `@AndroidEntryPoint` für Receiver |
| Nebenläufigkeit | Coroutines + Flow | injizierte Dispatcher, nie `Dispatchers.IO` hart |
| Hintergrund | WorkManager | für Forschungs-Upload und Aufräumjobs |
| Billing | Play Billing Library 9.x | {#a-8} |
| Serialisierung | `kotlinx.serialization` | Export + Proto-DataStore |
| Tests | JUnit5, Turbine, Robolectric, Roborazzi, Compose-UI-Test | {#a-11} |
| Nativer Kern | C++20, CMake, NDK (gepinnt), GoogleTest | nur für Messungen belegte Engpässe, {#a-3-4} |
| Build | Gradle Version Catalog (`libs.versions.toml`), Convention-Plugins in `build-logic` | |

### 3.4 Nativer Code (C++ / NDK) {#a-3-4}

**Performance-kritische Komponenten werden in C++ gebaut**, nicht in Kotlin. Sie liegen im Modul `:core:native` und werden über eine schmale JNI-Fassade angesprochen. Siehe ADR-006 ({#a-4-6}).

**Was nativ gehört:**

| Komponente | Warum | Zielmodul |
|---|---|---|
| Rhythmus-Analytik über lange Zeiträume: SRI, IS, IV, gleitende Mittel, Autokorrelation | Arbeitet auf einem minutengerasterten Zustandsvektor. Ein Jahr sind ~525 600 Punkte, mehrere Personen im Vergleich ein Vielfaches. In Kotlin bedeutet das Boxing, GC-Druck und ruckelnde Scrollframes. | `:core:native` |
| Rasterisierung und Downsampling des Aktogramms beim Zoomen über Jahre | Muss im 16-ms-Frame-Budget bleiben, sonst ruckelt die Hauptansicht. | `:core:native` |
| Aggregation über den gesamten Aufzeichnungszeitraum für die Pro-Ansicht | Wächst unbegrenzt mit der Nutzungsdauer. | `:core:native` |
| Serialisierung sehr großer Exporte und die Anonymisierungspipeline | Wird nur nativ, wenn die Messung es verlangt (siehe Regel 1 unten). | `:core:native` (optional) |

**Was ausdrücklich nicht nativ wird:** UI, Datenbankzugriff, Billing, alles Ereignisgetriebene, und die Intervall-Ableitung ({#a-5-3}). Letztere ist die am gründlichsten getestete Stelle im Projekt und arbeitet auf Ereignislisten von überschaubarer Größe – sie bleibt in Kotlin, damit sie lesbar und testbar bleibt.

**Regeln für den nativen Teil:**

1. **Erst messen, dann portieren.** Kein Kotlin-Code wird nach C++ überführt, ohne dass ein `androidx.benchmark`-Microbenchmark vorher belegt, dass er das Budget reißt. Schwelle: mehr als 8 ms auf einem Mittelklassegerät für etwas, das pro Frame läuft, oder mehr als 250 ms für eine Interaktionsantwort. Die Messung wird im Commit dokumentiert.
2. **Die JNI-Grenze ist teuer.** Ein Aufruf pro Auswertung, nicht einer pro Datenpunkt. Übergeben werden primitive Arrays (`long[]`, `int[]`, `float[]`) oder direkte `ByteBuffer`, niemals Objektgraphen. Rückgabe ebenso.
3. **Der C++-Kern ist plattformfrei.** Keine JNI-Typen, keine Android-Header in der Kernlogik – nur in der dünnen Fassadenschicht `jni_bridge.cpp`. So lässt sich der Kern auf dem Host mit GoogleTest testen, ohne Gerät und ohne Emulator.
4. **Speichersicherheit ist nicht verhandelbar.** C++20, `-Wall -Wextra -Werror`, keine rohen `new`/`delete`, `std::span` statt Zeiger-plus-Länge, `clang-tidy` und die Sanitizer ASan und UBSan im Debug-Build und in CI. Ein Absturz im nativen Code nimmt den ganzen Prozess mit und ist im Play-Console-Crashreport kaum lesbar.
5. **Determinismus.** Für jede native Funktion existiert eine Kotlin-Referenzimplementierung, und ein Test vergleicht beide auf denselben Eingaben. Die Kotlin-Fassung darf langsam sein; sie ist die Wahrheit, an der die schnelle Fassung gemessen wird.
6. **Build.** CMake über das Android-Gradle-Plugin, `ndkVersion` im Version Catalog gepinnt. ABIs `arm64-v8a` und `x86_64` (Emulator); 32-Bit nur auf Nachfrage. `-O2`, LTO im Release. Optimierung über explizite SIMD-Nutzung (NEON) erst, wenn ein Benchmark sie rechtfertigt.
7. **16-KB-Speicherseiten.** Pflicht für Play-Uploads mit aktuellem Target-SDK: alle nativen Bibliotheken müssen auf 16 KB ausgerichtet gelinkt sein (`-Wl,-z,max-page-size=16384`). Ein CI-Schritt prüft die Ausrichtung der fertigen `.so`-Dateien.
8. **App-Größe.** Native Bibliotheken vervielfachen das APK. Auslieferung als Android App Bundle mit ABI-Splits ist Pflicht, nicht optional.
9. **Kein Fallback-Zweig ohne Test.** Schlägt das Laden der nativen Bibliothek fehl (`UnsatisfiedLinkError`), fällt die App auf die Kotlin-Referenzimplementierung aus Regel 5 zurück. Dieser Pfad hat einen eigenen Test.


---

## 4. Architekturentscheidungen (ADR) {#a-4}

Diese Entscheidungen sind getroffen. Der Agent setzt sie um und ändert sie nicht eigenmächtig. Wer sie ändern will, schreibt einen neuen ADR-Eintrag in `.ai_state.json` unter `decisions` und begründet ihn.

### ADR-001 — Event-Sourcing statt gespeicherter Zyklen {#a-4-1}

**Entscheidung:** Es werden ausschließlich `StateEvent`-Zeilen gespeichert (Zeitpunkt + Zustand). Schlaf- und Wachintervalle werden zur Laufzeit abgeleitet, nie persistiert.

**Warum:** Ein Intervall hat zwei Enden, die zu unterschiedlichen Zeiten entstehen – das führt zu halboffenen Zeilen, Race Conditions zwischen Widget und App, und zu kaputten Daten, sobald jemand nachträglich korrigiert. Ein Append-only-Log hat keinen dieser Zustände. Der Export der Rohdaten (PRD 3) wird damit trivial, Korrekturen sind nachvollziehbar (P2), und die Ableitung ist eine reine Funktion, die man vollständig testen kann.

**Konsequenz:** Jede Auswertung geht durch `DeriveIntervalsUseCase`. Diese Funktion ist die am gründlichsten getestete Stelle im Projekt.

### ADR-002 — Zahlenräder als Overlay-Activity, nicht im Widget {#a-4-2}

**Entscheidung:** Das Widget zeigt die Räder als Anzeige. Ein Tap auf ein Rad öffnet `WheelPickerActivity` – eine transparente, dialogartige Activity, die optisch wie das aufgeklappte Widget aussieht und beide Räder mit echtem Fling-Scrollen und Rastung (Compose `LazyColumn` + `rememberSnapFlingBehavior`) darstellt.

**Warum:** Siehe {#a-2-1}. RemoteViews liefern keine Wischgesten. Die Overlay-Variante gibt exakt das Bediengefühl des Aktenkoffer-Zahlenschlosses (Trägheit, Rastung, haptisches Feedback über `HapticFeedbackConstants.CLOCK_TICK` pro Raste) und ist die Lösung, die etablierte Widget-Apps ebenfalls verwenden.

**Kosten:** Ein zusätzlicher Tap. Er ist in den 3-Sekunden-Budget aus P1 enthalten (Tap Rad → flingen → Tap Sonne/Mond).

**Verworfene Alternative B (dokumentiert, als Fallback nutzbar):** Glance `LazyColumn` direkt im Widget, in dem jede Zeile ein Zeitwert ist. Das Scrollen wäre widget-nativ, aber die Auswahl bräuchte trotzdem einen Tap auf den Zielwert, das Rendering ist deutlich unruhiger und die Liste lässt sich nicht zuverlässig auf die aktuelle Zeit zurückscrollen. Nur umsetzen, wenn ADR-002 im Nutzertest durchfällt.

### ADR-003 — Live-Zeit über eingebettetes `TextClock` {#a-4-3}

**Entscheidung:** Im Zustand `LIVE` wird der zentrale Zeitwert der Räder über ein `TextClock` gerendert, eingebettet via Glance-`AndroidRemoteViews`. Im Zustand `HELD` wird stattdessen ein statischer Text mit dem gehaltenen Wert gerendert.

**Warum:** Das System aktualisiert `TextClock` selbst, minutengenau, ohne Alarm, ohne Wakelock, ohne Berechtigung. Siehe {#a-2-2}.

**Konsequenz:** Die Nachbarziffern ober- und unterhalb der Mitte sind im Zustand `LIVE` **dekorativ** (abgeblendete, angeschnittene Ziffernkanten mit Verlaufsmaske), weil ihre echten Werte nicht mitlaufen könnten. Im Zustand `HELD` und in `WheelPickerActivity` sind sie echt. Das ist visuell konsistent, weil dort ohnehin ein Zustandswechsel stattfindet.

### ADR-004 — Zeitspeicherung als UTC + Zonen-ID {#a-4-4}

**Entscheidung:** `occurredAtUtcMillis: Long` **und** `timeZoneId: String` (IANA, z. B. `Europe/Zurich`) werden gespeichert. Nie nur eines von beidem.

**Warum:** Ohne Zone lässt sich „Nacht vom 3. auf den 4." nach einem Zeitzonenwechsel nicht rekonstruieren; ohne UTC ist keine Dauer berechenbar. Sommerzeitumstellungen erzeugen Tage mit 23 bzw. 25 Stunden – die Auswertung muss das aushalten. Siehe Testfälle in {#a-11-2}.

### ADR-005 — Entitlement als eigener Domänenbegriff {#a-4-5}

**Entscheidung:** Die App fragt nie „hat gekauft?", sondern immer `EntitlementRepository.current(): Flow<Entitlement>`. Ein `Entitlement` hat eine Quelle (`FREE`, `SUBSCRIPTION`, `RESEARCH`) und ein Ablaufdatum.

**Warum:** Kauf und Forschungsteilnahme führen zur selben Funktionsfreischaltung über zwei völlig verschiedene Wege. Nur eine gemeinsame Abstraktion verhindert, dass sich Feature-Gates an zwanzig Stellen verzweigen.

### ADR-006 — Rechenintensive Auswertung in C++ {#a-4-6}

**Entscheidung:** Die Rhythmus-Analytik und die Aktogramm-Rasterisierung werden in C++ implementiert (`:core:native`, CMake, JNI-Fassade). Der Rest der App bleibt Kotlin.

**Warum:** Diese beiden Teile arbeiten auf minutengerasterten Vektoren, die mit der Aufzeichnungsdauer und der Zahl verglichener Personen wachsen. Sie laufen beim Scrollen und Zoomen, also im Frame-Budget. In Kotlin bedeutet das Autoboxing, Allokationsdruck und GC-Pausen genau dann, wenn die Hauptansicht flüssig sein muss. Nativer Code mit dicht gepackten primitiven Arrays hat dieses Problem nicht.

**Konsequenz:** Höhere Baukomplexität, größeres App-Paket, eine Klasse von Fehlern (Speicherfehler), die Kotlin nicht kennt. Deshalb die neun Regeln in {#a-3-4} – insbesondere die Pflicht zur Messung vor der Portierung, die Kotlin-Referenzimplementierung als Wahrheit und die Sanitizer in CI.

**Verworfene Alternative:** Alles in Kotlin und auf einen Hintergrund-Thread schieben. Löst das Ruckeln nicht, sondern verschiebt es in eine Ladeanzeige, und skaliert bei der Vergleichsansicht mehrerer Personen über Jahre nicht.

### ADR-007 — Forschungsmodus hinter einem lokalen Build-Schalter {#a-4-7}

**Entscheidung:** Der gesamte Forschungsmodus wird technisch fertig gebaut, aber hinter dem Schalter `FEATURE_RESEARCH_MODE` versteckt, der in allen Buildtypen auf `false` steht. Die Inhalte – Fragebogen, Aufklärungs- und Einwilligungstext, Upload-Endpunkt – bleiben bis zuletzt Platzhalter und werden erst in M11 ersetzt. Erst dann wird der Schalter umgelegt.

**Warum:** Die Technik dahinter ist reine Logik und vollständig baubar, ohne dass eine einzige der offenen Fragen aus {#a-15} beantwortet ist: Warteschlange, Wiederholungsstrategie, Anonymisierung, Widerruf, Freischaltungsdauer. Genau dieser Teil ist auch der fehleranfälligste und profitiert davon, früh und in Ruhe zu entstehen. Was fehlt, sind Texte und ein Endpunkt – und die kommen von Menschen, nicht vom Agenten.

Der zweite, wichtigere Grund ist datenschutzrechtlich: Solange der Schalter aus ist, erhebt die App nichts für die Forschung, speichert keine Einwilligung und registriert keinen Upload-Job. Das Data-Safety-Formular für v1.0 beschreibt deshalb wahrheitsgemäß **keine** Datenweitergabe. Ein halbfertiger Forschungsmodus, den man nur „nicht bewirbt", wäre in dieser Hinsicht eine Falle.

**Konsequenz:** Version 1.0 geht ohne sichtbaren Forschungsmodus in den Store. Die Freischaltung über Forschungsteilnahme kommt in einem Folge-Release (M11). Die kostenpflichtige Stufe (M7) ist davon unberührt und v1.0-fähig.

**Verworfene Alternative:** M8 komplett nach hinten schieben und gar nicht anfangen. Das häuft die schwierigste Logik des Projekts genau dort an, wo am wenigsten Zeit ist, und macht die Anonymisierung zu einer Last-Minute-Arbeit an Gesundheitsdaten.

---

## 5. Datenmodell {#a-5}

### 5.1 Entities {#a-5-1}

```kotlin
// :core:model

@JvmInline value class PersonId(val value: String)   // UUIDv4 als String

data class Person(
    val id: PersonId,
    val displayName: String,
    val colorSeed: Int,          // deterministische Farbe für Diagramme
    val sortIndex: Int,
    val createdAtUtcMillis: Long,
    val archivedAtUtcMillis: Long?,   // Soft-Delete: Daten bleiben, Person ist ausgeblendet
)

enum class SleepState { AWAKE, ASLEEP }

enum class EventSource { WIDGET, APP, CORRECTION, IMPORT }

data class StateEvent(
    val id: String,                    // UUIDv4
    val personId: PersonId,
    val state: SleepState,
    val occurredAtUtcMillis: Long,     // ADR-004
    val timeZoneId: String,            // ADR-004
    val source: EventSource,
    val recordedAtUtcMillis: Long,     // wann der Eintrag entstand (≠ occurredAt bei Nachträgen)
    val supersedesEventId: String?,    // bei Korrektur: Verweis auf das ersetzte Event
    val voidedAtUtcMillis: Long?,      // Soft-Delete, nie hartes DELETE (P2)
    val note: String?,
)
```

### 5.2 Room-Schema {#a-5-2}

Zwei Tabellen plus zwei Konfigurationstabellen.

```
persons(
  id TEXT PRIMARY KEY, display_name TEXT NOT NULL, color_seed INTEGER NOT NULL,
  sort_index INTEGER NOT NULL, created_at INTEGER NOT NULL, archived_at INTEGER
)

state_events(
  id TEXT PRIMARY KEY,
  person_id TEXT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
  state TEXT NOT NULL,                    -- 'AWAKE' | 'ASLEEP'
  occurred_at INTEGER NOT NULL,           -- UTC epoch millis
  time_zone_id TEXT NOT NULL,
  source TEXT NOT NULL,
  recorded_at INTEGER NOT NULL,
  supersedes_event_id TEXT,
  voided_at INTEGER,
  note TEXT
)
INDEX idx_events_person_time ON state_events(person_id, occurred_at)
INDEX idx_events_active     ON state_events(person_id, voided_at, occurred_at)

widget_bindings(
  app_widget_id INTEGER PRIMARY KEY,
  person_id TEXT NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
  created_at INTEGER NOT NULL
)

research_consents(
  id TEXT PRIMARY KEY, granted_at INTEGER NOT NULL, revoked_at INTEGER,
  survey_version TEXT NOT NULL, consent_text_hash TEXT NOT NULL,
  participant_pseudonym TEXT NOT NULL, valid_until INTEGER NOT NULL
)
```

**Migrationsregel:** Ab dem ersten Commit gilt `version = 1` und `exportSchema = true`. Schemadateien liegen unter `:core:database/schemas/` und werden **eingecheckt**. Jede Schemaänderung braucht eine `Migration`-Klasse und einen `MigrationTest`. Kein `fallbackToDestructiveMigration()` – auch nicht in Debug-Builds.

### 5.3 Ableitung der Intervalle {#a-5-3}

Der Kern der Domäne. Reine Funktion, kein Zustand, keine Android-API.

```kotlin
// :core:domain
class DeriveIntervalsUseCase(private val clock: Clock) {
    operator fun invoke(events: List<StateEvent>, windowEndUtc: Long): List<Interval>
}

data class Interval(
    val personId: PersonId,
    val state: SleepState,
    val startUtcMillis: Long,
    val endUtcMillis: Long,
    val isOpen: Boolean,       // endet an windowEnd, weil noch kein Folge-Event existiert
    val startZoneId: String,
    val endZoneId: String,
)
```

Regeln, die die Funktion einhalten muss:

1. Eingabe wird nach `occurred_at` aufsteigend sortiert; `voided_at != null` wird vorher gefiltert.
2. Ein Intervall reicht von einem Event bis zum **nächsten Event mit anderem Zustand**.
3. Zwei aufeinanderfolgende Events **gleichen** Zustands: das spätere wird ignoriert (Doppeltipp), das Intervall bleibt beim früheren Startzeitpunkt.
4. Das letzte Event erzeugt ein offenes Intervall bis `windowEndUtc` mit `isOpen = true`.
5. Events, die zeitlich **vor** dem vorherigen Event liegen (möglich durch manuelle Zeiteinstellung), werden eingeordnet, nicht abgelehnt. Die Sortierung nach `occurred_at` erledigt das automatisch.
6. Intervalle über eine Sommerzeitumstellung behalten ihre echte Dauer in Millisekunden. Die *kalendarische* Zuordnung passiert erst in der Darstellungsschicht.

### 5.4 Tagesgrenzen für die Darstellung {#a-5-4}

Für „letzte 7 Tage" und für das Aktogramm gilt eine konfigurierbare **Tagesgrenze** (Default: 18:00 Uhr lokal). Eine Nacht gehört zu dem Kalendertag, an dem sie *begonnen* hat. Ohne diese Regel wird jede Nacht auf zwei Balken zerschnitten und die Grafik unlesbar.

Implementierung in `:core:domain/DayBucketing.kt`, konfigurierbar in den Einstellungen, mit expliziten Tests für Umstellungstage.

---

## 6. Das Widget {#a-6}

Modul `:widget`. Das wichtigste und kniffligste Stück Software im Projekt.

### 6.1 Layout {#a-6-1}

```
┌─────────────────────────────────────┐
│ Lena                                │  ← Personenname, klein, oben links (PRD 1)
│                                     │
│      ╭────────╮   ╭────────╮        │
│      │   ~06~ │   │   ~41~ │        │  ← angeschnittene Nachbarziffer (dekorativ)
│      │┃  07  ┃│ : │┃  42  ┃│        │  ← Rastfenster, aktiver Wert
│      │   ~08~ │   │   ~43~ │        │
│      ╰────────╯   ╰────────╯        │
│         Stunde       Minute         │
│                                     │
│     ☀ Sonne          ☾ Mond         │  ← Aktions-Buttons
└─────────────────────────────────────┘
```

Größen: `targetCellWidth=4`, `targetCellHeight=2`, mit `SizeMode.Responsive` und mindestens drei Layoutvarianten (schmal / normal / breit). Bei zu geringer Höhe entfallen die Achsenbeschriftungen, nie die Buttons.

### 6.2 Widget-Zustandsautomat {#a-6-2}

Der Widget-Zustand liegt in Glance-`GlanceStateDefinition` (Proto-DataStore, pro `GlanceId`):

```kotlin
data class WidgetUiState(
    val personId: PersonId?,
    val personName: String,
    val timeMode: TimeMode,          // LIVE | HELD
    val heldEpochMinute: Long?,      // gesetzter Wert, minutengenau
    val holdExpiresAtUtcMillis: Long?,
    val lastState: SleepState?,      // null = noch nie erfasst
)
```

**Zeitmodus:**

```
        Tap auf Rad → WheelPickerActivity → Wert bestätigt
LIVE ─────────────────────────────────────────────────────► HELD
  ▲                                                           │
  │            60 s abgelaufen  ODER  Sonne/Mond gedrückt     │
  └───────────────────────────────────────────────────────────┘
```

- `HELD` dauert **exakt 60 Sekunden** ab Bestätigung (PRD 1).
- Umsetzung: `holdExpiresAtUtcMillis = now + 60_000` in den Zustand schreiben, dazu einen einmaligen `AlarmManager.set()` auf diesen Zeitpunkt, der einen `UpdateWidgetReceiver` weckt.
- **Zusätzlich** prüft jeder Widget-Render, ob `holdExpiresAtUtcMillis` in der Vergangenheit liegt, und fällt dann sofort auf `LIVE` zurück. Damit ist der Alarm nur eine Optimierung, keine Korrektheitsbedingung – wichtig, weil inexakte Alarme verzögert feuern dürfen.
- Nach dem Drücken von Sonne oder Mond wird der Hold sofort beendet (der Zweck ist erfüllt).

**Buttonzustände (PRD 1):**

| `lastState` | Sonne | Mond |
|---|---|---|
| `null` (frisch platziert) | aktiv | aktiv |
| `AWAKE` | ausgegraut | aktiv |
| `ASLEEP` | aktiv | ausgegraut |

`lastState` wird **aus der Datenbank abgeleitet** (letztes nicht-voidetes Event der Person), nicht im Widget-Zustand gehalten. Sonst laufen zwei Widgets derselben Person auseinander.

**Ausgegraut heißt nicht deaktiviert:** Der Button bleibt klickbar, führt aber zu einem kurzen Hinweis-Toast statt zu einem Event. Grund: ein toter Bereich im Widget fühlt sich kaputt an, und Doppel-Events werden ohnehin von Regel 3 in {#a-5-3} abgefangen.

### 6.3 Aktions-Flow beim Buttonklick {#a-6-3}

```
Klick auf ☀ / ☾
   │
   ├─ ActionCallback (Glance) → RecordStateEventUseCase
   │     · occurredAt = HELD ? heldEpochMinute : aktuelle Systemzeit (auf Minute gerundet)
   │     · timeZoneId = ZoneId.systemDefault().id
   │     · source     = EventSource.WIDGET
   │
   ├─ Hold beenden, timeMode = LIVE
   ├─ Haptik: HapticFeedbackConstants.CONFIRM
   └─ Alle Widgets dieser Person neu rendern (updateAll)
```

Die Verarbeitung läuft in einer Coroutine mit `goAsync()`-Semantik. Der Klick darf **nie** die App starten.

### 6.4 Konfiguration und Zero-State {#a-6-4}

`AppWidgetProviderInfo.configure = WidgetConfigActivity`.

```
Widget wird auf den Homescreen gezogen
   │
   ├─ WidgetConfigActivity startet, setzt sofort RESULT_CANCELED
   │
   ├─ Personen vorhanden?
   │     │
   │     ├─ NEIN → App-Onboarding starten (PersonSetupActivity, Modus FIRST_RUN)
   │     │          · Direkt im Namensfeld, Tastatur offen
   │     │          · Nach dem Speichern zurück zur Konfiguration
   │     │          · Bricht der Nutzer ab → RESULT_CANCELED bleibt → System entfernt das Widget
   │     │
   │     └─ JA  → Personenauswahl (Liste + "Neue Person")
   │
   └─ Auswahl bestätigt
         · widget_bindings-Zeile schreiben
         · Widget-Zustand initialisieren, erstes Rendern auslösen
         · setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, id))
         · finish()
```

Das erfüllt PRD 2 („öffnet sich automatisch die App und führt direkt in die Namenserfassung") exakt und ohne Sonderfall: die Konfigurations-Activity *ist* die App.

### 6.5 `WheelPickerActivity` {#a-6-5}

- Theme: `Theme.Circadia.Transparent` (`windowIsTranslucent`, `windowBackground=@android:color/transparent`, kein ActionBar, `windowIsFloating=false`).
- Inhalt: zwei `LazyColumn` (Stunden 0–23, Minuten 0–59), `rememberSnapFlingBehavior`, unendlich wirkend über eine große virtuelle Item-Anzahl mit Modulo-Abbildung.
- Initial gescrollt auf den aktuell angezeigten Wert.
- Pro einrastender Ziffer: `performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)`.
- Vertikale Verlaufsmaske ober- und unterhalb des Rastfensters, damit die Räder physisch wirken.
- Bestätigung: automatisch, sobald das Fling zur Ruhe kommt und 400 ms nichts passiert; zusätzlich ein "Fertig"-Bereich. Tippen außerhalb verwirft.
- Ergebnis: Zeit an das Widget übergeben (`appWidgetId` im Intent), Zustand auf `HELD` setzen, Activity beenden. Rückkehr auf den Homescreen ohne sichtbaren Aktivitätswechsel (`overridePendingTransition` mit Fade).

**Barrierefreiheit:** Die Räder brauchen `contentDescription` und funktionierende TalkBack-Inkremente (`SemanticsProperties` mit `setProgress`), sonst ist die Zeiteinstellung für blinde Nutzende unbedienbar. Zusätzlich ein Fallback-Modus: langes Drücken auf ein Rad öffnet einen normalen `TimePicker`.

---

## 7. In-App-Auswertung {#a-7}

Modul `:feature:timeline`.

### 7.1 Hauptansicht: Aktogramm {#a-7-1}

Das Aktogramm ist die Standarddarstellung der Schlafforschung: eine Zeile pro Tag, die Zeitachse horizontal, Schlafphasen als dunkle Balken.

- **Double-Plot** (Standard in der Chronobiologie): jede Zeile zeigt 48 Stunden, die zweite Tageshälfte wiederholt sich in der Folgezeile. Dadurch werden Verschiebungen des Schlafzeitpunkts als schräge Linie sichtbar statt am Rand abgeschnitten.
- Umschaltbar auf Single-Plot (24 h) für die kompakte Ansicht.
- Freie Version: 7 Zeilen. Bezahlt/Forschung: gesamter Zeitraum mit Zoom.
- Umsetzung in Compose `Canvas`, nicht mit einer Chart-Bibliothek – die Darstellung ist zu speziell und muss exakt kontrollierbar sein.

### 7.2 Vergleichsansicht mehrerer Personen {#a-7-2}

Zwei Modi, umschaltbar:

1. **Übereinandergelegt** (PRD 3): alle ausgewählten Personen im selben Aktogramm, je Person eine Farbe aus `colorSeed`, Balken halbtransparent mit `BlendMode.Multiply`, sodass Überlappungen dunkler erscheinen.
2. **Nebeneinander:** je Person eine eigene Spur, gleiche Zeitachse, synchronisiertes Scrollen und Zoomen.

Farbwahl folgt den Vorgaben aus dem `dataviz`-Skill: kategoriale Palette, in hell und dunkel geprüft, Unterscheidbarkeit auch bei Rot-Grün-Schwäche, zusätzlich Musterung (Schraffur) als redundanter Kanal.

### 7.3 Kennzahlen {#a-7-3}

| Kennzahl | Stufe | Definition | Quelle |
|---|---|---|---|
| Gesamtschlafzeit pro Tag | frei | Summe der `ASLEEP`-Intervalle im Tages-Bucket | – |
| Anzahl Schlafepisoden | frei | Anzahl `ASLEEP`-Intervalle | – |
| Längste zusammenhängende Wachphase | frei | max. `AWAKE`-Intervall | – |
| Einschlaf-/Aufwachzeit | frei | Start/Ende der Hauptschlafepisode | – |
| **Schlafmitte (Mid-Sleep)** | Pro | Mittelpunkt der Hauptschlafepisode | MCTQ, Roenneberg et al. |
| **MSF<sub>sc</sub> / Chronotyp** | Pro | Schlafmitte an freien Tagen, korrigiert um Schlafschuld | Roenneberg et al., *Curr Biol* 2004 / *Sleep Med Rev* 2007 |
| **Sozialer Jetlag** | Pro | \|Schlafmitte frei − Schlafmitte Arbeitstage\| | Wittmann et al., *Chronobiol Int* 2006 |
| **Sleep Regularity Index (SRI)** | Pro | Wahrscheinlichkeit, im 24-h-Abstand im selben Zustand zu sein, skaliert 0–100 | Phillips et al., *Sci Rep* 2017 |
| **Interdaily Stability (IS)** | Pro | Kopplung des Rhythmus an den 24-h-Takt | Van Someren et al. |
| **Intradaily Variability (IV)** | Pro | Fragmentierung des Rhythmus | Van Someren et al. |
| Schlaf-Wach-Verhältnis pro Woche | Pro | – | – |
| Trendlinien und gleitende Mittel (7/28 Tage) | Pro | – | – |

**Wichtige Ehrlichkeit gegenüber Nutzenden:** Diese Kennzahlen sind für Aktigraphie- oder Tagebuchdaten definiert. Unsere Daten sind selbstberichtete Zeitpunkte – gröber und lückenhafter. Jede Pro-Kennzahl bekommt einen ausklappbaren Hinweis mit Definition, Quelle und Genauigkeitsvorbehalt. Es werden **keine** medizinischen Aussagen oder Diagnosen formuliert, und keine Empfehlungen, die als Behandlung gelesen werden könnten. Siehe auch {#a-9-3}.

Jede Kennzahl braucht eine Mindestdatenmenge (SRI z. B. ≥ 7 vollständige Tage). Darunter wird sie mit dem Hinweis „zu wenig Daten" ausgegraut, nie geschätzt.

### 7.4 Datenkorrektur {#a-7-4}

Antippen eines Balkens öffnet ein Detailblatt: Zeitpunkt ändern, Eintrag als ungültig markieren, Notiz hinzufügen. Jede Änderung erzeugt ein neues `StateEvent` mit `source = CORRECTION` und `supersedesEventId` (P2). Ein Verlaufsbereich zeigt die Historie eines Eintrags.

---

## 8. Monetarisierung {#a-8}

### 8.1 Abo {#a-8-1}

- Play Billing Library **9.x** (Pflicht ab 31.08.2026 mindestens v8).
- Produkt: Subscription `circadia_pro`, Base Plan `yearly-chf`, Preis **CHF 5.00**, Auto-Renew.
- `queryPurchasesAsync` beim App-Start und beim Wiederaufnehmen; `acknowledgePurchase` innerhalb von 3 Tagen, sonst wird die Zahlung zurückerstattet.
- Kein eigener Server nötig: lokale Verifikation über den Play-Cache genügt für diese Preisklasse. `PurchasesUpdatedListener` schreibt in `EntitlementRepository`.
- Kulanz-Fenster: Ist Play nicht erreichbar, bleibt ein zuvor gültiges Entitlement noch 7 Tage aktiv (`lastVerifiedAt`). Danach Rückfall auf `FREE` – ohne Datenverlust, nur mit eingeschränkter Ansicht.

### 8.2 Freischaltung durch Forschungsteilnahme {#a-8-2}

Der zweite Weg zur Vollversion. Vier Schritte, alle abbrechbar. **Der gesamte Abschnitt liegt hinter dem Schalter aus {#a-8-4} und ist in Version 1.0 nicht sichtbar** (ADR-007).

```
1. Aufklärung      → Wer forscht, wozu, welche Daten, wie lange, wie widerrufbar.
                     Volltext, nicht nur eine Checkbox.
2. Umfrage         → In-App-Fragebogen (versioniert, JSON-definiert in assets/surveys/).
                     Typische Felder: Alter (Spanne), Geschlecht (freiwillig),
                     Arbeitszeitmodell, bekannte Schlafstörungen (freiwillig).
3. Einwilligung    → Getrennte Zustimmung: (a) Umfrageantworten, (b) Schlaf-Wach-Daten.
                     Beide einzeln erteilbar und einzeln widerrufbar.
                     Gespeichert wird: Zeitpunkt, Textversion, Hash des Einwilligungstexts.
4. Freischaltung   → Entitlement RESEARCH, gültig 365 Tage ab Zustimmung.
                     Läuft ab → Erinnerung 14 Tage vorher, Verlängerung durch neue Umfrage.
```

**Anonymisierung** (verpflichtend, vor jedem Upload):

- Kein Personenname, keine `PersonId`, keine Geräte-ID, keine Account-Kennung.
- Stattdessen ein zufälliges `participant_pseudonym` (UUIDv4), das nur lokal einer Person zugeordnet ist.
- Zeitstempel auf 5 Minuten gerundet, Datumsangaben nur als Wochentag + Tagesoffset relativ zum Studienbeginn, Zeitzone nur als UTC-Offset-Gruppe.
- Upload über WorkManager, gebündelt, verschlüsselt (TLS), mit Wiederholungsstrategie.
- **Widerruf** löscht das lokale Pseudonym und stößt eine Löschanfrage beim Endpunkt an. Die App muss den Widerruf auch dann anbieten, wenn kein Netz da ist (Queue).

**Play-Konformität:** Die Gratis-Freischaltung ist kein Kauf, umgeht also keine Billing-Pflicht. Sie darf aber nicht als „Bezahlung mit Daten" beworben werden und muss echt freiwillig sein – die kostenlose Basisversion inklusive Vollexport bleibt ohne jede Einwilligung nutzbar.

### 8.3 Feature-Gates {#a-8-3}

Genau eine Stelle entscheidet:

```kotlin
// :core:domain
enum class Feature { UNLIMITED_HISTORY, ADVANCED_ANALYTICS }

class FeatureAccess(private val entitlements: EntitlementRepository) {
    fun has(feature: Feature): Flow<Boolean>
}
```

Freie Version explizit: 7-Tage-Sicht, **vollständiger Rohdatenexport** (PRD 3 – der Export ist bewusst *nicht* eingeschränkt), beliebig viele Personen, alle Widgets.

### 8.4 Feature-Schalter {#a-8-4}

Ein einziger Mechanismus, im Build fest verdrahtet. Kein Server, keine Fernsteuerung, keine zusätzliche Abhängigkeit.

```kotlin
// :core:common
object FeatureFlags {
    val researchMode: Boolean = BuildConfig.FEATURE_RESEARCH_MODE
}
```

Der Wert kommt als `buildConfigField` aus Gradle. Standard in **allen** Buildtypen: `false`. Nur der `debug`-Build lässt sich über eine Gradle-Property übersteuern:

```
./gradlew assembleDebug -PresearchMode=true
```

**Regeln:**

1. **Ein Schalter ist ein Zweig, kein Kommentar.** Der Code dahinter wird kompiliert, gelintet und getestet. Er wird nur nicht erreicht. Auskommentierter oder gelöschter Code ist kein Feature-Schalter.
2. **Kein toter Einstiegspunkt.** Ist der Schalter aus, wird der Menüeintrag „Forschung" und der Verweis darauf im Abo-Screen **gar nicht gerendert** – nicht ausgegraut, nicht mit Hinweis. Ein sichtbarer, nicht benutzbarer Einstieg erzeugt Supportanfragen und Rückfragen im Play-Review.
3. **Keine Nebenwirkungen bei ausgeschaltetem Schalter.** Der Upload-Worker wird nicht registriert, keine Einwilligung ist speicherbar, kein Pseudonym wird erzeugt, keine Umfrage-Assets werden geladen. Prüfbar über Testfall 34 in {#a-11-2}.
4. **Beide Zweige werden getestet.** Die Testkonfiguration setzt den Schalter explizit, statt sich auf den Standardwert zu verlassen. Jede Verzweigung hat einen Test für „an" und einen für „aus".
5. **Schalter haben ein Verfallsdatum.** Zu jedem Schalter gehört ein Task, der ihn wieder entfernt. Steht ein Schalter länger als zwei Releases, kommt er auf die Tagesordnung. `FEATURE_RESEARCH_MODE` verschwindet mit T-1105.
6. **Was nicht hinter dem Schalter steht:** „Alle meine Daten löschen" ({#a-9-1}) ist unabhängig vom Forschungsmodus Pflicht. Die Freischaltungsquelle `RESEARCH` bleibt im Datenmodell und in `FeatureAccess` – sie kann nur nicht entstehen. So bleibt die Domäne stabil, wenn der Schalter fällt.

### 8.5 Platzhalter-Inhalte und die Sicherung dagegen {#a-8-5}

Damit der Forschungsmodus ohne die Antworten auf Q1, Q2, Q3 und Q6 baubar bleibt, arbeitet er gegen Platzhalter:

| Was | Platzhalter | Ersetzt in |
|---|---|---|
| Fragebogen | `assets/surveys/survey-draft-0.json`, generische Fragen, Feld `"status": "draft"` | T-1101 |
| Aufklärungs- und Einwilligungstext | `res/raw/consent_draft_0.md`, Feld `status: draft` im Kopf | T-1102 |
| Upload-Ziel | `NoopUploadEndpoint` – die Warteschlange füllt sich, es geht nichts hinaus | T-1103 |

Der Umfrage-Motor ist **inhaltsagnostisch**: er rendert, was im JSON steht. Das Austauschen des Fragebogens ist damit eine Datei, kein Codeumbau.

> **Sicherung (Testfall 35):** Ein Test schlägt fehl, sobald `FEATURE_RESEARCH_MODE = true` ist **und** ein geladener Inhalt noch `status: draft` trägt oder der Endpunkt noch `NoopUploadEndpoint` ist. Damit kann ein Platzhalter-Einwilligungstext nicht versehentlich in einen Produktivbuild geraten. Das ist die wichtigste einzelne Absicherung im gesamten Forschungsteil – ein Platzhaltertext, unter dem jemand echte Gesundheitsdaten freigibt, wäre keine gültige Einwilligung.


---

## 9. Datenschutz, Recht, Play-Anforderungen {#a-9}

### 9.1 Rechtlicher Rahmen {#a-9-1}

Schweizer Anbieter, europäische Nutzende: **revDSG** (CH) und **DSGVO** (EU) gelten parallel. Schlafdaten sind besonders schützenswerte Personendaten bzw. Gesundheitsdaten nach Art. 9 DSGVO.

Notwendig:

- Datenschutzerklärung, in der App verlinkt und im Play-Store-Eintrag hinterlegt.
- Rechtsgrundlage für den Forschungs-Upload: **ausdrückliche Einwilligung** (Art. 9 Abs. 2 lit. a DSGVO). Keine andere Grundlage ist hier tragfähig.
- Auskunft, Berichtigung, Löschung, Datenübertragbarkeit – der Export deckt Übertragbarkeit ab, Löschung braucht einen expliziten Menüpunkt „Alle Daten löschen".
- Verzeichnis von Verarbeitungstätigkeiten; bei Weitergabe an Forschende ein Auftragsverarbeitungs- oder Datenweitergabevertrag.
- Eine Datenschutz-Folgenabschätzung (DSFA) ist bei systematischer Verarbeitung von Gesundheitsdaten sehr wahrscheinlich erforderlich.

> Der Agent implementiert die technischen Voraussetzungen. Die juristische Prüfung der Texte ist **kein** Agenten-Task und wird als offener Punkt in `.ai_state.json` unter `openQuestions` geführt. Dies ist keine Rechtsberatung.

### 9.2 Play-Store-Pflichten {#a-9-2}

- **Data Safety**-Formular vollständig: Erhebung von Gesundheitsdaten, optionale Weitergabe, Verschlüsselung bei Übertragung, Löschmöglichkeit.
- Health-Apps-Policy prüfen: Die App darf keine Diagnose stellen und nichts Medizinisches behaupten.
- Abo-Regeln: Preis, Laufzeit und Kündigungsweg müssen **vor** dem Kauf sichtbar sein.
- Berechtigungen: minimal. `POST_NOTIFICATIONS` nur, wenn Erinnerungen implementiert werden; `INTERNET` nur wegen Billing/Upload; **keine** Exact-Alarm-Berechtigung ({#a-2-2}).

### 9.3 Formulierungen in der App {#a-9-3}

Verboten: „Ihre Schlafqualität ist schlecht", „Sie leiden vermutlich an …", „empfohlene Schlafdauer für Sie".
Erlaubt: „Ihre Schlafmitte lag diese Woche im Mittel um 03:47." und beschreibende Vergleiche zur eigenen Historie.

---

## 10. Internationalisierung {#a-10}

Von Tag 1, nicht nachträglich. Der Agent darf keinen nutzersichtbaren String im Code hinterlassen.

- Standardsprache: **Deutsch** in `res/values/strings.xml`. Kein `values-de`, sonst gibt es zwei Wahrheiten.
- Vorbereitet: `values-en`, `values-fr`, `values-it` als Ordner mit identischem Key-Satz (zu Beginn identisch mit dem deutschen Inhalt, mit `<!-- TODO:i18n -->` markiert).
- Namenskonvention der Keys: `bereich_element_bedeutung`, z. B. `widget_button_sun_label`, `paywall_price_per_year`, `analysis_metric_sri_title`.
- Jeder Key bekommt einen Übersetzerkommentar, wenn der Kontext ohne ihn unklar ist. Beispiel: `<!-- Kurzform, max. 12 Zeichen, steht im Widget -->`.
- Pluralformen über `<plurals>`, nie über Stringkonkatenation. Zahlenformatierung über `NumberFormat`, Datum/Zeit über `java.time.format.DateTimeFormatter.ofLocalizedTime(...)` mit `Locale`.
- **12h/24h:** Nie hart kodieren. `DateFormat.is24HourFormat(context)` respektieren – im Widget wie in der App. Die Räder haben im 12h-Modus ein zusätzliches AM/PM-Element.
- Erster Wochentag und Wochenberechnung über `WeekFields.of(locale)`.
- RTL: `start`/`end` statt `left`/`right`, `supportsRtl="true"`. Das Aktogramm bleibt in RTL zeitlich links→rechts (Zeit ist keine Leserichtung), aber Beschriftungen spiegeln.
- Lint-Regel `HardcodedText` auf `error` hochsetzen; Build bricht ab.

**Für die Übersetzung dieses Dokuments:** Anker-IDs, Codeblöcke, Dateipfade, Gradle-Koordinaten und die Bezeichner-Spalten aller Tabellen bleiben unverändert.

---

## 11. Qualitätssicherung {#a-11}

### 11.1 Verifikations-Gate {#a-11-1}

Nach **jedem** Task, ohne Ausnahme:

```bash
./gradlew ktlintCheck detekt lint testDebugUnitTest
```

Bei Änderungen hinter einem Feature-Schalter zusätzlich beide Zustände:

```bash
./gradlew testDebugUnitTest                       # Schalter aus (Standard)
./gradlew testDebugUnitTest -PresearchMode=true   # Schalter an
```

Bei Änderungen am nativen Kern zusätzlich:

```bash
./gradlew nativeHostTest        # GoogleTest auf dem Host, ohne Gerät
./gradlew assembleDebug         # mit ASan/UBSan
./tools/check_so_alignment.sh   # 16-KB-Seitenausrichtung aller .so
```

Bei UI-Änderungen zusätzlich:

```bash
./gradlew verifyRoborazziDebug connectedDebugAndroidTest
```

Ist auch nur eines rot, ist der Task nicht erledigt. Der Agent setzt `.ai_state.json.currentTask.status = "blocked"` und beschreibt das Problem, statt Tests abzuschalten oder `@Ignore` zu setzen.

### 11.2 Pflicht-Testfälle {#a-11-2}

Diese 36 Fälle müssen als benannte Tests existieren. Sie sind aus Erfahrung die Stellen, an denen Schlaf-Tracker kaputtgehen.

**Intervall-Ableitung (`DeriveIntervalsUseCaseTest`):**

1. Leere Eventliste → leeres Ergebnis.
2. Ein einziges Event → ein offenes Intervall bis `windowEnd`.
3. Zwei gleiche Zustände hintereinander → ein Intervall, späteres Event ignoriert.
4. Event mit `occurredAt` **vor** dem vorherigen Event → korrekt einsortiert.
5. Event mit `voidedAt != null` → vollständig ignoriert.
6. Korrektur-Event mit `supersedesEventId` → ersetzt, Original bleibt in der DB.
7. Intervall über Mitternacht → nicht zerschnitten.
8. Intervall über die Umstellung auf Sommerzeit (Nacht mit 23 h) → Dauer korrekt.
9. Intervall über die Umstellung auf Winterzeit (Nacht mit 25 h) → Dauer korrekt.
10. Reise über Zeitzonen zwischen zwei Events → Dauer korrekt, Zonen unterschiedlich.
11. Zwei Events in derselben Minute → deterministische Reihenfolge über `recordedAt`.

**Widget-Zustand (`WidgetStateTest`):**

12. Frisch platziert → beide Buttons aktiv.
13. Nach Sonne → Sonne ausgegraut, Mond aktiv.
14. Hold läuft nach exakt 60 s ab.
15. Hold-Alarm feuert verspätet (simulierte 90 s) → Render fällt trotzdem korrekt auf `LIVE`.
16. Zwei Widgets derselben Person → beide zeigen nach einer Aktion denselben Buttonzustand.
17. Zugeordnete Person wird gelöscht → Widget zeigt Fehlerzustand mit „neu zuordnen", kein Absturz.

**Konfiguration (`WidgetConfigActivityTest`):**

18. Keine Person vorhanden → Onboarding startet.
19. Onboarding abgebrochen → `RESULT_CANCELED`, Widget wird nicht platziert.
20. Person angelegt → `RESULT_OK` mit korrekter `appWidgetId`.

**Entitlement (`FeatureAccessTest`):**

21. `FREE` → 7-Tage-Grenze greift, Export voll verfügbar.
22. `SUBSCRIPTION` aktiv → unbegrenzt.
23. `RESEARCH` gültig → unbegrenzt; nach 365 Tagen → zurück auf `FREE`, keine Daten verloren.
24. Play nicht erreichbar, letzte Prüfung vor 3 Tagen → Entitlement bleibt.
25. Play nicht erreichbar, letzte Prüfung vor 10 Tagen → Rückfall auf `FREE`.

**Export (`ExportTest`):**

26. CSV mit Sonderzeichen und Semikolon im Personennamen → korrekt maskiert.
27. Export bei 0 Events → gültige Datei mit Kopfzeile.
28. Roundtrip JSON-Export → Import ergibt identische Events.

**Feature-Schalter (`ResearchFlagTest`):**

33. Schalter aus → kein Einstiegspunkt in der UI sichtbar (Compose-Test auf Menü und Abo-Screen).
34. Schalter aus → Upload-Worker nicht registriert, keine Einwilligung speicherbar, kein Pseudonym erzeugt.
35. Schalter an **und** Inhalt trägt `status: draft` oder Endpunkt ist `NoopUploadEndpoint` → Test schlägt fehl. Siehe {#a-8-5}.
36. Schalter an → vollständiger Ablauf bis in die Warteschlange gegen `NoopUploadEndpoint`, ohne Netzwerk.

**Nativer Kern (`NativeParityTest`, GoogleTest + JVM):**

29. SRI: native und Kotlin-Referenzimplementierung liefern auf denselben Eingaben identische Werte (Toleranz 1e-9).
30. IS und IV: dasselbe für zufällig erzeugte Eingaben, 1000 Durchläufe mit festem Seed.
31. Leerer und einelementiger Eingabevektor → kein Absturz, definierter Rückgabewert.
32. `UnsatisfiedLinkError` beim Laden → App fällt auf die Kotlin-Referenzimplementierung zurück, Ergebnis bleibt korrekt.

**Datenbank:** Migrationstest für jede Schemaversion (ab der zweiten).

### 11.3 Manuelle Prüfliste vor jedem Release {#a-11-3}

- Widget auf Pixel Launcher, Samsung One UI und Nova Launcher platzieren.
- Gerät in den Dunkelmodus schalten – Widget und App prüfen.
- Systemschrift auf größte Stufe – Widget darf nichts abschneiden.
- Flugmodus – App voll benutzbar außer Kauf.
- Gerätesprache auf Englisch – keine deutschen Reste sichtbar.
- Zeitformat auf 12 h umstellen – Räder korrekt.
- Gerätedatum manuell über eine Zeitumstellung schieben – Aktogramm bleibt korrekt.

---

## 12. Meilensteine {#a-12}

Die ausführbare Aufschlüsselung steht in `TASKS.md`. Hier nur die Reihenfolge und die Abnahmekriterien.

| # | Meilenstein | Abnahmekriterium |
|---|---|---|
| M0 | **Gerüst** | Multi-Modul-Projekt baut, CI grün, Version Catalog, Convention-Plugins, leere App startet. |
| M1 | **Datenkern** | Room-Schema v1, DAOs, Repositories, `DeriveIntervalsUseCase` mit allen Tests aus {#a-11-2} 1–11. |
| M2 | **Personen & Onboarding** | Personen anlegen/bearbeiten/archivieren in der App. Tests 18–20 vorbereitet. |
| M3 | **Widget (Basis)** | Widget platzierbar, Konfiguration mit Zero-State, Zeitanzeige `LIVE`, Sonne/Mond schreiben Events, Buttonlogik. Tests 12–13, 16–20. |
| M4 | **Widget (Räder)** | `WheelPickerActivity`, `HELD`-Modus, 60-s-Regel, Haptik, Barrierefreiheit. Tests 14–15. |
| M5 | **Auswertung** | Aktogramm single/double plot, 7-Tage-Sicht, Vergleichsansicht, Korrektur-Detailblatt. |
| M6 | **Export** | CSV und JSON über `ACTION_CREATE_DOCUMENT`. Tests 26–28. |
| M7 | **Monetarisierung** | Billing 9.x, Abo-Flow, `FeatureAccess`, Paywall-Screen. Tests 21–25. |
| M8 | **Forschungsmodus: Technik & Logik** | Umfrage-Motor, gestufte Einwilligung, Anonymisierung, Warteschlange, Widerruf – vollständig gebaut und getestet, hinter `FEATURE_RESEARCH_MODE = false`, gegen Platzhalter-Inhalte. Nichts davon ist in der App sichtbar. |
| M9 | **Pro-Analytik** | Schlafmitte, MSF<sub>sc</sub>, sozialer Jetlag, SRI, IS/IV – je mit Quelle und Mindestdatenprüfung. |
| M10 | **Release-Härtung v1.0** | i18n-Vollständigkeit, Prüfliste {#a-11-3}, Data-Safety-Formular (**ohne** Datenweitergabe), Store-Assets, R8-Regeln. Release geht mit ausgeschaltetem Forschungsmodus. |
| M11 | **Forschungsmodus: Inhalte & Scharfschaltung** | Endgültiger Fragebogen, juristisch freigegebene Texte, echter Endpunkt, Data-Safety aktualisiert, Schalter an, Schalter entfernt. Folge-Release v1.1. |

**M8 und M11 sind bewusst getrennt.** Die Logik des Forschungsmodus – Anonymisierung, Warteschlange, Widerruf – ist der heikelste Code im Projekt und entsteht früh, in Ruhe und vollständig testbar gegen Platzhalter. Was fehlt, sind Texte und ein Endpunkt, und die kommen von Menschen. Bis dahin ist nichts davon sichtbar oder aktiv (ADR-007, {#a-4-7}).

**M3 und M4 sind bewusst getrennt.** Ein funktionierendes Widget mit Live-Zeit ist bereits ein benutzbares Produkt. Die Räder sind eine Verbesserung, kein Blocker.

---

## 13. Glossar {#a-13}

Die Spalte „Bezeichner" wird bei Übersetzungen nicht verändert.

| Begriff (de) | Bezeichner | Bedeutung |
|---|---|---|
| Zustands-Event | `StateEvent` | Einzelner erfasster Zeitpunkt mit Zustand `AWAKE`/`ASLEEP`. |
| Intervall | `Interval` | Abgeleitete Zeitspanne zwischen zwei Events. Nie gespeichert. |
| Halten / Haltezustand | `HELD` | Widget zeigt 60 s lang eine manuell gesetzte Zeit. |
| Live-Zustand | `LIVE` | Widget folgt der Systemzeit. |
| Zahlenrad | `Wheel` | Scrollbares Ziffernrad für Stunde bzw. Minute. |
| Rastfenster | `SnapWindow` | Der hervorgehobene mittlere Bereich eines Rads. |
| Berechtigung / Freischaltung | `Entitlement` | Zugriffsstufe: `FREE`, `SUBSCRIPTION`, `RESEARCH`. |
| Aktogramm | `Actogram` | Tageszeilen-Diagramm der Schlafforschung. |
| Tagesgrenze | `DayBoundary` | Uhrzeit, ab der ein neuer Auswertungstag beginnt (Default 18:00). |
| Pseudonym | `participant_pseudonym` | Zufällige Kennung für den Forschungs-Upload. |

---

## 14. Bewusst nicht im Umfang {#a-14}

Damit der Agent nicht abschweift:

- Keine automatische Schlaferkennung über Sensoren, Mikrofon oder Bewegung.
- Keine Cloud-Synchronisation zwischen Geräten (M-Version später, braucht eigenen ADR).
- Keine Wear-OS-App, keine iOS-Version.
- Keine Health-Connect-Anbindung in v1 (naheliegende v2-Erweiterung, dann mit eigener Berechtigungsprüfung).
- Keine Erinnerungs-Benachrichtigungen in v1.
- Kein Login, kein Nutzerkonto.
- Kein sichtbarer Forschungsmodus in Version 1.0. Er wird in M8 gebaut, bleibt aber ausgeschaltet und kommt mit M11 in v1.1 ({#a-4-7}).

---

## 15. Offene Punkte für den Menschen {#a-15}

Diese Fragen kann der Agent nicht beantworten. Sie gehören in `.ai_state.json` unter `openQuestions` und blockieren die genannten Meilensteine. **Keine davon blockiert M8** – der Forschungsmodus wird technisch fertig gebaut, bevor eine davon beantwortet ist (ADR-007, {#a-4-7}).

| # | Frage | Blockiert |
|---|---|---|
| Q1 | Wer ist die verantwortliche Stelle für die Forschungsdaten, und existiert ein Ethikvotum? | M11 |
| Q2 | Wohin gehen die Forschungsdaten technisch (Endpunkt, Betreiber, Standort)? | M11 |
| Q3 | Wer verfasst und prüft Datenschutzerklärung und Einwilligungstext? | M11 (Datenschutzerklärung für v1.0 schon in M10) |
| Q4 | Google-Play-Entwicklerkonto vorhanden, Abo-Produkt angelegt, Auszahlungsprofil eingerichtet? | M7 |
| Q5 | Finaler App-Name und Paket-ID (Vorschlag: `ch.circadia.tracker`) – vor dem ersten Store-Upload unveränderlich. | M0 |
| Q6 | Umfang und Fragen des Forschungsfragebogens. | M11 |
| Q7 | Soll die Tagesgrenze ({#a-5-4}) für Nutzende einstellbar sein oder fest bei 18:00 bleiben? | M5 |
