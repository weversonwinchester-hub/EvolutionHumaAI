package com.example.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PERFORMAI SECURITY LAYER — PASSWORD HASHER V2
 *
 * Implementa padrão de segurança PBKDF2-HMAC-SHA512 com salt individual criptográfico (128-bit)
 * e 120.000 iterações, com retrocompatibilidade transparente e lazy rehash para hashes legados SHA-256.
 */
object PasswordHasher {
    private const val LEGACY_SALT = "PERFORMAI_EVOLUTION_FOUNDATION_SALT_v1"
    private const val ALGORITHM = "PBKDF2WithHmacSHA512"
    private const val FALLBACK_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val PREFIX_V2 = "\$pbkdf2-sha512\$i="

    fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        val hash = derivePbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hash.joinToString("") { "%02x".format(it) }
        return "$PREFIX_V2$ITERATIONS\$$saltHex\$$hashHex"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || password.isBlank()) return false

        if (storedHash.startsWith(PREFIX_V2)) {
            return try {
                val parts = storedHash.removePrefix(PREFIX_V2).split("$")
                if (parts.size != 3) return false
                val iterations = parts[0].toIntOrNull() ?: return false
                val salt = hexToBytes(parts[1])
                val expectedHashHex = parts[2]
                val computedHash = derivePbkdf2(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
                val computedHashHex = computedHash.joinToString("") { "%02x".format(it) }
                constantTimeEquals(computedHashHex, expectedHashHex)
            } catch (e: Exception) {
                false
            }
        }

        // Retrocompatibilidade: verificação de hash legado v1 (SHA-256 com salt global)
        return constantTimeEquals(hashLegacy(password), storedHash)
    }

    fun needsRehash(storedHash: String): Boolean {
        return !storedHash.startsWith(PREFIX_V2)
    }

    private fun hashLegacy(password: String): String {
        val input = "$LEGACY_SALT:$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun derivePbkdf2(password: CharArray, salt: ByteArray, iterations: Int, keyLengthBits: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, keyLengthBits)
        val factory = try {
            SecretKeyFactory.getInstance(ALGORITHM)
        } catch (e: Exception) {
            SecretKeyFactory.getInstance(FALLBACK_ALGORITHM)
        }
        return factory.generateSecret(spec).encoded
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}
