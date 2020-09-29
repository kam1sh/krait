package com.github.kam1sh.krait.core

/**
 * Collection of configuration sources.
 */
class ConfigSources: Iterable<ConfigSource> {
    private val items = mutableListOf<ConfigSource>()

    override fun iterator(): Iterator<ConfigSource> = items.iterator()

    fun add(src: ConfigSource) = items.add(src)
}