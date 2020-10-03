package com.github.kam1sh.krait.core

interface ConfigSource {
    fun load()
    fun <T: Any> get(keys: Keys, cls: Class<T>): T?
    fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T

    fun list(keys: Keys): List<ConfigNode>
    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode>
    fun <T: Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode>
}