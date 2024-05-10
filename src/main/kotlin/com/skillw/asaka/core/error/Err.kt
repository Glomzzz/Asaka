package com.skillw.asaka.core.error

import com.skillw.asaka.core.Span

object Err {
    fun syntax(message: String, vararg sources: Span): Nothing {
        throw AsahiSyntaxException(message, *sources)
    }

    fun unexpect(expect: String, found: String, vararg sources: Span): Nothing {
        throw AsahiSyntaxException("Expect $expect, but found $found", *sources)
    }

    fun type(message: String, vararg sources: Span): Nothing {
        throw AsahiTypeException(message, *sources)
    }

    fun interpret(message: String, vararg sources: Span): Nothing {
        throw AsahiInterpretException(message, *sources)
    }
}