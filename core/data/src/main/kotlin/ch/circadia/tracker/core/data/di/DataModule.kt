package ch.circadia.tracker.core.data.di

import ch.circadia.tracker.core.data.*
import ch.circadia.tracker.core.database.PersonDao
import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.domain.PersonRepository
import ch.circadia.tracker.core.domain.StateEventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePersonRepository(
        personDao: PersonDao
    ): PersonRepository = OfflinePersonRepository(personDao)

    @Provides
    @Singleton
    fun provideStateEventRepository(
        stateEventDao: StateEventDao
    ): StateEventRepository = OfflineStateEventRepository(stateEventDao)
}
