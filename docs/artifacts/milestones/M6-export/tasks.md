# M6 — Export: Tasks

| ID | Task | Status | Spec |
|---|---|---|---|
| T-601 | `:feature:export`: CSV-Export der Roh-Events (alle Spalten aus `state_events`, RFC-4180-konform maskiert, UTF-8 mit BOM für Excel). | pending | {#a-8-3} |
| T-602 | CSV-Export der abgeleiteten Intervalle als zweite Datei. | pending | {#a-5-3} |
| T-603 | JSON-Export (`kotlinx.serialization`), versioniertes Schema mit `schemaVersion`. | pending | {#a-8-3} |
| T-604 | Speichern über `ACTION_CREATE_DOCUMENT` (SAF), kein Schreiben in fremde Verzeichnisse, keine Speicher-Berechtigung. Fortschrittsanzeige bei großen Datenmengen. | pending | {#a-9-2} |
| T-605 | JSON-Import (Roundtrip), Events werden mit `source = IMPORT` übernommen, Duplikate über `id` erkannt. | pending | {#a-11-2} |
| T-606 | Tests 26–28. | pending | {#a-11-2} |
