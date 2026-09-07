---
doc_id: readme
project: circadia
lang: de
version: 1.0.0
translatable: true
---

# Circadia — Bauplan-Paket {#r-0}

Bauplan für einen Schlaf-Wach-Zyklus-Tracker (Android), geschrieben für einen Gemini-Coding-Agenten, lesbar für Menschen.

## Dateien {#r-1}

| Datei | Zweck | Wer liest sie |
|---|---|---|
| `BUILD_PLAN.md` | Die Spezifikation: Architektur, Datenmodell, Widget-Konzept, Monetarisierung, Recht, Tests. | Mensch und Agent |
| `TASKS.md` | Das Backlog: 11 Meilensteine, ~90 atomare Tasks mit Abhängigkeiten und Verweisen in die Spezifikation. | Agent (Arbeitsreihenfolge) |
| `agent/GEMINI_RULES_ADDENDUM.md` | Inhalte zum Übernehmen in die bestehende `.geminirules`: Arbeitszyklus, 14 Verbote, Codekonventionen, Commit-Format, Grenzen des Agenten. | Agent |
| `agent/ai_state.schema.json` | JSON-Schema für `.ai_state.json`. Meilensteine, Tasks, Verifikationsergebnisse, Pflichttestfälle, ADRs, offene Fragen. | Agent (Validierung) |
| `agent/ai_state.example.json` | Ausgefülltes Beispiel mitten in M1, inklusive eines blockierten Tasks. Als Startpunkt kopierbar. | Agent |

## Einrichtung {#r-2}

```bash
cp BUILD_PLAN.md TASKS.md            <projekt>/
cp agent/ai_state.schema.json        <projekt>/.ai_state.schema.json
cp agent/ai_state.example.json       <projekt>/.ai_state.json   # danach Tasks auf "pending" zurücksetzen
cat agent/GEMINI_RULES_ADDENDUM.md >> <projekt>/.geminirules
```

Danach `.ai_state.json` leeren: alle `tasks` auf `"status": "pending"`, alle `milestones` auf `"pending"`, `currentMilestone` auf `"M0"`, `currentTask` auf `null`, `sessionLog` und `decisions` leeren. Die `openQuestions` und `constraints` bleiben stehen – sie gelten von Anfang an.

## Zweiter technischer Vorbehalt: nativer Code {#r-2b}

Performance-kritische Komponenten werden in C++ gebaut, nicht in Kotlin: die Rhythmus-Analytik (SRI, IS, IV über Jahre minutengerasterter Daten) und die Rasterisierung des Aktogramms beim Zoomen. Sie liegen im Modul `:core:native` hinter einer schmalen JNI-Fassade. Bedingung ist ein Benchmark-Beleg vor jeder Portierung, eine Kotlin-Referenzimplementierung als Wahrheit und als Fallback, Sanitizer in CI und 16-KB-Seitenausrichtung. Details in `BUILD_PLAN.md {#a-3-4}` und `{#a-4-6}`.

## Forschungsmodus: gebaut, aber aus {#r-2c}

M8 baut die gesamte Logik des Forschungsmodus — Umfrage-Motor, gestufte Einwilligung, Anonymisierung, Warteschlange, Widerruf — gegen Platzhalter-Inhalte und hinter `FEATURE_RESEARCH_MODE = false`. In der App ist davon nichts sichtbar, es wird nichts erhoben und nichts hochgeladen; das Data-Safety-Formular für v1.0 beschreibt entsprechend keine Datenweitergabe. Die Inhalte — Fragebogen, freigegebene Texte, echter Endpunkt — kommen in M11 und münden in v1.1. Ein Pflichttest verhindert, dass der Schalter angeht, solange Platzhalter im Spiel sind. Details in `BUILD_PLAN.md {#a-4-7}`, `{#a-8-4}` und `{#a-8-5}`.

## Verweissystem {#r-3}

`TASKS.md` verweist mit Anker-IDs in `BUILD_PLAN.md`, z. B. `{#a-5-3}` → Abschnitt 5.3 „Ableitung der Intervalle". Diese IDs sind stabil und übersetzungsfest. Wer einen Abschnitt umbenennt, behält die ID.

## Was ein Mensch entscheiden muss {#r-4}

Sieben Punkte, gesammelt in `BUILD_PLAN.md {#a-15}` und gespiegelt in `.ai_state.json.openQuestions`. Vier davon blockieren die **Scharfschaltung** des Forschungsmodus (M11), einer die Monetarisierung (M7). Alles bis einschließlich M10 — inklusive der kompletten Forschungslogik — lässt sich ohne eine einzige Antwort durchbauen.

## Wichtigster technischer Vorbehalt {#r-5}

Android-Homescreen-Widgets können keine Wischgesten erkennen. Die PRD-Anforderung „Scrollen auf den Zahlenrädern im Widget" wird deshalb über eine transparente Overlay-Activity gelöst, die wie das aufgeklappte Widget aussieht und echtes Fling-Scrollen mit Rastung bietet. Begründung und verworfene Alternative in `BUILD_PLAN.md {#a-2-1}` und `{#a-4-2}`.
