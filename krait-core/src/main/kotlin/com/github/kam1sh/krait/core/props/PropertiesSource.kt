package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import org.slf4j.LoggerFactory
import java.util.*

class PropertiesSource(val prefix: String): ConfigSource {
    private val log = LoggerFactory.getLogger(PropertiesSource::class.java)
    var loaded: Properties? = null

    override fun load() = load(System.getProperties())

    fun load(props: Properties) {
        loaded = Properties()
        props.filter { it.key.toString().startsWith(prefix) }.map { loaded!![it.key] = it.value }
        log.debug("Found properties: {}", loaded)
    }

    override fun get(keys: List<Any?>): Any? = retrieve(keys)

    override fun getWithoutNull(keys: List<Any?>): Any = retrieve(keys) ?: ConfigNode.Absent

    private fun retrieve(keys: List<Any?>): Any? {
        if (loaded == null) throw SourceNotReadyException()
        val prefixedKeys: List<Any?> = listOf(prefix) + keys
        val stringKeys = prefixedKeys.map { it.toString() }.joinToString(".")
        log.trace("Accessing {}", stringKeys)
        return loaded!![stringKeys]
    }
}
