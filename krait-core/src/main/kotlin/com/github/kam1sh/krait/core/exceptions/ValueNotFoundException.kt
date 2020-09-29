package com.github.kam1sh.krait.core.exceptions

import com.github.kam1sh.krait.core.Keys
import com.github.kam1sh.krait.core.repr

class ValueNotFoundException(val keys: Keys) : KraitException("Value by key ${keys.repr()} does not exist.")