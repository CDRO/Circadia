package ch.circadia.tracker.core.data.di

import ch.circadia.tracker.core.common.*
import ch.circadia.tracker.core.data.*
import ch.circadia.tracker.core.database.PersonDao
import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.domain.*
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

    @Provides
    @Singleton
    fun provideClock(): Clock = SystemClock()

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    fun provideDeriveIntervalsUseCase(clock: Clock): DeriveIntervalsUseCase =
        DeriveIntervalsUseCase(clock)

    @Provides
    fun provideRecordStateEventUseCase(
        stateEventRepository: StateEventRepository,
        clock: Clock
    ): RecordStateEventUseCase = RecordStateEventUseCase(stateEventRepository, clock)

    @Provides
    fun provideCalculateDailyMetricsUseCase(): CalculateDailyMetricsUseCase =
        CalculateDailyMetricsUseCase()

    @Provides
    fun provideCsvExportUseCase(): CsvExportUseCase = CsvExportUseCase()
}
