package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.member.Constructor
import com.skillw.asaka.core.ir.type.TypeInst

interface CommonParamBuilder : ParamBuilder {
    infix fun type(type: Builder<TypeInst>): CommonParamBuilder
}

interface ConstructorBuilder : Builder<Constructor>,
    TypeHolder, ModifierSetterComponent<ModifiersBuilder> {

    fun params(builder: MethodParamsBuilder.() -> Unit)
    fun superInit(vararg args: ExprBuilder<*>)
    fun thisInit(vararg args: ExprBuilder<*>)
    fun superInit(args: LinkedHashMap<String, ExprBuilder<*>>)
    fun thisInit(args: LinkedHashMap<String, ExprBuilder<*>>)

    fun body(block: CommonBlockBuilder.() -> Unit)
}

interface MethodParamBuilder : ParamBuilder, BlockExprTrait {
    infix fun type(type: Builder<TypeInst>): MethodParamBuilder
    infix fun inline(inline: Boolean): MethodParamBuilder
    infix fun default(default: ExprBuilder<*>): MethodParamBuilder
}

interface ParamBuilder : Builder<AsahiParameter> {
    var name: String
    var type: Builder<TypeInst>
}