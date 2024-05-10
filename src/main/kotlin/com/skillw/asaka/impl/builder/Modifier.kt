package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.ir.data.Modifier

class ModifiersBuilderImpl(scope: BuilderScope) : BuilderImpl<Set<Modifier>>(scope), ModifiersBuilder,
    ClassModifiersBuilder,
    FieldModifiersBuilder, MethodModifiersBuilder,
    InnerClassModifiersBuilder {

    private val modifiers = HashSet<Modifier>()
    override fun toAbstract() {
        modifiers.add(Modifier.ABSTRACT)
    }

    override fun toInterface() {
        modifiers.add(Modifier.INTERFACE)
    }

    override fun toAnnotation() {
        modifiers.add(Modifier.ANNOTATION)
    }

    override fun toInner() {
        modifiers.add(Modifier.INNER)
    }

    override fun volatile() {
        modifiers.add(Modifier.VOLATILE)
    }

    override fun inline() {
        modifiers.add(Modifier.INLINE)
    }

    override fun native() {
        modifiers.add(Modifier.NATIVE)
    }

    override fun static() {
        modifiers.add(Modifier.STATIC)
    }

    override fun synchronized() {
        modifiers.add(Modifier.SYNCHRONIZED)
    }

    override fun private() {
        modifiers.add(Modifier.PRIVATE)
    }

    override fun public() {
        modifiers.add(Modifier.PUBLIC)
    }

    override fun protected() {
        modifiers.add(Modifier.PROTECTED)
    }

    override fun final() {
        modifiers.add(Modifier.FINAL)
    }

    override fun buildTarget(context: BuildContext): Set<Modifier> {
        return modifiers
    }

}