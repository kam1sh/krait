package com.github.kam1sh.krait.core.dotenv

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.env.EnvironmentSource
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.castTo
import org.slf4j.LoggerFactory
import java.io.File

// TODO create base source common for Properties, Environment and Dotenv


class DotenvSource(val prefix: String, val recursive: Boolean = false) : ConfigSource {
    private val log = LoggerFactory.getLogger(javaClass)
    private val entries = mutableMapOf<String, String>()
    private val parsedTree = Entry(null)

    override fun load() {
        val file = discoverFile()
        load(file)
    }

    fun load(file: File) {
        file.forEachLine {
            val bits = it.split('=')
            val key = bits[0]
            val value = if (bits.size > 2) {
                bits.subList(1, bits.size).joinToString("=")
            } else {
                bits[1]
            }
            if (!key.startsWith(prefix, ignoreCase = true)) {
                return@forEachLine
            }
            entries[key.toUpperCase()] = value
            store(key.toUpperCase().split("__"), value)
        }
        log.debug("Entries: {}", entries)
        log.debug("Parsed tree: {}", parsedTree)
    }

    override fun <T: Any> get(keys: Keys, cls: Class<T>): T? {
        val item = retrieveSimple(keys)
        return item?.castTo(cls)
    }

    override fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        val item = retrieveSimple(keys) ?: throw ValueNotFoundException(keys)
        return item.castTo(cls)
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)

    override fun list(keys: Keys) = retrieveAdvanced(keys).list()

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

    private fun discoverFile(dir: File? = null): File {
        val dirOrCwd = dir ?: File(System.getProperty("user.dir"))
        if (dirOrCwd.isFile) throw IllegalArgumentException("Got file instead of directory.")
        for (file in dirOrCwd.listFiles()!!) {
            if (file.isFile) continue
            if (file.name == ".env") return file
        }
        if (recursive) {
            return discoverFile(dirOrCwd.parentFile)
        }
        else {
            throw KraitException("Dotenv file not found.")
        }
    }

    class Entry(var value: String?) {
        val listMap = mutableMapOf<Int, DotenvConfigNode>()
        val childMap = mutableMapOf<String, Entry>()

        @Suppress("UNCHECKED_CAST")
        fun <T> configNodes(cls: Class<T>): Map<T, ConfigNode> {
            when {
                cls == String::class.java -> {
                    val result = mutableMapOf<String, ConfigNode>()
                    for (item in childMap) {
                        result[item.key.toLowerCase()] = DotenvConfigNode(item.value)
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
                    listMap[num] = DotenvConfigNode(value)
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

class DotenvConfigNode(val entry: DotenvSource.Entry): ConfigNode {
    override fun list() = entry.list()

    override fun get(key: Any): ConfigNode {
        return DotenvConfigNode(entry[key.toString().toUpperCase()] ?: throw ValueNotFoundException(listOf(key)))
    }

    override fun <T : Any> decodeTo(cls: Class<T>) = entry.value?.castTo(cls) ?: throw ValueNotFoundException(listOf())
    override fun <T : Any> entries(cls: Class<T>) = entry.configNodes(cls)
}
