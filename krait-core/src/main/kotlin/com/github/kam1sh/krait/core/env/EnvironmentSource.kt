package com.github.kam1sh.krait.core.env

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys

class EnvironmentSource(val prefix: String) : ConfigSource {
    private val entries = mutableMapOf<String, String>()

    override fun load() = load(System.getenv())

    fun load(env: Map<String, String>) {
        env.forEach {
            if (!it.key.startsWith(prefix, ignoreCase = true)) {
                return@forEach
            }
            entries[it.key.toUpperCase()] = it.value
        }
    }

    override fun get(keys: Keys): Any? = retrieve(keys)

    override fun getWithoutNull(keys: Keys): Any = retrieve(keys) ?: ConfigNode.Absent

    private fun retrieve(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix}__${key}"
        return entries[fullKey.toUpperCase()]
    }
}