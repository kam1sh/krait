package com.github.kam1sh.krait.core

import com.github.kam1sh.krait.core.exceptions.ValueNotFoundException
import com.github.kam1sh.krait.core.misc.SimpleConfigNode

/**
 * Node (or branch/leaf) of a configuration tree.
 */
interface ConfigNode {
    /**
     * Get value as string.
     */
    fun text() = decodeTo(String::class.java)

    /**
     * Get value as long.
     */
    fun long() = decodeTo(Long::class.java)

    /**
     * Get value as integer.
     */
    fun int() = decodeTo(Int::class.java)

    /**
     * Get value as boolean.
     */
    fun bool() = decodeTo(Boolean::class.java)

    /**
     * Check if there is a value in this node
     */
    fun exists(): Boolean

    /**
     * Check if there is no value in this node.
     */
    fun isAbsent() = !exists()

    /**
     * Get list of nodes.
     * Resulting list will contain items from all sources.
     * Example usage:
     *   Input:
     *     app.loggers.0.name = app
     *     app.loggers.0.level = info
     *     app.loggers.1.name = framework
     *     app.loggers.1.level = warn
     *   Code:
     *     kr["loggers"].list()[0]["name"].text() // "app"
     */
    fun list(): List<ConfigNode>

    /**
     * Get map of key-node pairs.
     * Resulting map will contain keys from all sources, where key from the first source wins.
     * Example usage:
     *   Input:
     *     app.entries.first = 1
     *     app.entries.second = 2
     *     app.entries.third = 3
     *   Code:
     *     kr["entries"].entries()["first"]!!.int() // 1
     */
    fun entries(): Map<String, ConfigNode> = entries(String::class.java)

    /**
     * Get map of custom key-node pairs.
     * Resulting map will contain keys from all sources, where key from the first source wins.
     * Example usage:
     *     app.entries.1 = first
     *     app.entries.3 = third
     *     app.entries.5 = fifth
     *   Code:
     *     kr["entries"].entries(Int::class.java)[3]!!.text() // "third"
     */
    fun <T: Any> entries(cls: Class<T>): Map<T, ConfigNode>

    /**
     * Ask source to decode value to custom type.
     */
    fun <T: Any> decodeTo(cls: Class<T>): T

    /**
     * Get child node.
     */
    operator fun get(key: String): ConfigNode

    /**
     * Get child node.
     */
    operator fun get(key: Int): ConfigNode

    companion object {
        operator fun invoke(krait: Krait, keys: Keys) = SimpleConfigNode(krait, keys)
    }
}