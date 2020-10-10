package com.github.kam1sh.krait.core.dotenv

import com.github.kam1sh.krait.core.ConfigNode
import com.github.kam1sh.krait.core.ConfigSource
import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.env.EnvironmentSource
import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.AbstractTextSource
import com.github.kam1sh.krait.core.misc.TextualConfigNode
import com.github.kam1sh.krait.core.misc.castTo
import org.slf4j.LoggerFactory
import java.io.File


class DotenvSource(val prefix: String, val recursive: Boolean = false) : AbstractTextSource() {
    private val log = LoggerFactory.getLogger(javaClass)
    private var _entries: MutableMap<String, String>? = null
    private val entries
        get() = _entries ?: throw SourceNotReadyException()

    override fun load() {
        val file = discoverFile()
        load(file)
    }

    fun load(file: File) {
        _entries = mutableMapOf()
        _parsedTree = Entry(null)
        file.forEachLine {
            val bits = it.split('=')
            val key = bits[0]
            val value = if (bits.size > 2) {
                bits.subList(1, bits.size).joinToString("=")
            } else {
                bits[1]
            }
            if (!key.startsWith(prefix, ignoreCase = true)) {
                return@forEachLine
            }
            entries[key.toLowerCase()] = value
            store(key.toLowerCase().split("__"), value)
        }
        log.debug("Entries: {}", entries)
        log.debug("Parsed tree: {}", parsedTree)
    }

    override fun <T: Any> get(keys: Keys, cls: Class<T>): T? {
        val item = retrieveSimple(keys)
        return item?.castTo(cls)
    }

    override fun <T: Any> getWithoutNull(keys: Keys, cls: Class<T>): T {
        val item = retrieveSimple(keys) ?: throw ValueNotFoundException(keys)
        return item.castTo(cls)
    }

    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)

    override fun list(keys: Keys) = retrieveAdvanced(keys).list()

    private fun retrieveSimple(keys: Keys): String? {
        val key = keys.map { it.toString() }.joinToString("__")
        val fullKey = "${prefix.toLowerCase()}__${key}"
        return entries[fullKey.toLowerCase()]
    }

    private fun retrieveAdvanced(keys: Keys): Entry {
        var current = parsedTree[prefix.toLowerCase()] ?: throw ValueNotFoundException(keys)
        for (key in keys) {
            val item = current[key.toString().toLowerCase()] ?: throw ValueNotFoundException(keys)
            if (keys.lastIndexOf(key) == keys.size - 1) {
                return item
            } else {
                current = item
            }
        }
        throw ValueNotFoundException(keys)
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
