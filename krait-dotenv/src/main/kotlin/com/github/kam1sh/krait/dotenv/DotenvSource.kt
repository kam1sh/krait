package com.github.kam1sh.krait.dotenv

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.KraitException
import java.io.File

class DotenvSource(val prefix: String, val recursive: Boolean = false) : ConfigSource {
    private val entries = mutableMapOf<String, String>()

    override fun load() {
        val file = discoverFile()
        load(file)
    }

    fun load(file: File) {
        file.forEachLine {
            val bits = it.split('=')
            val key = bits[0]
            val value = bits.subList(1, -1)
            if (!key.startsWith(prefix, ignoreCase = true)) {
                return@forEachLine
            }
            entries[key.toUpperCase()] = value.joinToString("=")
        }
    }

    override fun get(keys: Keys): Any? = retrieve(keys)

    override fun getWithoutNull(keys: Keys): Any = retrieve(keys) ?: ConfigNode.Absent

    private fun retrieve(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix}__${key}"
        return entries[fullKey.toUpperCase()]
    }

    private fun discoverFile(dir: File? = null): File {
        val dirOrCwd = dir ?: File(System.getProperty("user.dir"))
        if (dirOrCwd.isFile) throw IllegalArgumentException("Got file instead of directory.")
        for (file in dirOrCwd.listFiles()!!) {
            if (file.isFile) continue
            if (file.name == ".env") return file
        }
        if (recursive) {
            return discoverFile(dirOrCwd.parentFile)
        }
        else {
            throw KraitException("Dotenv file not found.")
        }
    }
}