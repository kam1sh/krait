package com.github.kam1sh.krait.core.misc

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.slf4j.LoggerFactory

abstract class AbstractTextSource : ConfigSource {
    private val log = LoggerFactory.getLogger(javaClass)

    // parsed tree from .load(...)
    protected var _parsedTree: Entry? = null
    protected val parsedTree
        get() = _parsedTree ?: throw SourceNotReadyException()

    /**
     * put value by keys path
     */
    protected fun store(keys: List<String>, value: String) {
        log.debug("Setting {}={}", keys, value)
        var current = parsedTree
        for (key in keys) {
            // latest key?
            if (keys.lastIndexOf(key) == keys.size - 1) {
                // check key so we won't override it
                // there could be cases like
                // store([app, nested, key], value)
                // store([app, nested], value2)
                // store([app, nested, key2], value3)
                // this check is required so second call won't override first call value
                if (current.childMap.containsKey(key)) {
                    // setting value without overriding whole structure
                    current[key]!!.value = value
                } else {
                    // creating new entry
                    current[key] = Entry(value)
                }
            } else {
                if (!current.childMap.containsKey(key)) current[key] = Entry(null)
                current = current[key]!!
            }
        }

    }


    /**
     * Element of a configuration tree.
     * Has value field, list and map of child entries.
     */
    class Entry(var value: String?) {
        val childMap = mutableMapOf<String, Entry>()
        // why it's a map, not list? two reasons:
        // 1. properties are coming in a unordered way, i.e.
        //      app.list.1.name = name1
        //      app.list.0.name = name0
        //      app.list.2.name = name2
        //    and by using map their keys could be sorted later
        // 2. some inputs could be sparsed, i.e.
        //      app.list.0.name = name1
        //      app.list.2.name = name2
        //    and map helps with solving that issue
        val listMap = mutableMapOf<Int, Entry>()

        /**
         * Smart set: you can set string number, and value will be added to a list,
         * or it will be added to a map
         */
        operator fun set(key: String, value: Entry): Entry? {
            // internal assert
            if (childMap.containsKey(key)) throw IllegalArgumentException("Key $key has already been set.")
            // number? working with listMap!
            val num = key.toIntOrNull()
            if (num != null) {
                // there is no such key? creating one!
                if (!listMap.containsKey(num)) {
                    listMap[num] = value
                } else {
                    // key exists? merging childMap and listMaps-s!
                    for (item in value.childMap) {
                        listMap[num]!!.childMap[item.key] = item.value
                    }
                    for (item in value.listMap) {
                        listMap[num]!!.listMap[item.key] = item.value
                    }
                }
            } else {
                // any other string? just put it in a childMap
                childMap.put(key, value)
            }
            return value
        }

        /**
         * Get value by string number (listMap) or by a string key (childMap).
         */
        operator fun get(key: String): Entry? {
            val num = key.toIntOrNull()
            if (num != null) {
                return listMap[num]
            }
            return childMap[key]
        }

        fun exists() = listMap.isNotEmpty() || childMap.isNotEmpty() || value != null

        /**
         * Converts map of Entries to a map of ConfigNodes.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> configNodes(cls: Class<T>): Map<T, ConfigNode> {
            if (cls != String::class.java) return mapOf()
            val result = mutableMapOf<String, ConfigNode>()
            for (item in childMap) {
                result[item.key] = TextualConfigNode(item.value)
            }
            return result as Map<T, ConfigNode>
        }

        fun list(): List<ConfigNode> {
            val result = mutableListOf<ConfigNode>()
            for (item in listMap.keys.sorted()) {
                result.add(TextualConfigNode(listMap[item]!!))
            }
            return result
        }

        override fun toString(): String {
            return "${javaClass.name}(value=$value, list=$listMap, map=$childMap)"
        }
    }
}

/**
 * Entry wrapper that implements ConfigNode interface.
 */
class TextualConfigNode(val entry: AbstractTextSource.Entry) : ConfigNode {
    override fun exists() = entry.value != null
    override fun <T : Any> decodeTo(cls: Class<T>): T {
        val item = entry.value ?: throw ValueNotFoundException(listOf())
        return item.castTo(cls)
    }
    override fun <T : Any> entries(cls: Class<T>) = entry.configNodes(cls)
    override fun list(): List<ConfigNode> = entry.list()
    override fun get(key: String): ConfigNode {
        val item = entry[key] ?: throw ValueNotFoundException(listOf(key))
        return TextualConfigNode(item)
    }

    override fun get(key: Int): ConfigNode {
        val item = entry[key.toString()] ?: throw ValueNotFoundException(listOf(key))
        return TextualConfigNode(item)
    }

    override fun toString(): String {
        return "${javaClass.name}(entry=$entry)"
    }
}
