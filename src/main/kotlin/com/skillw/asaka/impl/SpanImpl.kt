package com.skillw.asaka.impl

import com.skillw.asaka.core.Span


data class SpanImpl(
    override val index: IntRange,
    override val line: Int,
    override val script: String,
    override val path: String
) : Span {

    override val isEmpty: Boolean
        get() = this == Span.EMPTY
    override var native: String? = null

    override infix operator fun rangeTo(other: Span): Span {
        return this..(other.index.last)
    }

    override infix operator fun rangeTo(other: Int): Span {
        return SpanImpl(index.first..other, line, script, path)
    }


    override fun toString(): String {
        return "Span(index=$index, path='$path')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpanImpl) return false

        if (index != other.index) return false
        if (line != other.line) return false
        if (script != other.script) return false
        if (path != other.path) return false
        if (native != other.native) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + line
        result = 31 * result + script.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + (native?.hashCode() ?: 0)
        return result
    }
}
