---
doc_id: master_plan
project: circadia
lang: de
version: 1.0.0
spec: BUILD_PLAN.md
backlog: TASKS.md
state: .ai_state.json
translatable: true
translation_notes: Meilenstein-IDs, Task-IDs, Anker-Verweise, Ordnernamen und Befehle NICHT übersetzen.
---

# MASTER_PLAN — Meilensteine in Reihenfolge {#mp-0}

**Für den Agenten:** Dies ist die Reihenfolge. Nimm den obersten Meilenstein, dessen Status in `.ai_state.json` nicht `completed` ist und der nicht blockiert ist. Lies dann den Abschnitt „Spec" dieses Meilensteins in `BUILD_PLAN.md` und seine Tasks in `TASKS.md` vollständig, bevor du irgendeinen `gh`-Befehl ausführst.

Danach legst du `docs/artifacts/milestones/<ID>-<slug>/` an und schreibst dort `implementation_plan.md` und `tasks.md`, bevor die erste Codezeile entsteht.

| ID | Ordner-Slug | Titel | Spec | Tasks | Abnahme |
|---|---|---|---|---|---|
| M0 | `M0-geruest` | Gerüst | {#a-3-2}, {#a-3-3} | T-001 … T-010 | Multi-Modul-Projekt baut, CI grün, leere App startet |
| M1 | `M1-datenkern` | Datenkern | {#a-5} | T-101 … T-110 | Room-Schema v1, Intervall-Ableitung, Pflichttests 1–11 grün |
| M2 | `M2-personen` | Personen & Onboarding | {#a-6-4} | T-201 … T-205 | Personen verwaltbar, Namenserfassung liefert Ergebnis |
| M3 | `M3-widget-basis` | Widget: Basis | {#a-6} | T-301 … T-310 | Widget platzierbar, Live-Zeit, Ereigniserfassung, Zero-State |
| M4 | `M4-widget-raeder` | Widget: Zahlenräder | {#a-6-5} | T-401 … T-409 | Fling-Auswahl, 60-Sekunden-Halten, Barrierefreiheit |
| M5 | `M5-auswertung` | Auswertung | {#a-7} | T-501 … T-511 | Aktogramm, Vergleich, Korrektur von Einträgen |
| M6 | `M6-export` | Export | {#a-8-3} | T-601 … T-606 | CSV und JSON, verlustfreier Roundtrip |
| M7 | `M7-monetarisierung` | Monetarisierung | {#a-8-1} | T-701 … T-708 | Abo, Freischaltungslogik, Offline-Kulanz |
| M8 | `M8-forschung-logik` | Forschungsmodus: Technik & Logik | {#a-8-2}, {#a-8-4}, {#a-8-5} | T-801 … T-810 | Gegen Platzhalter durchlaufbar, Schalter aus, nichts sichtbar |
| M9 | `M9-pro-analytik` | Pro-Analytik | {#a-7-3}, {#a-3-4} | T-901 … T-914 | Kennzahlen belegt, nativer Kern gemessen begründet |
| M10 | `M10-release-v1` | Release-Härtung v1.0 | {#a-11-3}, {#a-9-2} | T-1001 … T-1008 | Release-Kandidat auf dem internen Track, Forschungsmodus aus |
| M11 | `M11-forschung-inhalte` | Forschungsmodus: Inhalte & Scharfschaltung | {#a-4-7}, {#a-8-5} | T-1101 … T-1106 | Schalter an und entfernt, echter Endpunkt, v1.1 |

## Blockierungen {#mp-1}

| Meilenstein | Blockiert durch | Was fehlt |
|---|---|---|
| M7 | Q4 | Play-Entwicklerkonto, Abo-Produkt angelegt |
| M11 | Q1, Q2, Q3, Q6 | Verantwortliche Stelle, Endpunkt, freigegebene Texte, Fragebogen |

Alle übrigen Meilensteine laufen ohne menschliche Entscheidung. **M8 ist ausdrücklich nicht blockiert** — er wird gegen Platzhalter-Inhalte gebaut, siehe ADR-007 in `BUILD_PLAN.md {#a-4-7}`.

## Vorlage für `implementation_plan.md` {#mp-2}

```markdown
# <ID> — <Titel>

## Auftrag
<Ein Absatz: was dieser Meilenstein liefert und woran man erkennt, dass er fertig ist.>

## Relevante Spezifikation
<Wörtlich übernommene oder zusammengefasste Abschnitte aus BUILD_PLAN.md, mit Anker-IDs.>

## Vorgehen
<Konkrete Umsetzung: Klassen, Dateien, Reihenfolge, Fallstricke.>

## Betroffene Pflichttestfälle
<Nummern aus {#a-11-2} und wo sie liegen werden.>

## Risiken und offene Punkte
<Was schiefgehen kann; alles, was blockiert, mit Q-Nummer.>

## Abnahme
<Die Abnahmekriterien aus diesem MASTER_PLAN, als Checkliste.>
```

## Vorlage für `tasks.md` {#mp-3}

Die Tasktabelle dieses Meilensteins aus `TASKS.md`, wörtlich kopiert, plus eine Statusspalte. `.ai_state.json` bleibt die Quelle der Wahrheit; diese Datei ist die menschenlesbare Spiegelung im PR.
