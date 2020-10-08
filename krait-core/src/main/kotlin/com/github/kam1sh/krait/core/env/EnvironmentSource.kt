package com.github.kam1sh.krait.core.env

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.castTo

// TODO create base source common for Properties, Environment and Dotenv

/**
 * Kinda changed PropertiesSource.
 */
class EnvironmentSource(val prefix: String) : ConfigSource {
    private val entries = mutableMapOf<String, String>()
    private val parsedTree = Entry(null)

    override fun load() = load(System.getenv())

    fun load(env: Map<String, String>) {
        env.forEach {
            val key = it.key.toUpperCase()
            if (!key.startsWith(prefix, ignoreCase = true)) {
                return@forEach
            }
            entries[key.toUpperCase()] = it.value
            store(key.split("__"), it.value)
        }
    }

    override fun <T : Any> get(keys: Keys, cls: Class<T>) = retrieveSimple(keys)?.castTo(cls)

    override fun <T : Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        return retrieveSimple(keys)?.castTo(cls) ?: throw ValueNotFoundException(keys)
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)

    override fun list(keys: Keys): List<ConfigNode> = retrieveAdvanced(keys).list()

    private fun retrieveSimple(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix}__${key}"
        return entries[fullKey.toUpperCase()]
    }

    private fun retrieveAdvanced(keys: Keys): Entry {
        var current = parsedTree[prefix] ?: throw ValueNotFoundException(keys)
        for (key in keys) {
            val item = current[key.toString().toUpperCase()] ?: throw ValueNotFoundException(keys)
            if (keys.lastIndexOf(key) == keys.size - 1) {
                return item
            } else {
                current = item
            }
        }
        throw ValueNotFoundException(keys)
    }

    private fun store(keys: List<String>, value: String) {
        var current = parsedTree
        for (key in keys) {
            if (keys.lastIndexOf(key) == keys.size - 1) {
                if (current.childMap.containsKey(key)) {
                    current[key]!!.value = value
                } else {
                    current[key] = Entry(value)
                }
            } else {
                if (!current.childMap.containsKey(key)) current[key] = Entry(null)
                current = current[key]!!
            }
        }
    }

    class Entry(var value: String?) {
        val listMap = mutableMapOf<Int, EnvironmentConfigNode>()
        val childMap = mutableMapOf<String, Entry>()

        @Suppress("UNCHECKED_CAST")
        fun <T> configNodes(cls: Class<T>): Map<T, ConfigNode> {
            when {
                cls == String::class.java -> {
                    val result = mutableMapOf<String, ConfigNode>()
                    for (item in childMap) {
                        result[item.key.toLowerCase()] = EnvironmentConfigNode(item.value)
                    }
                    return result as Map<T, ConfigNode>
                }
                else -> return mapOf()
            }
        }

        fun list(): List<ConfigNode> {
            val result = mutableListOf<ConfigNode>()
            for (item in listMap.keys.sorted()) {
                result.add(listMap[item]!!)
            }
            return result
        }

        operator fun set(key: String, value: Entry): Entry? {
            // internal assert
            if (childMap.containsKey(key)) throw IllegalArgumentException("Key $key has already been set.")
            // number? working with listMap!
            val num = key.toIntOrNull()
            if (num != null) {
                // there is no such key? creating one!
                if (!listMap.containsKey(num)) {
                    listMap[num] = EnvironmentConfigNode(value)
                } else {
                    // key exists? merging childMap and listMaps-s!
                    for (item in value.childMap) {
                        listMap[num]!!.entry.childMap[item.key] = item.value
                    }
                    for (item in value.listMap) {
                        listMap[num]!!.entry.listMap[item.key] = item.value
                    }
                }
            } else {
                // any other string? just put it in a childMap
                childMap.put(key, value)
            }
            return value
        }

        operator fun get(key: String): Entry? {
            val num = key.toIntOrNull()
            if (num != null) {
                return listMap[num]?.entry
            }
            return childMap[key]
        }
    }
}

class EnvironmentConfigNode(val entry: EnvironmentSource.Entry): ConfigNode {
    override fun list() = entry.list()

    override fun get(key: Any): ConfigNode {
        return EnvironmentConfigNode(entry[key.toString().toUpperCase()] ?: throw ValueNotFoundException(listOf(key)))
    }

    override fun <T : Any> decodeTo(cls: Class<T>) = entry.value?.castTo(cls) ?: throw ValueNotFoundException(listOf())
    override fun <T : Any> entries(cls: Class<T>) = entry.configNodes(cls)
}
