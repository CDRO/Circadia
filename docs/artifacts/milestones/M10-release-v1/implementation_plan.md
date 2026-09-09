# M10 — Release-Härtung v1.0

## Auftrag
Dieser Meilenstein bereitet die App auf den produktiven Einsatz vor (Release-Kandidat). Fokus liegt auf Polishing, Barrierefreiheit, Lokalisierungsvollständigkeit und der technischen Konfiguration für den Play Store (R8, Data Safety). Der Forschungsmodus bleibt wie geplant deaktiviert. Ziel ist ein Release-Kandidat auf dem internen Test-Track.

## Relevante Spezifikation
- **Internationalisierung:** {#a-10} (Audit, alle Sprachen synchron).
- **Barrierefreiheit:** {#a-6-5} (TalkBack-Audit, Kontraste, touch targets).
- **R8 / ProGuard:** {#a-3-3} (Minifizierung, Regeln für Room/Hilt/Serialization).
- **Data Safety & Policy:** {#a-9-2} (Health-Apps-Policy, Datenschutz).
- **Manuelle Prüfliste:** {#a-11-3} (Vollständige Abarbeitung vor Release).

## Vorgehen
1. **i18n-Audit (T-1001):** Systematischer Check aller `strings.xml`. Sicherstellen, dass keine Hardcoded-Strings vorhanden sind und alle Plurale/Formate stimmen.
2. **Accessibility-Check (T-1002):** Manueller Durchlauf mit TalkBack. Prüfung der Touch-Ziele (>= 48dp) und Schriftgrößen-Skalierung.
3. **Build-Optimierung (T-1003):** Aktivierung von `minifyEnabled`, Konfiguration der ProGuard-Regeln und Test des Release-Builds.
4. **Compliance (T-1004):** Vorbereitung der Data-Safety-Angaben und Verlinkung der Datenschutzerklärung.
5. **Store-Vorbereitung (T-1005):** Erstellung der Store-Assets (Texte, Screenshots).
6. **Verifikation (T-1006):** Abarbeitung der 36 mandatory Testfälle und der manuellen Prüfliste.

## Betroffene Pflichttestfälle
- **Alle:** Vollständiger Test-Run vor dem Release.

## Risiken und offene Punkte
- **R8-Nebenwirkungen:** Mögliche Runtime-Fehler bei Reflection (Hilt/Serialization) im Release-Build.
- **Q3:** Datenschutzerklärung muss vor dem Store-Upload final vorliegen.

## Abnahme
- [ ] Release-Build ohne Abstürze testbar.
- [ ] i18n und Accessibility-Vorgaben erfüllt.
- [ ] Manuelle Prüfliste {#a-11-3} dokumentiert abgehakt.
- [ ] Forschungsmodus im Produktiv-Build unsichtbar und inaktiv.
