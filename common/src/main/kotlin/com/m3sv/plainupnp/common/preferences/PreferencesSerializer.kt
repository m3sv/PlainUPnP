package com.m3sv.plainupnp.common.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object PreferencesSerializer : Serializer<Preferences> {
    override val defaultValue: Preferences = Preferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Preferences {
        try {
            return Preferences.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: Preferences, output: OutputStream) {
        t.writeTo(output)
    }
}

