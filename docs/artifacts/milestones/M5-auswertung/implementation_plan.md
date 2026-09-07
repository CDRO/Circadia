# M5 — Auswertung

## Auftrag
Dieses Milestone liefert die In-App-Visualisierung der Schlaf-Wach-Daten. Zentrales Element ist das Aktogramm (Single- und Double-Plot), das Verläufe pro Person und im Vergleich mehrerer Personen darstellt. Zusätzlich werden grundlegende Kennzahlen (Schlafzeit, Episoden) berechnet und eine Korrekturmöglichkeit für fehlerhafte Einträge geschaffen.

## Relevante Spezifikation
- **Aktogramm:** {#a-7-1} (Double-Plot, Double-Plot versetzt, Canvas).
- **Vergleichsansicht:** {#a-7-2} (Übereinandergelegt mit Multiply, Nebeneinander).
- **Kennzahlen:** {#a-7-3} (Gesamtschlafzeit, Episoden, Wachphasen, Einschlaf-/Aufwachzeit).
- **Datenkorrektur:** {#a-7-4} (Detailblatt, CORRECTION-Events).

## Vorgehen
1. **Design-Grundlagen (T-501):** Definition der Diagrammfarben (kategoriale Palette) und Achsenstile in `:core:designsystem`.
2. **Aktogramm-Kern (T-502, T-503):** Implementierung des Renderers mittels Compose `Canvas` für Single- und Double-Plot.
3. **Navigation & Zeit (T-504):** Integration der Zeitachsen-Steuerung (Scrollen/Zoomen).
4. **Mehrpersonen-Modus (T-505, T-506):** Implementierung der Überlagerung (`BlendMode.Multiply`) und der Nebeneinander-Ansicht.
5. **Kennzahlen (T-507):** Implementierung der Berechnungslogik in `:core:domain`.
6. **Korrektur-Workflow (T-508, T-509):** Erstellung des Detailblatts und der Verlaufsansicht.
7. **Feinschliff (T-510, T-511):** Leerzustände und Performance-Messung (Macrobenchmark).

## Betroffene Pflichttestfälle
- **DayBucketing:** (Bereits in M1 getestet, hier relevant für die Anzeige).
- **Performance:** T-511 entscheidet über die Notwendigkeit von `:core:native` (T-911).

## Risiken und offene Punkte
- **Performance bei Canvas:** Viele Zeichenoperationen könnten bei langen Zeiträumen ruckeln.
- **Q7:** Tagesgrenze einstellbar oder fest bei 18:00? (Wichtig für DayBucketing).

## Abnahme
- [ ] Aktogramm zeigt Schlafphasen korrekt an.
- [ ] Umschaltung zwischen Single- und Double-Plot funktioniert.
- [ ] Mehrere Personen können verglichen werden.
- [ ] Einträge lassen sich über das Detailblatt korrigieren.
