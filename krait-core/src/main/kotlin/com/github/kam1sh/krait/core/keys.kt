package com.github.kam1sh.krait.core

typealias Keys = List<Any?>

fun Keys.repr() = map { it.toString() }.joinToString(".")
