package com.github.kam1sh.krait.core

class SimpleConfigNode(
    private val krait: Krait,
    private val keys: Keys
) : ConfigNode {
    override fun <T: Any> decodeTo(cls: Class<T>): T = krait.resolveWithoutNull(keys, cls)

    override operator fun get(key: Any) = SimpleConfigNode(krait, keys + listOf(key))

    override fun list(): List<ConfigNode> = krait.list(keys)

    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> = krait.entries(keys, cls)
}