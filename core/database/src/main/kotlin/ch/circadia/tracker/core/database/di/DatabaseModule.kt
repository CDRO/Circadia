package ch.circadia.tracker.core.database.di

import android.content.Context
import androidx.room.Room
import ch.circadia.tracker.core.database.CircadiaDatabase
import ch.circadia.tracker.core.database.PersonDao
import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.database.WidgetBindingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CircadiaDatabase = Room.databaseBuilder(
        context,
        CircadiaDatabase::class.java,
        "circadia-database"
    ).build()

    @Provides
    fun providePersonDao(db: CircadiaDatabase): PersonDao = db.personDao()

    @Provides
    fun provideStateEventDao(db: CircadiaDatabase): StateEventDao = db.stateEventDao()

    @Provides
    fun provideWidgetBindingDao(db: CircadiaDatabase): WidgetBindingDao = db.widgetBindingDao()
}
