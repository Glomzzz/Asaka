package com.skillw.asaka.core.ir.data


enum class Modifier {
    INLINE,
    PRIVATE,
    PUBLIC,
    PROTECTED,
    STATIC,
    FINAL,
    ABSTRACT,
    SYNCHRONIZED,
    VOLATILE,
    TRANSIENT,
    NATIVE,
    INNER,
    INTERFACE,
    ENUM,
    ANNOTATION, ;

    fun display() = name.lowercase()
}

class Modifiers(holder: ModifierHolder) : ModifierHolder by holder {
    val isPublic: Boolean
        get() = has(Modifier.PUBLIC)
    val isProtected: Boolean
        get() = has(Modifier.PROTECTED)
    val isPrivate: Boolean
        get() = has(Modifier.PRIVATE)
    val isStatic: Boolean
        get() = has(Modifier.STATIC)
    val isFinal: Boolean
        get() = has(Modifier.FINAL)
    val isAbstract: Boolean
        get() = has(Modifier.ABSTRACT)
    val isSynchronized: Boolean
        get() = has(Modifier.SYNCHRONIZED)
    val isVolatile: Boolean
        get() = has(Modifier.VOLATILE)
    val isTransient: Boolean
        get() = has(Modifier.TRANSIENT)
    val isNative: Boolean
        get() = has(Modifier.NATIVE)
    val isInterface: Boolean
        get() = has(Modifier.INTERFACE)
    val isInline: Boolean
        get() = has(Modifier.INLINE)
}

interface ModifierHolder {
    val modifiersSet: Set<Modifier>?

    val modifiers
        get() = Modifiers(this)

    fun has(modifier: Modifier): Boolean {
        return modifiersSet?.contains(modifier) ?: error("Modifiers not initialized")
    }


}