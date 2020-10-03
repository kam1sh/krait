package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.props.PropertiesSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class KraitTest {
    @Test fun testOneSource() {
        val src = PropertiesSource("app")
        val kr = Krait {
            sources {
                add(src)
            }
        }
        val props = Properties().apply {
            set("app.key.item", "val")
            set("app.key.num", 1)
            set("app.key.bool", false)
            set("app.key.bool-true", true)
        }
        kr.load()
        src.load(props)
        assertEquals("val", kr["key"]["item"].text())
        assertEquals(1, kr["key"]["num"].long())
        assertEquals(1, kr["key"]["num"].int())
        assertThrows<ValueNotFoundException> {kr["key"]["absent"].decodeTo(String::class.java)}
        assertTrue(kr["key"]["absent"].isAbsent())
        assertFalse(kr["key"]["bool"].bool())
        assertTrue(kr["key"]["bool-true"].bool())
    }

    @Test fun testMultipleSources() {
        val src = PropertiesSource("app")
    }
}