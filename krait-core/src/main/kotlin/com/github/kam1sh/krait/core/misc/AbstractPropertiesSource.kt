package com.github.kam1sh.krait.core.misc

import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.slf4j.LoggerFactory
import java.util.*

abstract class AbstractPropertiesSource: AbstractTextSource() {
    private val log = LoggerFactory.getLogger(javaClass)
    protected var _loaded: Properties? = null
    protected val loaded: Properties
        get() = _loaded ?: throw SourceNotReadyException()

    /**
     * Retrieve value by its full path.
     * Accesses only prefixed properties by simple algorithm.
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T> retrieveSimple(keys: Keys, cls: Class<T>): T? {
        val prefixedKeys: Keys = keys
        // format Keys to a prefix.setting.key...
        val stringKeys = prefixedKeys.map { it.toString() }.joinToString(".")
        log.trace("Accessing {}", stringKeys)
        return loaded[stringKeys]?.toString()?.castTo(cls)
    }

    /**
     * Retrieve Entry by its full path.
     * Works with parsed Entry tree.
     */
    protected fun retrieveAdvanced(keys: Keys): Entry {
        var current = parsedTree
        val fullKeys = keys
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