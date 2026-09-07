---
doc_id: geminirules_addendum
project: circadia
lang: de
version: 1.0.0
purpose: Inhalte, die in die bestehende .geminirules übernommen werden
translatable: true
translation_notes: Codeblöcke, Pfade, Befehle und Bezeichner nicht übersetzen.
---

# Ergänzungen für `.geminirules` {#g-0}

> **Hinweis:** Für dieses Projekt gibt es bereits eine zusammengeführte, einbaufertige Datei: `agent/geminirules-merged.txt`. Sie enthält diese Regeln **plus** den bestehenden Windows-/PowerShell- und GitHub-Workflow und ersetzt die vorhandene `.geminirules` vollständig. Dieses Dokument hier ist die Referenzfassung der reinen Projektregeln — nützlich, wenn der Zielworkflow ein anderer ist.

Diese Regeln sind projektspezifisch und ergänzen die allgemeinen Konventionen; sie ersetzen sie nicht.

---

## 1. Arbeitszyklus {#g-1}

```
1. .geminirules lesen
2. .ai_state.json lesen
3. Nächsten Task aus TASKS.md bestimmen (Reihenfolge + blocked_by beachten)
4. Verwiesenen Abschnitt in BUILD_PLAN.md lesen (Anker-ID im Task)
5. Implementieren
6. Tests schreiben
7. ./gradlew ktlintCheck detekt lint testDebugUnitTest
8. Bei Rot: reparieren. Nie Tests abschalten.
9. .ai_state.json aktualisieren
10. Committen
11. Zurück zu 3
```

**Genau ein Task gleichzeitig `in_progress`.** Kein Vorgriff, keine Sammelcommits über mehrere Tasks.

---

## 2. Verbote {#g-2}

Diese Aktionen sind untersagt. Wenn eine davon nötig erscheint, ist das ein Blocker – Task auf `blocked` setzen, Begründung in `.ai_state.json`, aufhören.

| # | Verbot |
|---|---|
| V1 | `@Ignore`, `@Disabled` oder auskommentierte Tests. |
| V2 | Lint- oder detekt-Regeln herabstufen oder unterdrücken (`@Suppress`), um einen Build grün zu bekommen. |
| V3 | `fallbackToDestructiveMigration()` – auch nicht in Debug. |
| V4 | Nutzersichtbare Zeichenketten im Kotlin- oder XML-Layoutcode. Immer `strings.xml`. |
| V5 | `Dispatchers.IO` / `Dispatchers.Default` direkt verwenden. Immer den injizierten `DispatcherProvider`. |
| V6 | `System.currentTimeMillis()`, `LocalDateTime.now()`, `Instant.now()` direkt. Immer die injizierte `Clock`-Abstraktion. Sonst sind Zeit-Tests unmöglich. |
| V7 | Neue Abhängigkeiten hinzufügen, ohne sie im Version Catalog zu pinnen und in `.ai_state.json` unter `decisions` zu begründen. |
| V8 | Modulabhängigkeiten entgegen der Tabelle in `BUILD_PLAN.md {#a-3-2}`. |
| V9 | Berechtigungen im Manifest ergänzen, die nicht in `{#a-9-2}` genannt sind – insbesondere `SCHEDULE_EXACT_ALARM` und `USE_EXACT_ALARM`. |
| V10 | Preise, Produkt-IDs oder Store-Texte hart kodieren. Preise kommen immer aus `ProductDetails`. |
| V11 | Von einem ADR aus `{#a-4}` abweichen, ohne einen neuen ADR-Eintrag zu schreiben. |
| V12 | Personendaten, Namen oder Zeitstempel in Logs schreiben. Logging ist in Release-Builds vollständig entfernt. |
| V13 | `git push --force`, Rebase auf geteilten Branches, Commits verwerfen. |
| V14 | Kotlin-Code nach C++ portieren, ohne dass ein Benchmark vorher belegt, dass er das Budget reißt. Optimierung ohne Messung ist verboten. |
| V15 | Im nativen Kern: rohe `new`/`delete`, Zeiger-plus-Länge statt `std::span`, abgeschaltete Warnungen, deaktivierte Sanitizer, oder JNI-Aufrufe in einer Schleife über Datenpunkte. |
| V16 | Die Kotlin-Referenzimplementierung einer nativen Funktion löschen. Sie ist die Wahrheit, gegen die die schnelle Fassung getestet wird, und der Fallback bei `UnsatisfiedLinkError`. |
| V17 | Den Standardwert eines Feature-Schalters ändern. `FEATURE_RESEARCH_MODE` bleibt `false`, bis der Task, der ihn scharfschaltet (T-1105), an der Reihe ist. Zum Testen dient `-PresearchMode=true`, nicht der Standardwert. |
| V18 | Einen Platzhalter-Inhalt (`status: draft`) als `final` markieren, ohne dass der zugehörige Task aus M11 an der Reihe ist und die Freigabe vorliegt. |
| V19 | Die Platzhalter-Sicherung (Pflichttestfall 35) abschwächen, umgehen oder überspringen. Sie verhindert, dass jemand unter einem Platzhaltertext echte Gesundheitsdaten freigibt. |
| V20 | Einen Feature-Schalter durch eine Konstante, einen Kommentar oder gelöschten Code ersetzen. Ein Schalter ist ein Zweig, der gebaut, gelintet und getestet wird. |
| V21 | Generierte Dateien einchecken (`build/`, `.gradle/`) – Ausnahme: Room-Schemadateien unter `:core:database/schemas/`, die **müssen** eingecheckt werden. |

