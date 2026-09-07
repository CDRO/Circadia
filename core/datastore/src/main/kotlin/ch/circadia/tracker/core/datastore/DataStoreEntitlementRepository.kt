package ch.circadia.tracker.core.datastore

import androidx.datastore.core.DataStore
import ch.circadia.tracker.core.domain.EntitlementRepository
import ch.circadia.tracker.core.model.Entitlement
import ch.circadia.tracker.core.model.EntitlementSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreEntitlementRepository(
    private val dataStore: DataStore<EntitlementProto>
) : EntitlementRepository {

    override fun current(): Flow<Entitlement> = dataStore.data.map {
        Entitlement(
            source = when (it.source) {
                EntitlementProto.Source.SUBSCRIPTION -> EntitlementSource.SUBSCRIPTION
                EntitlementProto.Source.RESEARCH -> EntitlementSource.RESEARCH
                else -> EntitlementSource.FREE
            },
            validUntilUtcMillis = if (it.validUntilUtcMillis > 0) it.validUntilUtcMillis else null
        )
    }

    override suspend fun updateEntitlement(entitlement: Entitlement) {
        dataStore.updateData {
            it.toBuilder()
                .setSource(when (entitlement.source) {
                    EntitlementSource.SUBSCRIPTION -> EntitlementProto.Source.SUBSCRIPTION
                    EntitlementSource.RESEARCH -> EntitlementProto.Source.RESEARCH
                    EntitlementSource.FREE -> EntitlementProto.Source.FREE
                })
                .setValidUntilUtcMillis(entitlement.validUntilUtcMillis ?: 0L)
                .build()
        }
    }
}
