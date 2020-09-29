package com.github.kam1sh.krait.core.env

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        assertEquals("1", src[listOf("key")])
    }

    @Test fun testKeyWithUnderscore() {
        assertEquals("2", src[listOf("another_key")])
    }

    @Test fun testNestedKey() {
        assertEquals("3", src[listOf("nested", "key")])
        assertEquals("4", src[listOf("nested", "key2")])
        assertNull(src[listOf("another", "key")])
    }
}