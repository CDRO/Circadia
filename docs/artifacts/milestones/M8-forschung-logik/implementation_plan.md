# M8 — Forschungsmodus: Technik & Logik

## Auftrag
Dieser Meilenstein implementiert die technische Infrastruktur für den Forschungsmodus der App. Ziel ist ein vollständig funktionaler Pfad von der Aufklärung über die Umfrage bis zur anonymisierten Datenübertragung, jedoch ohne echte Inhalte (Platzhalter) und hinter einem deaktivierten Feature-Schalter (`FEATURE_RESEARCH_MODE = false`). So wird die komplexe Logik (Anonymisierung, Warteschlange, Widerruf) frühzeitig und testbar integriert, ohne die Datenschutz-Konformität der Version 1.0 zu gefährden.

## Relevante Spezifikation
- **Freischaltung durch Forschung:** {#a-8-2} (Aufklärung, Umfrage, Einwilligung, Freischaltung).
- **Anonymisierung:** {#a-8-2} (Rundung, Pseudonym, kein Klarname).
- **Feature-Schalter:** {#a-8-4} (Standardmäßig aus, keine Einstiegspunkte bei false).
- **Platzhalter-Sicherung:** {#a-8-5} (Testfall 35: kein Release mit Draft-Inhalten).
- **Widerruf:** {#a-9-1} (Löschung lokaler Daten, Queue für Server-Löschanfrage).

## Vorgehen
1. **Infrastruktur:** Einrichtung des `FeatureFlags` in `:core:common` und Verdrahtung in der UI.
2. **UI-Komponenten:** Aufklärungsseite (Scroll-to-continue) und Umfrage-Engine (inhaltsagnostisch via JSON).
3. **Datenmodell:** Erweiterung um `research_consents` und Teilnehmer-Pseudonyme.
4. **Logik:** Implementierung des `AnonymizeUseCase` mit strengen Tests auf Klarnamenfreiheit.
5. **Background:** WorkManager für den Upload mit exponentiellem Backoff und Noop-Endpunkt.
6. **Widerruf & Löschung:** Implementierung der „Alle Daten löschen" Funktion und der Widerrufs-Warteschlange.
7. **Sicherung:** Implementierung von Testfall 35, um versehentliches Scharfschalten von Platzhaltern zu verhindern.

## Betroffene Pflichttestfälle
- **33/34:** Schalter aus -> Keine UI, keine Nebenwirkungen.
- **35:** Schalter an + Draft -> Test schlägt fehl.
- **36:** Schalter an -> Vollständiger Ablauf gegen Noop-Endpunkt.

## Risiken und offene Punkte
- **Anonymisierung:** Muss absolut wasserdicht sein, bevor jemals ein Schalter umgelegt wird.
- **TalkBack:** Die Umfrage-Engine muss von Beginn an barrierefrei sein.

## Abnahme
- [ ] Forschungsweg mit `-PresearchMode=true` vollständig durchlaufbar.
- [ ] Anonymisierung testbar bewiesen.
- [ ] Widerruf funktioniert offline (Queue).
- [ ] Im Standardbuild (`false`) ist nichts sichtbar oder aktiv.
- [ ] Platzhalter-Sicherung (Test 35) greift.
