package com.github.kam1sh.krait.core.dotenv

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class DotenvSourceTest {
    lateinit var src: DotenvSource
    lateinit var file: File

    @BeforeEach fun before() {
        src = DotenvSource("APP")
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
            "APP__ENV" to "key=val key2=val2",
            "ANOTHER__KEY" to "null"
        )
        val text = data.asSequence().map { "${it.key}=${it.value}" }.joinToString("\n")
        file = Files.createTempFile("", ".env").toFile()
        file.writeText(text)
        src.load(listOf(file))
    }

    @AfterEach fun after() {
        file.delete()
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
        println(list[0])
        assertEquals("application", list[0]["name"].text())
        assertEquals("info", list[0]["level"].text())
        assertEquals("framework", list[1]["name"].text())
        assertEquals("warn", list[1]["level"].text())
        assertEquals("orm", list[2]["name"].text())
        assertEquals("debug", list[2]["level"].text())
    }

    @Test fun testValueWithEquals() {
        assertEquals("key=val key2=val2", src.find(listOf("env"), String::class.java))
    }

    @Test fun testExists() {
        assertTrue(src.exists(listOf("loggers")))
    }
}