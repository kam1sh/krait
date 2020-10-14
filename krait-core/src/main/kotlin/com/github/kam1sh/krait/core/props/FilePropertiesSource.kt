package com.github.kam1sh.krait.core.props

import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.misc.AbstractPropertiesSource
import java.io.File
import java.util.*

/**
 * Source that loads properties from a some file.
 * @param path: one of three:
 *   if classLoader provided - will be treated as a full path in resources
 *   if ends with .properties - will be treated as a full path
 *   else will be treated as prefix name ("$path.properties", "$path-$profile.properties")
 */
class FilePropertiesSource(val path: String, val classLoader: ClassLoader? = null) : AbstractPropertiesSource() {
    override fun load(profile: String) {
        _loaded = Properties()
        _parsedTree = Entry(null)
        val mainProps = Properties()
        val profileProps = Properties()
        if (classLoader != null) {
            classLoader.getResourceAsStream(path).use { mainProps.load(it) }
        } else if (path.endsWith(".properties")) {
            File(path).inputStream().use { mainProps.load(it) }
        } else {
            val mainFile = File("${path}.properties")
            if (mainFile.exists()) mainFile.inputStream().use { mainProps.load(it) }
            val profileFile = File("${path}-${profile}.properties")
            if (profileFile.exists()) profileFile.inputStream().use { profileProps.load(it) }
        }
        for (entry in mainProps) {
            loaded[entry.key] = entry.value
            store(entry.key.toString().split('.'), entry.value.toString())
        }
        for (entry in profileProps) {
            loaded[entry.key] = entry.value
            store(entry.key.toString().split('.'), entry.value.toString())
        }
    }

    override fun <T : Any> find(keys: Keys, cls: Class<T>): T? = retrieveSimple(keys)

    override fun list(keys: Keys) = retrieveAdvanced(keys).list()

    override fun <T : Any> entries(keys: Keys, cls: Class<T>) = retrieveAdvanced(keys).configNodes(cls)
}