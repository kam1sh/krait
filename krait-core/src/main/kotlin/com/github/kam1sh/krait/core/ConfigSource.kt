package com.github.kam1sh.krait.core

/**
 * Source of configuration.
 */
interface ConfigSource {
    /**
     * Load configuration using provided profile name.
     * Source may support profiles (like dotenv, or yaml sources),
     * and may not (env, properties)
     */
    fun load(profile: String)

    /**
     * Get value of type T by a list of keys.
     */
    fun <T: Any> find(keys: Keys, cls: Class<T>): T?

    /**
     * Get list of nodes by keys.
     */
    fun list(keys: Keys): List<ConfigNode>

    /**
     * Get list of root nodes.
     * Example data:
     *   ---
     *   - name: prepare
     *     action: ...
     *   - name: install
     *     action: ...
     *   - name: configure
     *     action: ...
     */
    fun list(): List<ConfigNode> = list(listOf())

    /**
     * Get map of nodes with keys of type T by provided path.
     */
    fun <T: Any> entries(keys: Keys, cls: Class<T>): Map<T, ConfigNode>

    /**
     * Like entries(Keys, Class<T>), but for root entries.
     */
    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode> = entries(listOf(), cls)
}