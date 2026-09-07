# M1 — Datenkern

## Auftrag
Dieser Meilenstein liefert den Kern der Datenverarbeitung von Circadia. Er umfasst das Room-Datenbankschema für Personen und Ereignisse, die Repository-Schicht sowie die zentrale Geschäftslogik zur Ableitung von Schlaf-Wach-Intervallen aus einem Ereignis-Log. Abgeschlossen ist der Meilenstein, wenn alle 11 Pflichttestfälle für die Intervall-Ableitung (insbesondere die DST-Fälle) grün sind.

## Relevante Spezifikation
- **Datenmodell:** {#a-5-1} (Person, StateEvent, SleepState)
- **Room-Schema:** {#a-5-2} (Tabelle `persons`, `state_events`, `widget_bindings`)
- **Intervall-Ableitung:** {#a-5-3} (Reine Funktion, 6 Regeln für die Ableitung)
- **Tagesgrenzen:** {#a-5-4} (DayBucketing, Default 18:00 Uhr)

## Vorgehen
1. **Infrastruktur-Bootstrap:** Da M0-Artefakte fehlen, werden `:core:model`, `:core:common` und `:core:domain` initialisiert.
2. **Datenmodell:** Implementierung der Domänenmodelle in `:core:model`.
3. **Logik:** Implementierung des `DeriveIntervalsUseCase` in `:core:domain`.
4. **Bugfix T-107:** Der Fokus liegt auf der korrekten Handhabung von UTC-Zeitstempeln vs. lokalen Zeitzonen bei der Intervallberechnung, um die 1-Stunden-Abweichung bei DST-Übergängen zu korrigieren.
5. **Persistence:** Room-Setup in `:core:database` und Repository-Implementierung in `:core:data`.

## Betroffene Pflichttestfälle
- **1–11:** `DeriveIntervalsUseCaseTest` (in `:core:domain`)
- **Fälle 8 & 9:** Explizite Tests für Sommer-/Winterzeitumstellung.

## Risiken und offene Punkte
- **Q7:** Tagesgrenze einstellbar? (Vorschlag: Ja, Default 18:00).
- **DST-Handling:** Die korrekte Berechnung der Dauer über Zonenwechsel hinweg ist kritisch.

## Abnahme
- [ ] Room-Schema v1 erstellt und eingecheckt.
- [ ] Repositories und DAOs implementiert.
- [ ] `DeriveIntervalsUseCase` implementiert.
- [ ] Pflichttests 1–11 grün (insb. DST).
