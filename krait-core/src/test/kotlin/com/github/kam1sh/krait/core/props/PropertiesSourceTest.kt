package com.github.kam1sh.krait.core.props

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PropertiesSourceTest {
    lateinit var src: PropertiesSource

    @BeforeEach
    fun before() {
        val props = Properties()
        props["app.key"] = "test"
        props["app.nested.key"] = "val"
        props["app.nested.key2"] = "val2"
        props["app.nested.next.key"] = "val3"
        props["another.key"] = "123"
        PropertiesSource("app")
        src = PropertiesSource("app")
        src.load(props)
    }

    @Test
    fun testNonExistent() {
        assertNull(src[listOf("not", "existent")])
    }

    @Test fun testSimpleKey() {
        assertEquals("test", src[listOf("key")])
    }

    @Test
    fun testNestedKey() {
        assertEquals("val", src[listOf("nested", "key")])
        assertEquals("val2", src[listOf("nested", "key2")])
        assertEquals("val3", src[listOf("nested", "next", "key")])
        assertNull(src[listOf("nested")])
    }
}