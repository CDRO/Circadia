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

In PowerShell, im Projektwurzelverzeichnis:

```powershell
New-Item -ItemType Directory -Force -Path docs\artifacts
Copy-Item BUILD_PLAN.md                docs\artifacts\
Copy-Item TASKS.md                     docs\artifacts\
Copy-Item docs\artifacts\MASTER_PLAN.md docs\artifacts\
Copy-Item agent\ai_state.schema.json  .ai_state.schema.json
Copy-Item agent\ai_state.example.json .ai_state.json
Copy-Item agent\geminirules-merged.txt .geminirules -Force
```

> **Wichtig:** `.geminirules` wird **ersetzt**, nicht ergänzt. `geminirules-merged.txt` ist die fertige Datei — sie enthält den bestehenden Windows-/PowerShell- und GitHub-Workflow **und** die Projektregeln. Ein blosses Anhängen von `GEMINI_RULES_ADDENDUM.md` würde widersprüchliche Regeln erzeugen (`./gradlew` gegen `.\gradlew.bat`, verkettete Befehle, zwei verschiedene Commit-Formate). Das Addendum bleibt als Referenz erhalten, ist aber nicht mehr die Datei, die man einbaut.

Danach `.ai_state.json` leeren: alle `tasks` auf `"status": "pending"`, alle `milestones` auf `"pending"`, `currentMilestone` auf `"M0"`, `currentTask` auf `null`, `github` und `reviewLoop` zurücksetzen, `sessionLog` und `decisions` leeren. Die `openQuestions`, `constraints` und `featureFlags` bleiben stehen – sie gelten von Anfang an.

## Zweiter technischer Vorbehalt: nativer Code {#r-2b}

Performance-kritische Komponenten werden in C++ gebaut, nicht in Kotlin: die Rhythmus-Analytik (SRI, IS, IV über Jahre minutengerasterter Daten) und die Rasterisierung des Aktogramms beim Zoomen. Sie liegen im Modul `:core:native` hinter einer schmalen JNI-Fassade. Bedingung ist ein Benchmark-Beleg vor jeder Portierung, eine Kotlin-Referenzimplementierung als Wahrheit und als Fallback, Sanitizer in CI und 16-KB-Seitenausrichtung. Details in `BUILD_PLAN.md {#a-3-4}` und `{#a-4-6}`.

## Forschungsmodus: gebaut, aber aus {#r-2c}

M8 baut die gesamte Logik des Forschungsmodus — Umfrage-Motor, gestufte Einwilligung, Anonymisierung, Warteschlange, Widerruf — gegen Platzhalter-Inhalte und hinter `FEATURE_RESEARCH_MODE = false`. In der App ist davon nichts sichtbar, es wird nichts erhoben und nichts hochgeladen; das Data-Safety-Formular für v1.0 beschreibt entsprechend keine Datenweitergabe. Die Inhalte — Fragebogen, freigegebene Texte, echter Endpunkt — kommen in M11 und münden in v1.1. Ein Pflichttest verhindert, dass der Schalter angeht, solange Platzhalter im Spiel sind. Details in `BUILD_PLAN.md {#a-4-7}`, `{#a-8-4}` und `{#a-8-5}`.

## Ablage im Projekt {#r-2d}

```
<projekt>/
├─ .geminirules                    ← agent/geminirules-merged.txt
├─ .ai_state.json                  ← agent/ai_state.example.json, geleert
├─ .ai_state.schema.json
└─ docs/artifacts/
   ├─ MASTER_PLAN.md               ← Einstieg: Meilensteinliste
   ├─ BUILD_PLAN.md                ← Spezifikation
   ├─ TASKS.md                     ← Backlog
   └─ milestones/
      └─ M3-widget-basis/          ← vom Agenten je Meilenstein angelegt
         ├─ implementation_plan.md
         └─ tasks.md
```

## Verweissystem {#r-3}

`TASKS.md` verweist mit Anker-IDs in `BUILD_PLAN.md`, z. B. `{#a-5-3}` → Abschnitt 5.3 „Ableitung der Intervalle". Diese IDs sind stabil und übersetzungsfest. Wer einen Abschnitt umbenennt, behält die ID.

## Was ein Mensch entscheiden muss {#r-4}

Sieben Punkte, gesammelt in `BUILD_PLAN.md {#a-15}` und gespiegelt in `.ai_state.json.openQuestions`. Vier davon blockieren die **Scharfschaltung** des Forschungsmodus (M11), einer die Monetarisierung (M7). Alles bis einschließlich M10 — inklusive der kompletten Forschungslogik — lässt sich ohne eine einzige Antwort durchbauen.

## Wichtigster technischer Vorbehalt {#r-5}

Android-Homescreen-Widgets können keine Wischgesten erkennen. Die PRD-Anforderung „Scrollen auf den Zahlenrädern im Widget" wird deshalb über eine transparente Overlay-Activity gelöst, die wie das aufgeklappte Widget aussieht und echtes Fling-Scrollen mit Rastung bietet. Begründung und verworfene Alternative in `BUILD_PLAN.md {#a-2-1}` und `{#a-4-2}`.
