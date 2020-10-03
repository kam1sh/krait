package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.slf4j.LoggerFactory

class Krait(val block: Krait.() -> Unit) {

    private val log = LoggerFactory.getLogger(Krait::class.java)
    private val srcs = ConfigSources()

    fun sources(srcblock: ConfigSources.() -> Unit) = srcs.srcblock()

    fun load() {
        block()
        srcs.map { it.load() }
    }

    fun <T: Any> resolve(keys: Keys, cls: Class<T>): T? {
        for (src in srcs) {
            val value = src.get(keys, cls)
            value?.let { return it }
        }
        return null
    }

    fun <T: Any> resolveWithoutNull(keys: Keys, cls: Class<T>): T {
        for (src in srcs) {
            try {
                val value = src.getWithoutNull(keys, cls)
                log.trace("$src value: $value")
                return value
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        throw ValueNotFoundException(keys)
    }

    fun list(keys: Keys): List<ConfigNode> {
        val result = mutableListOf<ConfigNode>()
        for (src in srcs) {
            try {
                val items = src.list(keys)
                result.addAll(items)
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        return result
    }

    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode> {
        val result = mutableMapOf<T, ConfigNode>()
        for (src in srcs.reversed()) {
            try {
                val items = src.entries(cls)
                result.putAll(items)
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        return result
    }

    fun <T: Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        val result = mutableMapOf<T, ConfigNode>()
        for (src in srcs.reversed()) {
            try {
                val items = src.entries(keys, cls)
                result.putAll(items)
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        return result
    }

    operator fun get(key: Any): ConfigNode {
        return ConfigNode(this, listOf(key))
    }
}