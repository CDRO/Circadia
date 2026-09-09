# M6 — Export

## Auftrag
Dieses Milestone liefert die Export-Funktionalität der App. Nutzende können ihre Rohdaten (Events) und die daraus abgeleiteten Intervalle als CSV- oder JSON-Dateien exportieren. Dies geschieht über das Android Storage Access Framework (SAF) mittels `ACTION_CREATE_DOCUMENT`, um Datenschutz und Nutzerkontrolle zu gewährleisten. Ein JSON-Import (Roundtrip) ermöglicht die Wiederherstellung oder den Transfer von Daten.

## Relevante Spezifikation
- **Datenmodell:** {#a-5-1} (StateEvent, Interval).
- **Export-Anforderungen:** {#a-1-1} (Prinzip P2: Rohdaten sind heilig).
- **SAF & Berechtigungen:** {#a-9-2} (Minimalistische Berechtigungen, SAF für Export).
- **CSV-Format:** RFC-4180 konform, UTF-8 mit BOM.
- **JSON-Format:** `kotlinx.serialization`, versioniertes Schema.

## Vorgehen
1. **Modul-Setup:** Erstellung des Moduls `:feature:export` (falls noch nicht geschehen).
2. **Serialisierung:** Implementierung von CSV-Writern für Events und Intervalle.
3. **JSON-Logik:** Einrichtung von `kotlinx.serialization` für `StateEvent` und `Person`.
4. **UI & SAF:** Erstellung eines Export-Screens in der App, der die SAF-Intents auslöst.
5. **Import:** Implementierung der Import-Logik für JSON-Dateien mit Duplikaterkennung.
6. **Tests:** Verifikation der Maskierung in CSV und des verlustfreien JSON-Roundtrips.

## Betroffene Pflichttestfälle
- **26:** CSV mit Sonderzeichen korrekt maskiert.
- **27:** Export bei 0 Events liefert gültige Datei mit Header.
- **28:** Verlustfreier JSON-Roundtrip (Export -> Import).

## Risiken und offene Punkte
- **Große Datenmengen:** Bei jahrelangen Aufzeichnungen muss der Export performant sein und den UI-Thread nicht blockieren (Dispatcher-IO).

## Abnahme
- [ ] Vollständiger CSV-Export der Events (alle Spalten).
- [ ] CSV-Export der Intervalle.
- [ ] JSON-Export und Import funktionieren verlustfrei.
- [ ] SAF wird korrekt verwendet (keine WRITE_EXTERNAL_STORAGE Berechtigung nötig).
