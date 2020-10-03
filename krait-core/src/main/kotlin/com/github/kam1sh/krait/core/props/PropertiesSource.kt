package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.castTo
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap

class PropertiesSource(val prefix: String): ConfigSource {
    private val log = LoggerFactory.getLogger(PropertiesSource::class.java)
    private var loaded: Properties? = null
    private val parsedProps = mutableMapOf<String, Entry>()

    override fun load() = load(System.getProperties())

    fun load(props: Properties) {
        loaded = Properties()
        props.filter { it.key.toString().startsWith(prefix) }.forEach {
            loaded!![it.key] = it.value
            store(it.key.toString().split('.'), it.value)
        }
        log.debug("Found properties: {}", loaded)
        log.debug("Parsed tree: {}", parsedProps)
    }

    override fun <T : Any> get(keys: Keys, cls: Class<T>): T? = retrieve(keys)

    override fun <T : Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        return retrieve(keys) ?: throw ValueNotFoundException(keys)
    }

    override fun list(keys: Keys): List<ConfigNode> {
        throw ValueNotFoundException(keys) // TODO don't support rn
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        var current = parsedProps
        val fullKeys = listOf(prefix) + keys
        for (key in fullKeys) {
            val item = current[key] ?: throw ValueNotFoundException(keys)
            if (fullKeys.lastIndexOf(key) == fullKeys.size - 1) {
                return item.configNodes(cls)
            } else {
                current = current[key] ?: throw ValueNotFoundException(keys)
            }
        }
        throw ValueNotFoundException(keys)
    }

    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> {
        return entries(listOf(), cls)
    }

    private fun store(keys: List<String>, value: Any) {
        var current = parsedProps
         for (key in keys) {
            if (keys.lastIndexOf(key) == keys.size - 1) {
                if (current.containsKey(key)) {
                    current[key]!!.value = value
                } else  {
                    current[key] = Entry(value)
                }
            } else {
                if (!current.containsKey(key)) current[key] = Entry(null)
                current = current[key]!!
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> retrieve(keys: List<Any?>): T? {
        if (loaded == null) throw SourceNotReadyException()
        val prefixedKeys: List<Any?> = listOf(prefix) + keys
        val stringKeys = prefixedKeys.map { it.toString() }.joinToString(".")
        log.trace("Accessing {}", stringKeys)
        return loaded!![stringKeys] as T
    }

    class Entry(value: Any?): HashMap<String, Entry>() {
        var value: Any? = null
        init {
            when(value) {
                is Map<*, *> -> value.forEach {this[it.key.toString()] = Entry(it.value!!)}
                else -> this.value = value
            }
        }
        fun <T> configNodes(cls: Class<T>): Map<T, ConfigNode> {
            if (cls != String::class.java) return mapOf()
            val result = mutableMapOf<T, ConfigNode>()
            for (item in this) {
                result[item.key as T] = PropertiesConfigNode(item.value)
            }
            return result
        }

        override fun put(key: String, value: Entry): Entry? {
            if (containsKey(key)) throw IllegalArgumentException("Key $key has already been set.")
            return super.put(key, value)
        }
    }
}

class PropertiesConfigNode(val entry: PropertiesSource.Entry) : ConfigNode {
    override fun list(): List<ConfigNode> {
        return listOf()
    }

    override fun get(key: Any): ConfigNode {
        return PropertiesConfigNode(entry[key.toString()] ?: throw ValueNotFoundException(listOf(key)))
    }

    override fun <T : Any> decodeTo(cls: Class<T>): T {
        return when (entry.value ?: throw ValueNotFoundException(listOf())) {
            is List<*> -> throw ValueFormatException(entry.value.toString())
            is Map<*, *> -> throw ValueFormatException(entry.value.toString())
            else -> entry.value as? T ?: entry.value.toString().castTo(cls)
        }
    }

    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> {
        return entry.configNodes(cls)
    }
}