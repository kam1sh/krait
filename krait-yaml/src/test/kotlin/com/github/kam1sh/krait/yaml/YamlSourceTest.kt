package com.github.kam1sh.krait.yaml

import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files

class YamlSourceTest {
    lateinit var src: YamlSource
    lateinit var file: File
    @BeforeEach fun before() {
        file = Files.createTempFile("", ".yaml").toFile()
        println(file)
        file.writeText("""
            text-key: value
            num-key: 1
            bool-key: yes
            nested:
              key: value
              keys:
                key1: val1
                key2: val2
                key3: val3
            array:
              - val1
              - val2
              - val3
        """.trimIndent())
        src = YamlSource(file.toString().removeSuffix(".yaml"))
        src.load("dev")
    }

    @AfterEach fun after() {
        file.delete()
    }

    @Test fun testSimple() {
        assertEquals("value", src.find(listOf("text-key"), String::class.java))
        assertEquals(1, src.find(listOf("num-key"), Int::class.java))
        assertEquals(true, src.find(listOf("bool-key"), Boolean::class.java))
        assertThrows<ValueFormatException> { println(src.find(listOf("text-key"), Int::class.java)) }
    }

    @Test fun testNested() {
        assertEquals("value", src.find(listOf("nested", "key"), String::class.java))
        assertEquals("val2", src.find(listOf("nested", "keys", "key2"), String::class.java))
    }

    @Test fun testEntries() {
        val entries = src.entries(listOf("nested", "keys"), String::class.java)
        assertEquals(3, entries.size)
        assertEquals("val1", entries["key1"]?.text())
        assertEquals("val2", entries["key2"]?.text())
        assertEquals("val3", entries["key3"]?.text())
    }

    @Test fun testListing() {
        val list = src.list(listOf("array"))
        assertEquals(3, list.size)
        assertEquals("val1", list[0].text())
        assertEquals("val2", list[1].text())
        assertEquals("val3", list[2].text())
    }

    @Test fun testExists() {
        assertTrue(src.exists(listOf("nested")))
    }
}