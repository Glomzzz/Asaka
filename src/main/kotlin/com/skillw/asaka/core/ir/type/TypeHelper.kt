package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.Span

val TypeInst.simpleName
    get() = name.simple
val String.simple
    get() = substringAfterLast('.')

fun AsakaType.of(source: Span): TypeInst {
    return TypeInst.new(this, source)
}

fun String.toTypeNumber(): Number {
    return if (endsWith('L')) substring(0, length - 1).toLong()
    else if (endsWith('f')) substring(0, length - 1).toFloat()
    else if (contains('.')) toDouble()
    else toInt()
}

fun Class<*>.toPrimitive(): AsakaType = when (this) {
    Double::class.javaObjectType -> A_DOUBLE
    Float::class.javaObjectType -> A_FLOAT
    Long::class.javaObjectType -> A_LONG
    Int::class.javaObjectType -> A_INT
    Short::class.javaObjectType -> A_SHORT
    Byte::class.javaObjectType -> A_BYTE
    Char::class.javaObjectType -> A_CHAR
    Boolean::class.javaObjectType -> A_BOOLEAN
    else -> toType()
}

fun Class<*>.toType(): AsakaType {
    return JavaClass.with(this)
}

fun Class<*>.toInst(source: Span = Span.EMPTY): TypeInst {
    return TypeInst.new(toType(), source)
}


val AsakaType.boxType: AsakaType
    get() = when (this) {
        A_BOOLEAN -> BOX_BOOLEAN
        A_DOUBLE -> BOX_DOUBLE
        A_FLOAT -> BOX_FLOAT
        A_LONG -> BOX_LONG
        A_INT -> BOX_INT
        A_SHORT -> BOX_SHORT
        A_BYTE -> BOX_BYTE
        A_CHAR -> BOX_CHAR
        else -> this
    }

val AsakaType.unboxType: AsakaType
    get() = when (this) {
        BOX_BOOLEAN -> A_BOOLEAN
        BOX_DOUBLE -> A_DOUBLE
        BOX_FLOAT -> A_FLOAT
        BOX_LONG -> A_LONG
        BOX_INT -> A_INT
        BOX_SHORT -> A_SHORT
        BOX_BYTE -> A_BYTE
        BOX_CHAR -> A_CHAR
        else -> this
    }


fun Class<*>.hasParent() = this != Any::class.java && !isInterface && !isPrimitive

fun String.toInternalName(): String {
    return replace('.', '/')
}

fun AsakaType.nullable(source: Span): TypeInst {
    return TypeInst.new(this, source).also { it.nullable = true }
}

fun AsakaType.nonnull(source: Span): TypeInst {
    return TypeInst.new(this, source).also { it.nullable = false }
}

internal fun <T> List<T>.toArgMap() = LinkedHashMap<String, T>().apply {
    forEachIndexed { index, t -> this["arg$index"] = t }
}