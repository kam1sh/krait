package com.github.kam1sh.krait.core.env

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.castTo

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

    override fun <T : Any> get(keys: Keys, cls: Class<T>): T? {
        val item = retrieve(keys)
        return item?.castTo(cls)
    }

    override fun <T : Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        val item = retrieve(keys) ?: throw ValueNotFoundException(keys)
        return item.castTo(cls)
    }

    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        TODO("Not yet implemented")
    }

    override fun list(keys: Keys): List<ConfigNode> {
        TODO("Not yet implemented")
    }

    private fun retrieve(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix}__${key}"
        return entries[fullKey.toUpperCase()]
    }
}