package ch.circadia.tracker.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import ch.circadia.tracker.core.model.PersonId

object UpdateWidgetReceiver {
    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(CircadiaWidget::class.java)
        glanceIds.forEach {
            CircadiaWidget().update(context, it)
        }
    }
}
