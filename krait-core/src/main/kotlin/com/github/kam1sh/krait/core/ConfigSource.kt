package com.github.kam1sh.krait.core

interface ConfigSource {
    fun load()
    operator fun get(keys: Keys): Any?
    fun getWithoutNull(keys: Keys): Any
}