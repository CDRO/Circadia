package ch.circadia.tracker.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Farbkatalog für Diagramme.
 * Basierend auf Paul Tols "Muted qualitative palette", optimiert für Farbbildblindheit.
 */
object ChartColors {
    val Blue = Color(0xFF332288)
    val Green = Color(0xFF117733)
    val Cyan = Color(0xFF44AA99)
    val SkyBlue = Color(0xFF88CCEE)
    val Yellow = Color(0xFFDDCC77)
    val Red = Color(0xFFCC6677)
    val Pink = Color(0xFFAA4499)
    val Wine = Color(0xFF882255)

    val Palette = listOf(
        Blue,
        Green,
        Cyan,
        SkyBlue,
        Yellow,
        Red,
        Pink,
        Wine
    )

    /**
     * Gibt eine Farbe aus der Palette basierend auf einem Seed zurück (z.B. PersonId-Hash).
     */
    fun colorForSeed(seed: Int): Color {
        val index = (seed % Palette.size).let { if (it < 0) it + Palette.size else it }
        return Palette[index]
    }

    val Axis = Color.Gray.copy(alpha = 0.5f)
    val NoData = Color.LightGray.copy(alpha = 0.3f)
}
