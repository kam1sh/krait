package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.env.EnvironmentSource
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.props.SystemPropertiesSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class KraitTest {
    @Test fun testOneSource() {
        val src = SystemPropertiesSource("app")
        val kr = Krait("dev") {
            sources {
                add(src)
            }
        }
        val props = Properties().apply {
            set("app.key.item", "val")
            set("app.key.num", 1)
            set("app.key.bool", false)
            set("app.key.bool-true", true)
            set("app.entries.key1", "val1")
            set("app.entries.key2", "val2")
            set("app.entries.key3", "val3")
            set("app.list.0.name", "name0")
            set("app.list.0.level", "level0")
            set("app.list.1.name", "name1")
            set("app.list.1.level", "level1")
        }
        src.load(props)
        assertEquals("val", kr["key"]["item"].text())
        assertEquals(1, kr["key"]["num"].long())
        assertEquals(1, kr["key"]["num"].int())
        assertThrows<ValueNotFoundException> {kr["key"]["absent"].decodeTo(String::class.java)}
        assertTrue(kr["key"]["absent"].isAbsent())
        assertFalse(kr["key"]["bool"].bool())
        assertTrue(kr["key"]["bool-true"].bool())
        val entries = kr.entries(listOf("entries"), String::class.java)
        assertEquals(3, entries.size)
        assertEquals("val1", entries["key1"]?.text())
        assertEquals("val2", entries["key2"]?.text())
        assertEquals("val3", entries["key3"]?.text())
        val list = kr.list(listOf("list"))
        assertEquals(2, list.size)
        assertEquals("name0", list[0]["name"].text())
        assertEquals("level0", list[0]["level"].text())
        assertEquals("name1", list[1]["name"].text())
        assertEquals("level1", list[1]["level"].text())
        assertTrue(kr["entries"].exists())
    }

    @Test fun testMultipleSources() {
        val src1 = SystemPropertiesSource("app")
        val props1 = Properties().apply {
            set("app.nested.key", "val")
            set("app.loggers.0.name", "application")
            set("app.loggers.0.level", "INFO")
            set("app.loggers.1.name", "framework")
            set("app.loggers.1.level", "WARN")
            set("app.entries.key1", "val1")
        }
        val props2 = mapOf(
            "APP__NESTED__KEY" to "new-val",
            "APP__LOGGERS__0__NAME" to "orm",
            "APP__LOGGERS__0__LEVEL" to "DEBUG",
            "APP__ENTRIES__KEY2" to "val2"
        )
        val src2 = EnvironmentSource("APP")
        val kr = Krait("dev") {
            sources {
                add(src2)
                add(src1)
            }
        }
        src1.load(props1)
        src2.load(props2)
        assertEquals("new-val", kr["nested"]["key"].text())
        val loggers = kr.list(listOf("loggers"))
        assertEquals(3, loggers.size)
        assertEquals("orm", loggers[0]["name"].text())
        assertEquals("DEBUG", loggers[0]["level"].text())
        val entries = kr.entries(listOf("entries"), String::class.java)
        assertEquals("val1", entries["key1"]?.text())
        assertEquals("val2", entries["key2"]?.text())
    }
}