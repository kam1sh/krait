package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException

/**
 * Node (or branch/leaf) of a configuration tree.
 */
class ConfigNode(
    private val krait: Krait,
    private val keys: Keys
) {
    fun text(): String {
        val item = retrieve()
        return when(item) {
            is String -> item
            else -> item.toString()
        }
    }

    fun long(): Long {
        val item = retrieve().toString()
        return item.toLongOrNull() ?: throw ValueFormatException(item)
    }

    fun int(): Int {
        val item = retrieve().toString()
        return item.toIntOrNull() ?: throw ValueFormatException(item)
    }

    fun bool(): Boolean {
        val item = retrieve().toString()
        return item.toBoolean()
    }

    fun isAbsent() = rawWithoutNull() == Absent

    fun raw(): Any? = krait.resolve(keys)

    fun rawWithoutNull(): Any = krait.resolveWithoutNull(keys)

    operator fun get(key: Any) = ConfigNode(krait, keys + listOf(key))

    object Absent

    private fun retrieve(): Any {
        val rawVal = rawWithoutNull()
        if (rawVal == Absent) throw ValueNotFoundException(keys)
        return rawVal
    }
}