package com.github.kam1sh.krait.core

interface ConfigSource {
    fun load()
    fun <T: Any> get(keys: Keys, cls: Class<T>): T?
    fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T

    fun list(keys: Keys): List<ConfigNode>

    fun list(): List<ConfigNode> = list(listOf())

    fun <T: Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode>

    /**
     * Like entries(Keys, Class<T>), but for root entries.
     */
    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode> = entries(listOf(), cls)
}