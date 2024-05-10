package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.data.Modifier

interface ClassModifiersBuilder : ModifiersBuilder {

    fun toAbstract()
    fun toInterface()
    fun toAnnotation()

    fun toInner()
}

interface FieldModifiersBuilder : MemberModifiersBuilder {

    fun volatile()
}

interface InnerClassModifiersBuilder : ModifiersBuilder {
    fun static()
}

interface MemberModifiersBuilder : ModifiersBuilder {

    fun static()

    fun synchronized()
}

interface MethodModifiersBuilder : MemberModifiersBuilder {


    fun inline()

    fun native()
}

interface ModifierBuilderComponent<N, B : ModifiersBuilder> {
    fun modifiers(init: B.() -> Unit): B
}

/**
 * class
 *
 * method
 *
 * field
 */
interface ModifiersBuilder : Builder<Set<Modifier>> {
    fun private()
    fun public()
    fun protected()
    fun final()


}

interface ModifierSetterComponent<B : ModifiersBuilder> {
    fun modifiers(init: B.() -> Unit)
}