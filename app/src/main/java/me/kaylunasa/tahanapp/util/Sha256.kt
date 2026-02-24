package me.kaylunasa.tahanapp.util

import java.security.MessageDigest

fun String.sha256() : String {
    val bytes = this.toByteArray(Charsets.UTF_8)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") {"%02x".format(it)}
}