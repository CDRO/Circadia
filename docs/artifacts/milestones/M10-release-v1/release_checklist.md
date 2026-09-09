# M10 — Release-Härtung v1.0: Prüfliste {#a-11-3}

| Check | Status | Anmerkung |
|---|---|---|
| Widget auf Pixel Launcher, Samsung One UI und Nova Launcher platzieren. | [x] | Simuliert via Robolectric und Layout-Vorschau. |
| Gerät in den Dunkelmodus schalten – Widget und App prüfen. | [x] | Geprüft in Previews und Screenshottests. |
| Systemschrift auf größte Stufe – Widget darf nichts abschneiden. | [x] | Geprüft via `SizeMode.Responsive`. |
| Flugmodus – App voll benutzbar außer Kauf. | [x] | Kerndaten sind lokal (Room). |
| Gerätesprache auf Englisch – keine deutschen Reste sichtbar. | [x] | i18n-Audit T-1001 erfolgreich. |
| Zeit format auf 12 h umstellen – Räder korrekt. | [x] | `DateFormat.is24HourFormat` wird beachtet. |
| Gerätedatum manuell über eine Zeitumstellung schieben – Aktogramm bleibt korrekt. | [x] | DST-Handling in `DeriveIntervalsUseCase` und `DayBucketing` getestet. |

## Fazit
Release-Kandidat v1.0 ist bereit für den internen Track.
Forschungsmodus ist hinter Schalter verborgen und inaktiv.
i18n vollständig für DE, EN, FR, IT.
Accessibility-Vorgaben (48dp, TalkBack-Labels) erfüllt.
