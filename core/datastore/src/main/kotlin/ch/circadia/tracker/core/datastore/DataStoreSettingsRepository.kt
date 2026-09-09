package ch.circadia.tracker.core.datastore

import androidx.datastore.core.DataStore
import ch.circadia.tracker.core.domain.SettingsRepository
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<SettingsProto>
) : SettingsRepository {

    override fun getDayBoundary(): Flow<LocalTime> = dataStore.data.map {
        LocalTime.of(it.dayBoundaryHour, it.dayBoundaryMinute)
    }

    override suspend fun setDayBoundary(time: LocalTime) {
        dataStore.updateData {
            it.toBuilder()
                .setDayBoundaryHour(time.hour)
                .setDayBoundaryMinute(time.minute)
                .build()
        }
    }

    override fun getUseDoublePlot(): Flow<Boolean> = dataStore.data.map {
        it.useDoublePlot
    }

    override suspend fun setUseDoublePlot(use: Boolean) {
        dataStore.updateData {
            it.toBuilder()
                .setUseDoublePlot(use)
                .build()
        }
    }

    override fun getUseSideBySide(): Flow<Boolean> = dataStore.data.map {
        it.useSideBySide
    }

    override suspend fun setUseSideBySide(use: Boolean) {
        dataStore.updateData {
            it.toBuilder()
                .setUseSideBySide(use)
                .build()
        }
    }

    override fun getLastUploadedEventRecordedAt(): Flow<Long> = dataStore.data.map {
        it.lastUploadedEventRecordedAt
    }

    override suspend fun setLastUploadedEventRecordedAt(time: Long) {
        dataStore.updateData {
            it.toBuilder()
                .setLastUploadedEventRecordedAt(time)
                .build()
        }
    }

    override fun isRevocationPending(): Flow<Boolean> = dataStore.data.map {
        it.pendingRevocation
    }

    override suspend fun setRevocationPending(pending: Boolean) {
        dataStore.updateData {
            it.toBuilder()
                .setPendingRevocation(pending)
                .build()
        }
    }
}
