package com.github.kam1sh.krait.misc

import com.fasterxml.jackson.databind.JsonNode
import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import java.math.BigDecimal
import java.math.BigInteger

@Suppress("UNCHECKED_CAST")
fun <T> JsonNode.decodeTo(cls: Class<T>): T = when {
    cls == Long::class.java -> if (isNumber) longValue() else throw ValueFormatException(toString())
    cls == Int::class.java -> if (isNumber) intValue() else throw ValueFormatException(toString())
    cls == String::class.java -> if (isTextual) textValue() else throw ValueFormatException(toString())
    cls == Boolean::class.java -> if (isBoolean) booleanValue() else throw ValueFormatException(toString())
    cls == Float::class.java -> if (isNumber) floatValue() else throw ValueFormatException(toString())
    cls == Double::class.java -> if (isNumber) doubleValue() else throw ValueFormatException(toString())
    cls == BigInteger::class.java -> if (isNumber) bigIntegerValue() else throw ValueFormatException(toString())
    cls == BigDecimal::class.java -> if (isNumber) decimalValue() else throw ValueFormatException(toString())
    else -> throw KraitException("Cannot decode $this into $cls")
} as T


class JacksonConfigNode(val jsonNode: JsonNode) : ConfigNode {

    override fun <T : Any> decodeTo(cls: Class<T>) = jsonNode.decodeTo(cls)

    override fun get(key: Int) = JacksonConfigNode(jsonNode[key])

    override fun get(key: String) = JacksonConfigNode(jsonNode[key])

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> entries(cls: Class<T>): MutableMap<T, ConfigNode> {
        when {
            cls == String::class.java -> {
                val out = mutableMapOf<String, ConfigNode>()
                for (it in jsonNode.fields()) {
                    out[it.key] = JacksonConfigNode(it.value)
                }
                return out as MutableMap<T, ConfigNode>
            }
            else -> return mutableMapOf()
        }
    }

    override fun list(): MutableList<ConfigNode> {
        val out = mutableListOf<ConfigNode>()
        for (it in jsonNode.elements()) {
            out.add(JacksonConfigNode(it))
        }
        return out
    }
}