package com.example.core.sync

import com.example.core.trainingengine.model.SyncStatus
import java.util.UUID

/**
 * EVOLUTION HUMAN AI — OFFLINE-FIRST SYNCHRONIZATION ENGINE V1
 */

enum class SyncEntityType {
    TRAINING_SESSION,
    WORKOUT_TEMPLATE,
    ATHLETE_BASELINE,
    ATHLETE_GOAL
}

enum class ConflictResolutionStrategy {
    LAST_WRITE_WINS,
    CLIENT_WINS,
    SERVER_WINS,
    MANUAL_MERGE
}

data class SyncQueueItem(
    val queueId: String = UUID.randomUUID().toString(),
    val entityType: SyncEntityType,
    val entityId: String,
    val payloadJson: String,
    val attempts: Int = 0,
    val lastAttemptAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val errorReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

class OfflineSyncManager(
    private val conflictStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LAST_WRITE_WINS
) {
    private val queue = mutableListOf<SyncQueueItem>()

    fun enqueue(entityType: SyncEntityType, entityId: String, payloadJson: String): SyncQueueItem {
        val item = SyncQueueItem(
            entityType = entityType,
            entityId = entityId,
            payloadJson = payloadJson,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        queue.add(item)
        return item
    }

    fun getPendingQueue(): List<SyncQueueItem> {
        return queue.filter { it.syncStatus == SyncStatus.PENDING_SYNC }
    }

    fun markSynced(queueId: String) {
        val index = queue.indexOfFirst { it.queueId == queueId }
        if (index >= 0) {
            queue[index] = queue[index].copy(syncStatus = SyncStatus.SYNCED)
        }
    }

    fun markFailed(queueId: String, reason: String) {
        val index = queue.indexOfFirst { it.queueId == queueId }
        if (index >= 0) {
            val item = queue[index]
            queue[index] = item.copy(
                attempts = item.attempts + 1,
                lastAttemptAt = System.currentTimeMillis(),
                syncStatus = if (item.attempts >= 3) SyncStatus.CONFLICT else SyncStatus.PENDING_SYNC,
                errorReason = reason
            )
        }
    }
}
