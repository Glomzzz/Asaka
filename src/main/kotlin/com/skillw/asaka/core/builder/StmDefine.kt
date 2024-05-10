package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.type.TypeInst

interface GenericsHolderBuilder<B : GenericsBuilder> {

    val generics: List<GenericBuilder>
    fun generics(builder: B.() -> Unit)
}

interface VarDefineBuilder : Builder<VarDefineStatement> {

    infix fun type(type: Builder<TypeInst>): VarDefineBuilder
    infix fun mutable(mutable: Boolean): VarDefineBuilder
    infix fun assignTo(value: ExprBuilder<*>)
}

interface MethodParamsBuilder : BlockExprTrait {
    fun name(name: String): MethodParamBuilder
}

interface GenericsBuilder : TypeHolder {
    fun name(name: String): GenericBuilder
}

interface MethodGenericsBuilder : GenericsBuilder {
    override fun name(name: String): MethodGenericBuilder
}

interface MethodDefineBuilder :
    Builder<MethodDefinition>,
    TypeHolder,
    ModifierSetterComponent<MethodModifiersBuilder>, GenericsHolderBuilder<MethodGenericsBuilder> {

    val name: String

    fun params(builder: MethodParamsBuilder.() -> Unit)

    fun body(body: CommonBlockBuilder.() -> Unit)
    fun returnType(type: TypeBuilder)

    fun assignTo(value: Builder<out Node>)
}

interface InnerClassBuilder : ClassKindBuilder,
    ModifierSetterComponent<InnerClassModifiersBuilder>, GenericsHolderBuilder<GenericsBuilder>

interface FieldDefineBuilder : Builder<FieldDefinition>, TypeHolder,
    ModifierBuilderComponent<FieldDefinition, FieldModifiersBuilder> {
    infix fun type(type: TypeBuilder): FieldDefineBuilder

    infix fun assignTo(value: ExprBuilder<*>): FieldDefineBuilder

}

interface EnumInstanceBuilder : ClassKindBuilder,
    ModifierSetterComponent<InnerClassModifiersBuilder>

interface EnumDefineBuilder : ClassBuilder<ClassModifiersBuilder> {
    fun enum(name: String, vararg args: ExprBuilder<*>, block: (EnumInstanceBuilder.() -> Unit)? = null)
    fun enum(name: String, args: LinkedHashMap<String, ExprBuilder<*>>, block: (EnumInstanceBuilder.() -> Unit)? = null)
}

interface DefineMember {

    fun defineMethod(name: String, builder: MethodDefineBuilder.() -> Unit)
    fun defineField(name: String, builder: FieldDefineBuilder.() -> Unit)
}

interface ClassKindBuilder : Builder<ClassDefinition>,
    DefineMember {
    val name: String
    fun clinit(init: CommonBlockBuilder.() -> Unit)
}

interface ClassDefineBuilder : ClassBuilder<ClassModifiersBuilder>, GenericsHolderBuilder<GenericsBuilder>

interface ClassBuilder<B : ModifiersBuilder> : ClassKindBuilder, ModifierSetterComponent<B> {
    fun constructor(init: ConstructorBuilder.() -> Unit)

    fun extends(vararg types: Builder<TypeInst>)
}