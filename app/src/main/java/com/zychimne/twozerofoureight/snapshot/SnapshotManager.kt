package com.zychimne.twozerofoureight.snapshot

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Games
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

/**
 * Manages the Google Cloud Save snapshot.
 */
object SnapshotManager {
    private var saveInProgress = false
    private var lastSnapshot: SnapshotData? = null
    private const val FILE_NAME = "2048"

    // TODO: Move this to a resource.
    private const val FILE_DESCRIPTION = "2048 high score save."
    fun loadSnapshot(context: Context, callback: Callback) {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val snapshotsClient: SnapshotsClient = Games.getSnapshotsClient(context, account)
            snapshotsClient.open(
                FILE_NAME,
                true,
                SnapshotsClient.RESOLUTION_POLICY_HIGHEST_PROGRESS
            )
                .addOnCompleteListener(object :
                    OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot?>?> {
                    override fun onComplete(task: Task<SnapshotsClient.DataOrConflict<Snapshot?>?>) {
                        val snapshot: Snapshot = getSnapshotFromTask(task) ?: return
                        val data: SnapshotData? = SnapshotData.deserialize(snapshot)
                        if (data != null) {
                            callback.run(data)
                        }
                    }
                })
        }
    }

    fun saveSnapshot(context: Context, data: SnapshotData) {
        lastSnapshot = data
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            if (saveInProgress) {
                return
            }
            saveInProgress = true
            val snapshotsClient: SnapshotsClient = Games.getSnapshotsClient(context, account)
            snapshotsClient.open(
                FILE_NAME,
                true,
                SnapshotsClient.RESOLUTION_POLICY_HIGHEST_PROGRESS
            )
                .addOnCompleteListener(object :
                    OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot?>?> {
                    override fun onComplete(task: Task<SnapshotsClient.DataOrConflict<Snapshot?>?>) {
                        saveInProgress = false
                        val snapshot: Snapshot = getSnapshotFromTask(task) ?: return
                        val dataToSave = lastSnapshot

                        // Do not overwrite a higher high-score.
                        val originalData: SnapshotData? =
                            SnapshotData.deserialize(snapshot)
                        if (originalData != null && dataToSave != null && originalData.highScore > dataToSave.highScore) {
                            return
                        }
                        if (dataToSave != null) {
                            snapshot.snapshotContents.writeBytes(dataToSave.serialize())
                            val metadataChange: SnapshotMetadataChange =
                                SnapshotMetadataChange.Builder()
                                    .setDescription(FILE_DESCRIPTION)
                                    .setProgressValue(dataToSave.highScore)
                                    .build()
                            snapshotsClient.commitAndClose(snapshot, metadataChange)
                        }
                    }
                })
        }
    }

    private fun getSnapshotFromTask(task: Task<SnapshotsClient.DataOrConflict<Snapshot?>?>): Snapshot? {
        if (!task.isSuccessful) {
            return null
        }
        return task.result?.data
    }

    interface Callback {
        fun run(data: SnapshotData)
    }
}
