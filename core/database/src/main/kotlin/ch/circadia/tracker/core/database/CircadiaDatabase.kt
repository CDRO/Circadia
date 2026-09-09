package ch.circadia.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PersonEntity::class,
        StateEventEntity::class,
        WidgetBindingEntity::class,
        ResearchConsentEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class CircadiaDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun stateEventDao(): StateEventDao
    abstract fun widgetBindingDao(): WidgetBindingDao
    abstract fun researchDao(): ResearchDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE research_consents ADD COLUMN consent_survey INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE research_consents ADD COLUMN consent_data INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN work_days INTEGER NOT NULL DEFAULT 31")
            }
        }
    }
}
