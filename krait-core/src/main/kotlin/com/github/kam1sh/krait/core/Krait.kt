package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.KraitException
import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import org.slf4j.LoggerFactory

/**
 * Configure Krait from here.
 * Usage:
 * val kr = Krait {
 *     profiles = setOf("dev", "test", "prod")
 *     sources {
 *         add(EnvironmentSource("APP")) // will have highest priority
 *         add(PropertiesSource("app")) // will have second priority
 *         add(DotenvSource("APP"))
 *     }
 * }
 * kr.load("dev") // will load default profile first, then dev profile
 * val dbUrl = kr["database"]["url"].text()
 * db.connect(dbUrl)
 */
class Krait(private val block: Krait.() -> Unit) {

    private val log = LoggerFactory.getLogger(Krait::class.java)
    private val srcs = ConfigSources()
    private var activeProfile: String? = null

    /**
     * Configure sources.
     * Usage:
     *   sources {
     *      add(...)
     *   }
     */
    fun sources(srcblock: ConfigSources.() -> Unit) = srcs.srcblock()

    /**
     * Load Krait, using profile from provided argument.
     */
    fun load(profile: String) {
        activeProfile = profile
        block()
        srcs.map { it.load(profile) }
    }

    /**
     * Reload all sources and optionally change profile.
     * @param profile profile to switch
     */
    fun reload(profile: String? = null) {
        val pr = profile ?: activeProfile ?: throw KraitException("Krait has not been loaded yet.")
        srcs.map { it.load(pr) }
    }

    /**
     * Find value of class T by the list of keys.
     */
    fun <T: Any> find(keys: Keys, cls: Class<T>): T? {
        for (src in srcs) {
            val value = src.find(keys, cls)
            value?.let { return it }
        }
        return null
    }

    /**
     * Like find(keys, cls), but with default value.
     */
    fun <T: Any> findOrDefault(keys: Keys, cls: Class<T>, default: T): T {
        return find(keys, cls) ?: default
    }

    /**
     * Get root list of nodes.
     * Example data:
     *   ---
     *   - name: prepare
     *     action: ...
     *   - task: install
     *     action: ...
     *   - task: configure
     *     action: ...
     */
    fun list() = list(listOf())

    /**
     * Get list of nodes.
     * Example data:
     *   app.loggers.0.name = app
     *   app.loggers.0.level = info
     *   app.loggers.1.name = framework
     *   app.loggers.1.level = warn
     * Call:
     *   kr.list(listOf("loggers"))[0]!!["name"].text()
     */
    fun list(keys: Keys): List<ConfigNode> {
        val result = mutableListOf<ConfigNode>()
        for (src in srcs) {
            try {
                val items = src.list(keys)
                result.addAll(items)
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        return result
    }

    /**
     * Get root map of nodes with keys of type T.
     * Example data:
     *   app.first = 1
     *   app.second = 2
     *   app.third = 3
     * Call:
     *   kr.entries(String::class.java)
     */
    fun <T: Any> entries(cls: Class<T>) = entries(listOf(), cls)

    /**
     * Get map of nodes with keys of type T by provided path.
     * Example data:
     *   app.numbers.first = 1
     *   app.numbers.second = 2
     *   app.numbers.third = 3
     * Call:
     *   kr.entries(listOf("numbers"), String::class.java)
     */
    fun <T: Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode> {
        val result = mutableMapOf<T, ConfigNode>()
        for (src in srcs.reversed()) {
            try {
                val items = src.entries(keys, cls)
                result.putAll(items)
            } catch (exc: ValueNotFoundException) {
                continue
            }
        }
        return result
    }

    /**
     * Get child ConfigNode by key.
     * Fine-looking way of accessing configuration.
     * kr["database"]["url"].text()
     */
    operator fun get(key: Any): ConfigNode {
        return ConfigNode(this, listOf(key))
    }
}