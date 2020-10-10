package com.github.kam1sh.krait.yaml

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.misc.JacksonConfigNode
import com.github.kam1sh.krait.misc.decodeTo
import java.io.File

class YamlSource(val filename: String) : ConfigSource {
    val mapper = ObjectMapper(YAMLFactory())
    var _tree: JsonNode? = null
    val tree
        get() = _tree ?: throw SourceNotReadyException()

    override fun load() {
        val file = File(filename)
        if (!file.exists()) throw KraitException("File $filename does not exist.")
        _tree = file.inputStream().use { mapper.readTree(it) }
    }

    override fun <T: Any> get(keys: Keys, cls: Class<T>) = retrieve(keys)?.decodeTo(cls)

    override fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        return get(keys, cls) ?: throw ValueNotFoundException(keys)
    }

    override fun list(keys: Keys): List<ConfigNode> {
        val item = retrieve(keys) ?: throw ValueNotFoundException(keys)
        return JacksonConfigNode(item).list()
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        val item = retrieve(keys) ?: throw ValueNotFoundException(keys)
        return JacksonConfigNode(item).entries(cls)
    }

    private fun retrieve(keys: Keys): JsonNode? {
        var item = tree
        for (key in keys) {
            val realKey = if (key != null) key else "null"
            item = retrieve(item, realKey)
//            item = if (item == null) retrieve(tree, realKey) else retrieve(item, realKey)
        }
        return item
    }

    private fun retrieve(obj: JsonNode, key: Any): JsonNode = when(key) {
        is Int -> obj[key]
        is String -> obj[key]
        else -> obj[key.toString()]
    }
}

