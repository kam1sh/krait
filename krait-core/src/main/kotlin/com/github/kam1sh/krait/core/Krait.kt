package com.github.kam1sh.krait.core

import org.slf4j.LoggerFactory

class Krait(val block: Krait.() -> Unit) {

    private val log = LoggerFactory.getLogger(Krait::class.java)
    private val srcs = ConfigSources()

    fun sources(srcblock: ConfigSources.() -> Unit) = srcs.srcblock()

    fun load() {
        block()
        srcs.map { it.load() }
    }

    fun resolve(keys: Keys): Any? {
        for (src in srcs) {
            val value = src[keys]
            value?.let { return it }
        }
        return null
    }

    fun resolveWithoutNull(key: Keys): Any {
        for (src in srcs) {
            val value = src.getWithoutNull(key)
            log.trace("$src value: $value")
            if (value != ConfigNode.Absent) return value
        }
        return ConfigNode.Absent
    }

    operator fun get(key: Any): ConfigNode {
        return ConfigNode(this, listOf(key))
    }
}