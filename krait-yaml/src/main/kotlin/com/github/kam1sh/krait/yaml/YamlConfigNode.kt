package com.github.kam1sh.krait.yaml

import com.fasterxml.jackson.databind.JsonNode
import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.misc.decodeTo

class YamlConfigNode(val jsonNode: JsonNode) : ConfigNode {

    override fun <T : Any> decodeTo(cls: Class<T>) = jsonNode.decodeTo(cls)

    override fun get(key: Any): ConfigNode {
        return when(key) {
            is String -> YamlConfigNode(jsonNode[key])
            is Int -> YamlConfigNode(jsonNode[key])
            else -> throw ValueNotFoundException(listOf(key))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> entries(cls: Class<T>): Map<T, ConfigNode> {
        when {
            cls == String::class.java -> {
                val out = mutableMapOf<String, ConfigNode>()
                for (it in jsonNode.fields()) {
                    out[it.key] = YamlConfigNode(it.value)
                }
                return out as Map<T, ConfigNode>
            }
            else -> return mapOf()
        }
    }

    override fun list(): List<ConfigNode> {
        val out = mutableListOf<ConfigNode>()
        for (it in jsonNode.elements()) {
            out.add(YamlConfigNode(it))
        }
        return out
    }
}