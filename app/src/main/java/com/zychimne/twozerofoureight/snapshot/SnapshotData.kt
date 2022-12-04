package com.zychimne.twozerofoureight.snapshot

import com.google.android.gms.games.snapshot.Snapshot
import java.io.IOException

/**
 * The data in a snapshot.
 */
class SnapshotData(val highScore: Long) {

    fun serialize(): ByteArray {
        return ("$VERSION_NUMBER,$highScore").toByteArray()
    }

    companion object {
        private const val VERSION_NUMBER: Long = 1
        private fun deserialize(bytes: ByteArray?): SnapshotData? {
            val data = listOf(
                *String(
                    bytes!!
                ).split(",").toTypedArray()
            )
            return if (data[0] != VERSION_NUMBER.toString()) {
                null
            } else SnapshotData(data[1].toLong())
        }

        fun deserialize(snapshot: Snapshot): SnapshotData? {
            return try {
                val bytes: ByteArray = snapshot.snapshotContents.readFully()
                deserialize(bytes)
            } catch (ignored: IOException) {
                null
            }
        }
    }
}