package com.github.kam1sh.krait.core.dotenv

import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.exceptions.SourceNotReadyException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.AbstractTextSource
import com.github.kam1sh.krait.core.misc.castTo
import org.slf4j.LoggerFactory
import java.io.File


class DotenvSource(val prefix: String, val recursive: Boolean = false) : AbstractTextSource() {
    private val log = LoggerFactory.getLogger(javaClass)

    private var _entries: MutableMap<String, String>? = null
    private val entries
        get() = _entries ?: throw SourceNotReadyException()

    override fun load(profile: String) {
        val files = listOf(discoverFile(null), discoverFile(profile))
        log.info("Discovered files: {}", files)
        load(files)
    }

    fun load(files: List<File?>) {
        _entries = mutableMapOf()
        _parsedTree = Entry(null)
        val handler = { it: String ->
            val bits = it.split('=')
            val key = bits[0].toLowerCase().trim()
            var value = if (bits.size > 2) {
                bits.subList(1, bits.size).joinToString("=")
            } else {
                bits[1]
            }
            value = value.trim()
            if (key.startsWith(prefix, ignoreCase = true)) {
                entries[key] = value
                store(key.split("__"), value)
            }
        }
        files.forEach { it?.forEachLine(action = handler) }
        log.debug("Entries: {}", entries)
        log.debug("Parsed tree: {}", parsedTree)
    }

    override fun <T: Any> find(keys: Keys, cls: Class<T>): T? {
        val item = retrieveSimple(keys)
        return item?.castTo(cls)
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

    private fun discoverFile(profile: String? = null, dir: File? = null): File? {
        val dirOrCwd = dir ?: File(System.getProperty("user.dir"))
        if (dirOrCwd.isFile) throw IllegalArgumentException("Got file instead of directory.")
        val fileName = if (profile != null) ".${profile}.env" else ".env"
        for (file in dirOrCwd.listFiles()!!) {
            if (file.isDirectory) continue
            if (file.name == fileName) return file
        }
        return if (recursive) {
            discoverFile(profile, dirOrCwd.parentFile)
        } else null
    }
}
