# M9 — Pro-Analytik

## Auftrag
Dieses Milestone implementiert fortgeschrittene Schlafmetriken für die kostenpflichtige Stufe der App. Dazu gehören wissenschaftlich fundierte Kennzahlen wie der Sleep Regularity Index (SRI), Interdaily Stability (IS) und Intradaily Variability (IV) sowie die Bestimmung des Chronotyps (MSFsc). Ein wesentlicher Teil ist die Performance-Messung: Wir implementieren die Algorithmen zunächst in Kotlin und portieren sie nur dann nach C++ (:core:native), wenn Benchmarks belegen, dass das Budget (z. B. für flüssiges Scrollen im Aktogramm) überschritten wird.

## Relevante Spezifikation
- **Pro-Kennzahlen:** {#a-7-3} (Schlafmitte, MSFsc, Jetlag, SRI, IS, IV).
- **Wissenschaftliche Quellen:** MCTQ (Roenneberg), SRI (Phillips), IS/IV (Van Someren).
- **Nativer Kern:** {#a-3-4} (Measure first, platform-free core, 16-KB alignment).
- **Ehrlichkeit gegenüber Nutzenden:** {#a-7-3} (Definitionen, Quellen und Genauigkeitsvorbehalte einblenden).

## Vorgehen
1. **Algorithmik (T-901 bis T-907):** Schrittweise Implementierung der Metriken in `:core:domain` mit umfangreichen Unit-Tests gegen Referenzwerte.
2. **UI-Integration (T-908):** Erstellung des Analytik-Screens mit detaillierten Informationskarten für jede Metrik.
3. **Messung (T-909):** Durchführung von Microbenchmarks der Kotlin-Implementierungen mit großen Datensätzen (bis zu 5 Jahre).
4. **Nativer Kern (T-910 bis T-914):** Falls nötig, Aufbau des `:core:native` Moduls (CMake, JNI) und Portierung der rechenintensiven Teile.
5. **Verdrahtung:** Sicherstellung des Fallback-Mechanismus (Kotlin-Referenzimplementierung).

## Betroffene Pflichttestfälle
- **29 bis 32:** Parität zwischen nativem Kern und Kotlin-Referenzimplementierung.

## Risiken und offene Punkte
- **Datenqualität:** Da die Daten selbstberichtete Zeitpunkte sind, müssen die Algorithmen robust gegenüber Lücken sein.
- **Speichermanagement in C++:** Absolute Sorgfalt nötig, um Abstürze zu vermeiden (Sanitizer in CI).

## Abnahme
- [ ] Alle Pro-Metriken werden korrekt berechnet und angezeigt.
- [ ] Definitionen und Quellen sind in der UI einsehbar.
- [ ] Kotlin-Benchmarks liegen vor.
- [ ] Falls portiert: Nativer Kern ist paritätsgeprüft und 16-KB-ausgerichtet.
