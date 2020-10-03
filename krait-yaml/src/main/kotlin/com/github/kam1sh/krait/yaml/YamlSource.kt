package com.github.kam1sh.krait.yaml

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.repr
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger

class YamlSource(val filename: String) : ConfigSource {
    val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    var data: JsonNode? = null

    override fun load() {
        val file = File(filename)
        if (!file.exists()) throw KraitException("File $filename does not exist")
        data = file.inputStream().use { mapper.readTree(it) }
    }

    override fun <T: Any> get(keys: Keys, cls: Class<T>): T? {
        val item: JsonNode? = retrieve(keys)
        return item?.decodeTo(cls)
    }

    override fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        val item: JsonNode? = retrieve(keys)
        return if (item != null) item.decodeTo(cls) else throw ValueNotFoundException(keys)
    }

    private fun retrieve(keys: Keys): JsonNode? {
        if (data == null) throw SourceNotReadyException()
        var item: JsonNode? = null
        for (key in keys) {
            val realKey = if (key != null) key else "null"
            item = if (item == null) retrieve(data!!, realKey) else retrieve(item, realKey)
        }
        return item
    }

    fun list(keys: Keys): List<ConfigNode> {
        val item: JsonNode? = retrieve(keys)

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> JsonNode.decodeTo(cls: Class<T>): T = when {
        cls == Long::class.java -> if (isNumber) longValue() else throw ValueFormatException(toString())
        cls == Int::class.java -> if (isNumber) intValue() else throw ValueFormatException(toString())
        cls == String::class.java -> if (isTextual) textValue() else throw ValueFormatException(toString())
        cls == Boolean::class.java -> if (isBoolean) booleanValue() else throw ValueFormatException(toString())
        cls == Float::class.java -> if (isNumber) floatValue() else throw ValueFormatException(toString())
        cls == Double::class.java -> if (isNumber) doubleValue() else throw ValueFormatException(toString())
        cls == BigInteger::class.java -> if (isNumber) bigIntegerValue() else throw ValueFormatException(toString())
        cls == BigDecimal::class.java -> if (isNumber) decimalValue() else throw ValueFormatException(toString())
        else -> throw IllegalArgumentException("Cannot decode $this into $cls")
    } as T

    private fun retrieve(obj: JsonNode, key: Any): JsonNode = when(key) {
        is Int -> obj[key]
        is String -> obj[key]
        else -> obj[key.toString()]
    }
}