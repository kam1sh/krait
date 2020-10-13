package com.github.kam1sh.krait.yaml

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.misc.JacksonConfigNode
import com.github.kam1sh.krait.misc.decodeTo
import org.slf4j.LoggerFactory
import java.io.File

class YamlSource(val name: String) : ConfigSource {
    private val log = LoggerFactory.getLogger(javaClass)

    private val mapper = ObjectMapper(YAMLFactory())
    private var _tree: JsonNode? = null
    private val tree
        get() = _tree ?: throw SourceNotReadyException()

    private var _profileTree: JsonNode? = null

    override fun load(profile: String) {
        val fileNames = listOf("$name.yaml", "$name.yml", "$name-$profile.yaml", "$name-$profile.yml")
        for (fileName in fileNames) {
            val file = File(fileName)
            if (!file.exists()) {
                continue
            }
            val parsedFile = file.inputStream().use { mapper.readTree(it) }
            if (_tree == null) {
                log.info("Loading {} as primary", file)
                _tree = parsedFile
            } else {
                log.info("Loading {} as profile", file)
                _profileTree = parsedFile
            }
        }
    }

    override fun <T: Any> find(keys: Keys, cls: Class<T>) = retrieveFromBoth(keys)?.decodeTo(cls)

    override fun list(keys: Keys): List<ConfigNode> {
        val item = retrieve(keys) ?: throw ValueNotFoundException(keys)
        val out = JacksonConfigNode(item).list()
        _profileTree?.let {
            it.retrieve(keys)?.let { out.addAll(JacksonConfigNode(it).list()) }
        }
        return out
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        val item = retrieve(keys) ?: throw ValueNotFoundException(keys)
        val out = JacksonConfigNode(item).entries(cls)
        _profileTree?.let {
            it.retrieve(keys)?.let {
                val tmp = JacksonConfigNode(it).entries(cls)
                for (entry in tmp) {
                    out[entry.key] = entry.value
                }
            }
        }
        return out
    }

    private fun retrieveFromBoth(keys: Keys): JsonNode? {
        var out = retrieve(keys)
        if (_profileTree != null) out = _profileTree!!.retrieve(keys)
        return out
    }

    private fun retrieve(keys: Keys): JsonNode? {
        if (_tree == null) throw ValueNotFoundException(keys)
        return tree.retrieve(keys)
    }

    private fun JsonNode.retrieve(keys: Keys): JsonNode? {
        try {
            var item = this
            for (key in keys) {
                item = retrieve(item, key)
            }
            return item
        } catch (exc: Exception) {
            return null
        }
    }

    private fun retrieve(obj: JsonNode, key: Any): JsonNode = when(key) {
        is Int -> obj[key]
        is String -> obj[key]
        else -> obj[key.toString()]
    }
}

