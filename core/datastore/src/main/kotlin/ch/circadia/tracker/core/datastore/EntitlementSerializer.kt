package ch.circadia.tracker.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object EntitlementSerializer : Serializer<EntitlementProto> {
    override val defaultValue: EntitlementProto = EntitlementProto.newBuilder()
        .setSource(EntitlementProto.Source.FREE)
        .setValidUntilUtcMillis(0L)
        .build()

    override suspend fun readFrom(input: InputStream): EntitlementProto {
        try {
            return EntitlementProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: EntitlementProto, output: OutputStream) = t.writeTo(output)
}