---

## 3. Codekonventionen {#g-3}

- Kotlin Coding Conventions, ktlint als Autorität. Zeilenlänge 120.
- Öffentliche API jedes Moduls mit KDoc; interne Hilfsklassen brauchen keinen.
- Compose: zustandslose Composables (`@Composable fun Foo(state, onEvent)`), Zustand im ViewModel. Jedes Composable mit `@Preview` in hell und dunkel.
- ViewModels geben genau ein `StateFlow<UiState>` aus; Ereignisse gehen als versiegelte Klasse hinein.
- Keine `!!`. Keine `lateinit` außerhalb von Tests. Kein `runBlocking` in Produktivcode.
- Fehler werden als Zustand modelliert, nicht als geworfene Ausnahme über Schichtgrenzen.
- Ein `sealed interface` pro UI-Zustand mit den Fällen `Loading`, `Empty`, `Content`, `Error` – alle vier müssen in der UI behandelt sein.
- Datei- und Klassennamen englisch. Kommentare und KDoc deutsch. Nutzersichtbare Texte über Ressourcen ({#a-10}).

---

## 4. Testregeln {#g-4}

- Testnamen als Sätze in Backticks: ``fun `Intervall über Sommerzeitumstellung behält echte Dauer`()``.
- Ein Testfall prüft eine Sache.
- Zeit in Tests **immer** über `FakeClock`. Kein `Thread.sleep`.
- Zeitzonen in Tests explizit setzen, nie die Standardzone des Testgeräts annehmen.
- Datenbanktests gegen In-Memory-Room, nicht gegen Mocks.
- Für jede Fehlerbehebung zuerst der Test, der den Fehler reproduziert, dann die Behebung.
- Zielabdeckung: `:core:domain` ≥ 90 %, alles andere ≥ 60 %. Abdeckung ist kein Ersatz für die Pflichtfälle in `{#a-11-2}`.

---

## 4a. Regeln für nativen Code {#g-4a}

Performance-kritische Komponenten werden in C++ gebaut (`:core:native`, ADR-006). Dafür gilt zusätzlich:

- **Erst messen.** Ohne Benchmark-Beleg keine Portierung. Die Messung gehört in die Commit-Nachricht und in `.ai_state.json`.
- **C++20**, `-Wall -Wextra -Werror`, clang-tidy, ASan und UBSan im Debug-Build und in CI.
- **Kern plattformfrei.** Keine JNI- oder Android-Header außerhalb von `jni_bridge.cpp`. Der Kern wird mit GoogleTest auf dem Host getestet, ohne Gerät.
- **JNI-Grenze sparsam.** Ein Aufruf pro Auswertung. Primitive Arrays oder direkte `ByteBuffer`, keine Objektgraphen.
- **Parität.** Zu jeder nativen Funktion existiert eine Kotlin-Referenzimplementierung, und ein Test vergleicht beide auf denselben Eingaben.
- **16-KB-Seitenausrichtung** aller `.so` wird in CI geprüft (`-Wl,-z,max-page-size=16384`).
- **Auslieferung** als App Bundle mit ABI-Splits. Native Bibliotheken vervielfachen sonst das Paket.
- Testnamen im C++-Teil englisch (GoogleTest-Konvention), Kommentare deutsch.

---

## 4b. Regeln für Feature-Schalter {#g-4b}

- Ein Schalter ist ein **Zweig**, kein Kommentar. Der Code dahinter wird kompiliert, gelintet und getestet — er wird nur nicht erreicht.
- **Beide Zustände testen.** Die Testkonfiguration setzt den Schalter explizit; jede Verzweigung hat einen Test für „an" und einen für „aus". Nie auf den Standardwert verlassen.
- Ist der Schalter aus, gibt es **keinen sichtbaren Einstiegspunkt** — nicht ausgegraut, sondern gar nicht gerendert — und **keine Nebenwirkungen**: kein registrierter Worker, keine gespeicherte Einwilligung, kein erzeugtes Pseudonym.
- Der **Standardwert wird nie geändert**, außer im Task, der den Schalter scharfschaltet. Zum Ausprobieren dient die Gradle-Property.
- Jeder Schalter hat einen Task, der ihn wieder **entfernt**. Ein Schalter, der länger als zwei Releases steht, kommt auf die Tagesordnung.
- Platzhalter-Inhalte tragen `status: draft`. Die Sicherung, die ein Scharfschalten mit Platzhaltern verhindert, ist unantastbar (V19).
- Der Zustand jedes Schalters steht in `.ai_state.json` unter `featureFlags`.

---

## 5. Commits {#g-5}

```
<typ>(<modul>): <beschreibung> [T-xxx]

<optionaler Rumpf: warum, nicht was>
```

Typen: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`.
Module: `app`, `core`, `widget`, `persons`, `timeline`, `export`, `paywall`, `billing`, `data`, `domain`, `native`.

Beispiele:

```
feat(widget): Zahlenrad-Overlay mit Snap-Fling [T-402]
fix(domain): Intervall über Winterzeitumstellung war 1 h zu kurz [T-107]
test(domain): Pflichtfälle 1-11 für DeriveIntervalsUseCase [T-107]
perf(native): SRI in C++, 5 Jahre Daten 840 ms -> 22 ms [T-911]
feat(paywall): Anonymisierung + Warteschlange hinter FEATURE_RESEARCH_MODE [T-804]
```

Ein Commit pro Task. Kein Commit ohne Task-ID.

---

## 6. Umgang mit Unsicherheit {#g-6}

Wenn der Bauplan eine Frage nicht beantwortet:

1. Steht die Frage bereits in `{#a-15}`? → Task auf `blocked` setzen, in `.ai_state.json.openQuestions` verweisen, mit dem nächsten nicht blockierten Task weitermachen.
2. Ist es eine Umsetzungsentscheidung ohne Auswirkung auf die Architektur? → Entscheiden, in `.ai_state.json.decisions` mit einem Satz Begründung festhalten, weitermachen.
3. Berührt es einen ADR aus `{#a-4}` oder die Modulstruktur? → **Nicht selbst entscheiden.** Task auf `blocked`, Frage in `openQuestions` mit einem konkreten Vorschlag formulieren, aufhören.

Eigene ADR-Einträge werden ab `ADR-100` nummeriert. `ADR-001` bis `ADR-006` sind im Bauplan vergeben.

Nicht raten. Ein blockierter Task mit klarer Frage ist mehr wert als ein erledigter Task mit falscher Annahme.

---

## 7. Grenzen des Agenten {#g-7}

Der Agent führt **nicht** aus:

- Juristische Texte (Datenschutzerklärung, Einwilligung) verfassen oder freigeben.
- Etwas in den Play Store hochladen oder veröffentlichen.
- Signierschlüssel erzeugen, ändern oder committen.
- Echte Nutzerdaten anfassen, exportieren oder hochladen.
- Preise, Produkt-IDs oder Store-Einträge in der Play Console ändern.
- Netzwerkendpunkte für den Forschungs-Upload festlegen.
- Einen Feature-Schalter scharfschalten, dessen Inhalte noch nicht freigegeben sind.
- Entscheiden, dass ein Einwilligungs- oder Aufklärungstext fertig ist.

Diese Punkte gehören in `openQuestions` und werden vom Menschen erledigt.
