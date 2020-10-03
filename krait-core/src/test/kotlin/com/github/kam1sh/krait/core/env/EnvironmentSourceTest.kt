package com.github.kam1sh.krait.core.env

import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnvironmentSourceTest {
    lateinit var src: EnvironmentSource

    @BeforeEach fun before() {
        src = EnvironmentSource("APP")
        val data = mapOf(
            "APP__KEY" to "1",
            "APP__ANOTHER_KEY" to "2",
            "APP__NESTED__KEY" to "3",
            "APP__NESTED__KEY2" to "4",
            "ANOTHER__KEY" to "5"
        )
        src.load(data)
    }

    @Test fun testKey() {
        assertEquals("1", src.get(listOf("key"), String::class.java))
    }

    @Test fun testKeyWithUnderscore() {
        assertEquals("2", src.get(listOf("another_key"), String::class.java))
    }

    @Test fun testNestedKey() {
        assertEquals("3", src.get(listOf("nested", "key"), String::class.java))
        assertEquals("4", src.get(listOf("nested", "key2"), String::class.java))
        assertNull(src.get(listOf("another", "key"), String::class.java))
    }
}