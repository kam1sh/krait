package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.castTo
import org.slf4j.LoggerFactory
import java.util.*

// TODO create base source common for Properties, Environment and Dotenv

/**
 * JVM properties source.
 * prefix - first key in properties to filter them
 */
class PropertiesSource(val prefix: String): ConfigSource {
    private val log = LoggerFactory.getLogger(PropertiesSource::class.java)

    // filtered properties from .load(props)
    private var _loaded: Properties? = null
    private val loaded: Properties
        get() = _loaded ?: throw SourceNotReadyException()

    // parsed tree from .load(props)
    private var _parsedProps: Entry? = null
    private val parsedProps
        get() = _parsedProps ?: throw SourceNotReadyException()

    /**
     * Load properties from System.getProperties().
     */
    override fun load() = load(System.getProperties())

    /**
     * Load properties from a props.
     */
    fun load(props: Properties) {
        _loaded = Properties()
        _parsedProps = Entry(null)
        props.filter { it.key.toString().startsWith(prefix) }.forEach {
            loaded[it.key] = it.value
            store(it.key.toString().split('.'), it.value)
        }
        log.debug("Found properties: {}", loaded)
        log.debug("Parsed tree: {}", parsedProps)
    }

    /**
     * Get value or null of type T by its key.
     */
    override fun <T : Any> get(keys: Keys, cls: Class<T>): T? = retrieveSimple(keys)

    /**
     * Get value of type T by its key
     * @throws ValueNotFoundException of key was not found.
     */
    override fun <T : Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        return retrieveSimple(keys) ?: throw ValueNotFoundException(keys)
    }

    /**
     * Get list of properties.
     * Usage:
     * app.loggers.0.name = myapp
     * app.loggers.0.level = info
     * app.loggers.1.name = framework
     * app.loggers.1.level = warn
     *
     * val src = PropertiesSource("app")
     * src.load()
     * src.list(listOf("loggers")) // [{name=myapp, level=info}, {name=framework, level=warn}]
     */
    override fun list(keys: Keys) = retrieveAdvanced(keys).list()

    /**
     * Get meo of of nodes by a key.
     * Usage:
     * app.months.jan = 1
     * app.months.feb = 2
     * app.months.mar = 3
     *
     * val src = PropertiesSource("app")
     * src.load()
     * src.entries(listOf("months")) // {jan=ConfigNode(1), feb=ConfigNode(2), mar=ConfigNode(3)}
     */
    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)

    /**
     * put value by keys path
     */
    private fun store(keys: List<String>, value: Any) {
        log.debug("Setting {}={}", keys, value)
        var current = parsedProps
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
     * Retrieve value by its full path.
     * Accesses only prefixed properties by simple algorithm.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> retrieveSimple(keys: Keys): T? {
        val prefixedKeys: Keys = listOf(prefix) + keys
        // format Keys to a prefix.setting.key...
        val stringKeys = prefixedKeys.map { it.toString() }.joinToString(".")
        log.trace("Accessing {}", stringKeys)
        return loaded[stringKeys] as T
    }

    /**
     * Retrieve Entry by its full path.
     * Works with parsed Entry tree.
     */
    private fun retrieveAdvanced(keys: Keys): Entry {
        var current = parsedProps
        val fullKeys = listOf(prefix) + keys
        for (key in fullKeys) {
            val item = current[key.toString()] ?: throw ValueNotFoundException(keys)
            if (fullKeys.lastIndexOf(key) == fullKeys.size - 1) {
                return item
            } else {
                current = item
            }
        }
        throw ValueNotFoundException(keys)
    }

    /**
     * Element of a configuration tree.
     * Has value field, list and map of child entries.
     */
    class Entry(var value: Any?) {
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
        val listMap = mutableMapOf<Int, PropertiesConfigNode>()
        val childMap = mutableMapOf<String, Entry>()

        /**
         * Converts map of Entries to a map of ConfigNodes.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> configNodes(cls: Class<T>): Map<T, ConfigNode> {
            val result = mutableMapOf<T, ConfigNode>()
            for (item in childMap) {
                result[item.key as T] = PropertiesConfigNode(item.value)
            }
            return result
        }

        /**
         * Converts map of unordered entries to a ConfigNodes.
         * Implementation detail: empty values in original map are removed
         * ({0: first, 1: second, 3: fourth} -> [first, second, fourth]}
         */
        fun list(): List<ConfigNode> {
            val result = mutableListOf<ConfigNode>()
            for (item in listMap.keys.sorted()) {
                result.add(listMap[item]!!)
            }
            return result
        }

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
                    listMap[num] = PropertiesConfigNode(value)
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

        /**
         * Get value by string number (listMap) or by a string key (childMap).
         */
        operator fun get(key: String): Entry? {
            val num = key.toIntOrNull()
            if (num != null) {
                return listMap[num]?.entry
            }
            return childMap[key]
        }
    }
}

/**
 * Entry wrapper that implements ConfigNode interface.
 */
class PropertiesConfigNode(val entry: PropertiesSource.Entry) : ConfigNode {
    override fun list() = entry.list()

    override fun get(key: Any): ConfigNode {
        return PropertiesConfigNode(entry[key.toString()] ?: throw ValueNotFoundException(listOf(key)))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> decodeTo(cls: Class<T>): T {
        return when (entry.value ?: throw ValueNotFoundException(listOf())) {
            is List<*> -> throw ValueFormatException(entry.value.toString())
            is Map<*, *> -> throw ValueFormatException(entry.value.toString())
            else -> entry.value as? T ?: entry.value.toString().castTo(cls) // TODO toString().castTo(cls) seems kinda dirty for me...
        }
    }

    override fun <T : Any> entries(cls: Class<T>) = entry.configNodes(cls)
}