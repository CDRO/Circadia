package ch.circadia.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PersonEntity::class,
        StateEventEntity::class,
        WidgetBindingEntity::class,
        ResearchConsentEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CircadiaDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun stateEventDao(): StateEventDao
    abstract fun widgetBindingDao(): WidgetBindingDao
}
