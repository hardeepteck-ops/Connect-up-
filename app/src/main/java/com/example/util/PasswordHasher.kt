package com.example.util

import java.security.MessageDigest

object PasswordHasher {
    private const val SALT = "ConnectUp_Secure_Salt_2026_!#"

    fun hash(password: String): String {
        val bytes = (password + SALT).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun verify(password: String, storedHash: String): Boolean {
        return hash(password) == storedHash
    }
}
