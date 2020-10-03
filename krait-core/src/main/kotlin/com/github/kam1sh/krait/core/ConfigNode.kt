package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException

/**
 * Node (or branch/leaf) of a configuration tree.
 */
interface ConfigNode {
    fun text() = decodeTo(String::class.java)

    fun long() = decodeTo(Long::class.java)

    fun int() = decodeTo(Int::class.java)

    fun bool() = decodeTo(Boolean::class.java)

    fun isAbsent(): Boolean {
        try {
            text()
            return false
        } catch (exc: ValueNotFoundException) {
            return true
        }
    }

    fun list(): List<ConfigNode>

    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode>

    fun <T: Any> decodeTo(cls: Class<T>): T

    operator fun get(key: Any): ConfigNode

    companion object {
        operator fun invoke(krait: Krait, keys: Keys) = SimpleConfigNode(krait, keys)
    }
}