package com.github.kam1sh.krait.yaml

import com.github.kam1sh.krait.core.exceptions.ValueFormatException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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
        src = YamlSource(file.toString())
        src.load()
    }

    @AfterEach fun after() {
        file.delete()
    }

    @Test fun testSimple() {
        assertEquals("value", src.get(listOf("text-key"), String::class.java))
        assertEquals(1, src.get(listOf("num-key"), Int::class.java))
        assertEquals(true, src.get(listOf("bool-key"), Boolean::class.java))
        assertThrows<ValueFormatException> { println(src.getWithoutNull(listOf("text-key"), Int::class.java)) }
    }

    @Test fun testNested() {
        assertEquals("value", src.get(listOf("nested", "key"), String::class.java))
        assertEquals("val2", src.get(listOf("nested", "keys", "key2"), String::class.java))
    }

    @Test fun testListing() {
        val data = mapOf<String, String>()
        data.entries
    }
}