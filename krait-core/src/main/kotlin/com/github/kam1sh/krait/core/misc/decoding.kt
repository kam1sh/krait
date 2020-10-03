package com.github.kam1sh.krait.core.misc

import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.math.BigInteger

@Suppress("UNCHECKED_CAST")
fun <T> String.castTo(cls: Class<T>): T {
    val result = try {
        when {
            cls == Long::class.java -> toLong()
            cls == Int::class.java -> toInt()
            cls == String::class.java -> this
            cls == Boolean::class.java -> toBoolean()
            cls == Float::class.java -> toFloat()
            cls == Double::class.java -> toDouble()
            cls == BigInteger::class.java -> toBigInteger()
            cls == BigDecimal::class.java -> toBigDecimal()
            else -> throw IllegalArgumentException("Cannot decode $this into $cls")
        }
    } catch (_: NumberFormatException) {
        throw ValueFormatException(this)
    }
    return result as T
}