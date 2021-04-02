package com.github.kam1sh.krait.core.misc

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.Krait
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException

class SimpleConfigNode(
    private val krait: Krait,
    private val keys: Keys
) : ConfigNode {
    override fun exists() = krait.exists(keys)
    override fun <T: Any> decodeTo(cls: Class<T>): T = krait.find(keys, cls) ?: throw ValueNotFoundException(keys)

    override operator fun get(key: String) = SimpleConfigNode(krait, keys + listOf(key))
    override operator fun get(key: Int) = SimpleConfigNode(krait, keys + listOf(key))

    override fun list(): List<ConfigNode> = krait.list(keys)

    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> = krait.entries(keys, cls)
}