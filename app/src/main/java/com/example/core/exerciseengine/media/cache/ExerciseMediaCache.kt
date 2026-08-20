package com.example.core.exerciseengine.media.cache

import com.example.core.exerciseengine.media.model.ExerciseMediaReference
import java.util.concurrent.ConcurrentHashMap

data class CachedMediaEntry(
    val mediaId: String,
    val localFilePath: String,
    val sizeBytes: Long,
    val cachedAtTimestamp: Long = System.currentTimeMillis(),
    val expiresAtTimestamp: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000), // 7 dias
    val checksumSha256: String
)

data class CachePolicy(
    val maxCacheSizeBytes: Long = 100L * 1024 * 1024, // 100 MB limite padrão
    val allowAutomaticCellularDownload: Boolean = false,
    val maxAutoDownloadSizeBytes: Long = 5L * 1024 * 1024 // 5 MB
)

/**
 * EVOLUTION HUMAN AI — EXERCISE MEDIA CACHE MANAGER
 *
 * Gerencia cache local de mídias para disponibilidade offline segura.
 * Impede downloads automáticos de arquivos pesados sem autorização explícita do usuário.
 */
object ExerciseMediaCache {

    private val cacheEntries = ConcurrentHashMap<String, CachedMediaEntry>()
    private var policy: CachePolicy = CachePolicy()

    fun getPolicy(): CachePolicy = policy

    fun updatePolicy(newPolicy: CachePolicy) {
        policy = newPolicy
    }

    fun getCachedEntry(mediaId: String): CachedMediaEntry? {
        val entry = cacheEntries[mediaId] ?: return null
        if (System.currentTimeMillis() > entry.expiresAtTimestamp) {
            cacheEntries.remove(mediaId)
            return null
        }
        return entry
    }

    fun putCachedEntry(entry: CachedMediaEntry) {
        // Verifica se ultrapassaria limite do cache
        evictIfNecessary(entry.sizeBytes)
        cacheEntries[entry.mediaId] = entry
    }

    fun isCachedLocally(mediaId: String): Boolean {
        return getCachedEntry(mediaId) != null
    }

    fun getTotalCacheSizeBytes(): Long {
        return cacheEntries.values.sumOf { it.sizeBytes }
    }

    fun canAutoDownload(media: ExerciseMediaReference, isCellular: Boolean): Boolean {
        if (isCellular && !policy.allowAutomaticCellularDownload) {
            return false
        }
        val size = media.fileSize ?: 0L
        return size <= policy.maxAutoDownloadSizeBytes
    }

    private fun evictIfNecessary(incomingBytes: Long) {
        while (getTotalCacheSizeBytes() + incomingBytes > policy.maxCacheSizeBytes && cacheEntries.isNotEmpty()) {
            // Remove o item mais antigo
            val oldestKey = cacheEntries.minByOrNull { it.value.cachedAtTimestamp }?.key
            if (oldestKey != null) {
                cacheEntries.remove(oldestKey)
            } else {
                break
            }
        }
    }

    fun clearAll() {
        cacheEntries.clear()
    }
}
