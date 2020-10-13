package com.github.kam1sh.krait.core.env

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.AbstractTextSource
import com.github.kam1sh.krait.core.misc.castTo

/**
 * Kinda changed PropertiesSource.
 */
class EnvironmentSource(val prefix: String) : AbstractTextSource() {
    private var _entries: MutableMap<String, String>? = null
    private val entries
        get() = _entries ?: throw SourceNotReadyException()

    override fun load(profile: String) = load()

    fun load() = load(System.getenv())

    fun load(env: Map<String, String>) {
        _entries = mutableMapOf()
        _parsedTree = Entry(null)
        env.forEach {
            val key = it.key.toUpperCase()
            if (!key.startsWith(prefix, ignoreCase = true)) {
                return@forEach
            }
            entries[key.toUpperCase()] = it.value
            store(key.toLowerCase().split("__"), it.value)
        }
    }

    override fun <T : Any> find(keys: Keys, cls: Class<T>) = retrieveSimple(keys)?.castTo(cls)

    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)

    override fun list(keys: Keys): List<ConfigNode> = retrieveAdvanced(keys).list()

    private fun retrieveSimple(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix}__${key}"
        return entries[fullKey.toUpperCase()]
    }

    private fun retrieveAdvanced(keys: Keys): Entry {
        var current = parsedTree[prefix.toLowerCase()] ?: throw ValueNotFoundException(keys)
        for (key in keys) {
            val item = current[key.toString().toLowerCase()] ?: throw ValueNotFoundException(keys)
            if (keys.lastIndexOf(key) == keys.size - 1) {
                return item
            } else {
                current = item
            }
        }
        throw ValueNotFoundException(keys)
    }
}
