# M4 — Widget: Zahlenräder

## Auftrag
Dieses Milestone implementiert die interaktiven Zahlenräder für das Widget über eine transparente Overlay-Activity (`WheelPickerActivity`). Es führt den `HELD`-Zustand ein, in dem das Widget für 60 Sekunden eine manuell gewählte Zeit anzeigt, bevor es wieder zur `LIVE`-Zeit zurückkehrt. Barrierefreiheit (TalkBack) und haptisches Feedback sind integraler Bestandteil.

## Relevante Spezifikation
- **Android-Widget Constraints:** {#a-2-1} (Keine Wischgesten im Widget).
- **Zahlenräder ADR:** {#a-4-2} (Overlay-Activity statt direkt im Widget).
- **WheelPickerActivity:** {#a-6-5} (Transparentes Theme, Fling-Scrollen, Haptik).
- **HELD-Modus:** {#a-6-2} (Zustandsautomat, 60s Ablauf).

## Vorgehen
1. **Infrastruktur:** Transparentes Theme in `:core:designsystem` anlegen. `WheelPickerActivity` in `:widget` registrieren.
2. **UI:** Implementierung der Zahlenräder mit Compose `LazyColumn` + `rememberSnapFlingBehavior`.
3. **Logik:** Kommunikation zwischen Widget und Activity über Intents und `GlanceStateDefinition`.
4. **Hold-Mechanik:** Ablaufprüfung im Widget-Render und optionaler Alarm.
5. **Polishing:** Haptik und TalkBack-Unterstützung.

## Betroffene Pflichttestfälle
- **14:** Hold läuft nach exakt 60 s ab.
- **15:** Hold-Alarm feuert verspätet -> Render fällt trotzdem korrekt auf `LIVE`.

## Risiken und offene Punkte
- **Activity-Transition:** Muss absolut flüssig wirken, um das Gefühl eines "aufgeklappten" Widgets zu vermitteln.
- **Zonen-Handling:** Sicherstellen, dass die gewählte Zeit korrekt in UTC umgerechnet wird für das Event.

## Abnahme
- [ ] Räder lassen sich per Wischgeste verstellen.
- [ ] Zeit wird im Widget für 60s "gehalten".
- [ ] TalkBack liest Ziffern vor und erlaubt Inkremente.
