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
            "APP__LOGGERS__0__NAME" to "application",
            "APP__LOGGERS__0__LEVEL" to "info",
            "APP__LOGGERS__1__NAME" to "framework",
            "APP__LOGGERS__1__LEVEL" to "warn",
            "APP__LOGGERS__2__NAME" to "orm",
            "APP__LOGGERS__2__LEVEL" to "debug",
            "ANOTHER__KEY" to "null"
        )
        src.load(data)
    }

    @Test fun testKey() {
        assertEquals("1", src.find(listOf("key"), String::class.java))
    }

    @Test fun testKeyWithUnderscore() {
        assertEquals("2", src.find(listOf("another_key"), String::class.java))
    }

    @Test fun testNestedKey() {
        assertEquals("3", src.find(listOf("nested", "key"), String::class.java))
        assertEquals("4", src.find(listOf("nested", "key2"), String::class.java))
        assertNull(src.find(listOf("another", "key"), String::class.java))
    }

    @Test fun testEntries() {
        val entries = src.entries(listOf("nested"), String::class.java)
        assertEquals(2, entries.size)
        println(entries)
        assertEquals("3", entries["key"]?.text())
        assertEquals("4", entries["key2"]?.text())
    }

    @Test fun testList() {
        val list = src.list(listOf("loggers"))
        assertEquals(3, list.size)
        assertEquals("application", list[0]["name"].text())
        assertEquals("info", list[0]["level"].text())
        assertEquals("framework", list[1]["name"].text())
        assertEquals("warn", list[1]["level"].text())
        assertEquals("orm", list[2]["name"].text())
        assertEquals("debug", list[2]["level"].text())
    }
}