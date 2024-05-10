package com.skillw.asaka.core

import com.skillw.asaka.impl.SpanImpl

interface Span {
    val index: IntRange
    val line: Int
    val script: String
    val path: String
    val isEmpty: Boolean

    var native: String?

    infix operator fun rangeTo(other: Span): Span

    infix operator fun rangeTo(other: Int): Span


    companion object {
        var EMPTY: Span = SpanImpl(IntRange.EMPTY, 0, "", "Temp")
        fun of(index: IntRange, line: Int, script: String, path: String): Span = SpanImpl(index, line, script, path)

        fun native(native: String) = EMPTY.also { it.native = native }

        infix operator fun Int.rangeTo(other: Span): Span {
            return SpanImpl(this..other.index.last, other.line, other.script, other.path)
        }
    }
}