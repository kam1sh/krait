package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class PropertiesSourceTest {
    lateinit var src: PropertiesSource

    @BeforeEach
    fun before() {
        val props = Properties()
        props["app.key"] = "test"
        props["app.nested"] = "wow"
        props["app.nested.key"] = "val"
        props["app.nested.key2"] = "val2"
        props["app.nested.next.key"] = "val3"

        props["app.loggers.0.name"] = "logger1"
        props["app.loggers.0.level"] = "WARN"

        props["app.loggers.1.name"] = "logger2"
        props["app.loggers.1.level"] = "INFO"
        props["app.another-list.0"] = "listval"
        props["app.another-list.1"] = "listval2"
        props["app.another-list.2.name"] = "nameval"

        props["another.key"] = "123"
        PropertiesSource("app")
        src = PropertiesSource("app")
        src.load(props)
    }

    @Test
    fun testNonExistent() {
        assertNull(src.get(listOf("not", "existent"), Long::class.java))
    }

    @Test fun testSimpleKey() {
        assertEquals("test", src.get(listOf("key"), String::class.java))
    }

    @Test fun testNestedKey() {
        assertEquals("val", src.get(listOf("nested", "key"), String::class.java))
        assertEquals("val2", src.get(listOf("nested", "key2"), String::class.java))
        assertEquals("val3", src.get(listOf("nested", "next", "key"), String::class.java))
        assertEquals("wow",  src.get(listOf("nested"), String::class.java))
    }

    @Test fun testRootEntries() {
        val entries = src.entries(String::class.java)
        assertEquals(4, entries.size)
    }

    @Test fun testEntries() {
        val entries = src.entries(listOf("nested"), String::class.java)
        println(entries)
        assertEquals(3, entries.size)
        assertEquals("val", entries["key"]?.text())
        assertEquals("val2", entries["key2"]?.text())
        assertEquals("val3", entries["next"]?.get("key")?.text())
    }

    @Test fun testLists() {
        val loggers = src.list(listOf("loggers"))
        assertEquals(2, loggers.size)
        assertEquals("logger1", loggers[0]["name"].text())
        assertEquals("WARN", loggers[0]["level"].text())
        assertEquals("logger2", loggers[1]["name"].text())
        assertEquals("INFO", loggers[1]["level"].text())
    }

    @Test fun testListsAdvanced() {
        val list = src.list(listOf("another-list"))
        assertEquals("listval", list[0].text())
        assertEquals("listval2", list[1].text())
        assertEquals("nameval", list[2]["name"].text())
    }
}