package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.AbstractTextSource
import org.slf4j.LoggerFactory
import java.util.*

/**
 * JVM properties source.
 * prefix - first key in properties to filter them
 */
class PropertiesSource(val prefix: String): AbstractTextSource() {
    private val log = LoggerFactory.getLogger(PropertiesSource::class.java)

    // filtered properties from .load(props)
    private var _loaded: Properties? = null
    private val loaded: Properties
        get() = _loaded ?: throw SourceNotReadyException()

    override fun load(profile: String) = load()

    /**
     * Load properties from System.getProperties().
     */
    fun load() = load(System.getProperties())

    /**
     * Load properties from a props.
     */
    fun load(props: Properties) {
        _loaded = Properties()
        _parsedTree = Entry(null)
        props.filter { it.key.toString().startsWith(prefix) }.forEach {
            loaded[it.key] = it.value
            store(it.key.toString().split('.'), it.value.toString())
        }
        log.debug("Found properties: {}", loaded)
        log.debug("Parsed tree: {}", parsedTree)
    }

    /**
     * Get value or null of type T by its key.
     */
    override fun <T : Any> find(keys: Keys, cls: Class<T>): T? = retrieveSimple(keys)

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
        var current = parsedTree
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

}
