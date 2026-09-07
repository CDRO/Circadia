# M4 — Widget: Zahlenräder: Tasks

| ID | Task | Status | Spec |
|---|---|---|---|
| T-401 | `WheelPickerActivity` + `Theme.Circadia.Transparent`, öffnet ohne sichtbaren Aktivitätswechsel, Tap außerhalb verwirft. | in_progress | {#a-6-5} |
| T-402 | Zwei Compose-Räder mit `rememberSnapFlingBehavior`, virtuell endlos, initial auf den angezeigten Wert gescrollt, Verlaufsmaske, Haptik `CLOCK_TICK` pro Raste. | pending | {#a-6-5} |
| T-403 | 12h-Modus mit AM/PM-Element. | pending | {#a-10} |
| T-404 | Automatische Bestätigung 400 ms nach Stillstand + expliziter Bestätigungsbereich. Ergebnis zurück an das Widget. | pending | {#a-6-5} |
| T-405 | `HELD`-Modus im Widget: statischer Wert statt `TextClock`, sichtbarer Halte-Indikator, `holdExpiresAtUtcMillis`. | pending | {#a-6-2} |
| T-406 | Einmaliger `AlarmManager.set()` auf das Hold-Ende **plus** Ablaufprüfung bei jedem Render (Alarm ist nur Optimierung). Kein `SCHEDULE_EXACT_ALARM`. | pending | {#a-2-2} |
| T-407 | Hold endet sofort beim Drücken von Sonne/Mond; das Event bekommt die gehaltene Zeit als `occurredAt`. | pending | {#a-6-3} |
| T-408 | Barrierefreiheit: `contentDescription`, TalkBack-Inkremente auf den Rädern, Langdruck-Fallback auf System-`TimePicker`. | pending | {#a-6-5} |
| T-409 | Tests 14 und 15 (60-s-Regel, verspäteter Alarm). | pending | {#a-11-2} |
