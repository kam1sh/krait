package com.github.kam1sh.krait.core.exceptions

class ValueFormatException(val value: String) : KraitException("Failed to format value $value.")