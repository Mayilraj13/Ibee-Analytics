package com.example.photo_organizer.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object FileCrypto {
    private const val ITERATIONS = 10000
    private const val KEY_SIZE = 256
    private val random = SecureRandom()

    fun encrypt(plain: ByteArray, passphrase: String): ByteArray {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val iv = ByteArray(16)
        random.nextBytes(iv)

        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val cipherBytes = cipher.doFinal(plain)

        // format: salt + iv + ciphertext
        return salt + iv + cipherBytes
    }

    fun decrypt(data: ByteArray, passphrase: String): ByteArray {
        if (data.size < 32) throw IllegalArgumentException("Invalid data")
        val salt = data.copyOfRange(0, 16)
        val iv = data.copyOfRange(16, 32)
        val cipherBytes = data.copyOfRange(32, data.size)

        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return cipher.doFinal(cipherBytes)
    }

    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_SIZE)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }
}
