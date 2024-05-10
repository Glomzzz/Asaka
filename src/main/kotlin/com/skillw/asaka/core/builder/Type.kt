package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.data.TypeSite
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.GenericType
import com.skillw.asaka.core.ir.type.TypeInst

interface GenericBuilder : Builder<GenericType> {
    val name: String
    val reified: Boolean
    infix fun upper(types: Array<TypeBuilder>): GenericBuilder

    infix fun variance(variance: GenericType.Variance): GenericBuilder
}

interface MethodGenericBuilder : GenericBuilder {
    infix fun reified(reified: Boolean): GenericBuilder
}


interface ExprHolder {
    infix fun invoke(name: String): InvokeBuilder
    infix fun invokeSafety(name: String): InvokeBuilder
    infix fun field(name: String): VarCallBuilder
    infix fun fieldSafety(name: String): VarCallBuilder
    infix fun reference(name: String): ExprBuilder<*>
}

interface SelfHolder : DefineMember, ExprHolder
interface TypeBuilder : Builder<TypeInst>, SelfHolder {

    fun toRef():TypeBuilder
    infix fun nullable(nullable: Boolean): TypeBuilder

    infix fun generics(generics: Array<TypeBuilder>): TypeBuilder

    fun of(type: AsakaType): TypeBuilder
    fun of(clazz: Class<*>): TypeBuilder
    fun of(name: String): TypeBuilder
    fun of(generic: GenericBuilder): TypeBuilder
    fun of(builder: Builder<out AsakaType>): TypeBuilder
    fun new(): InvokeBuilder
}

interface TypeHolder : TypeSite {

    fun type(name: String): TypeBuilder

    fun generic(name: String): TypeBuilder

    fun type(clazz: Class<*>): TypeBuilder

    fun type(type: AsakaType): TypeBuilder

    fun type(type: TypeInst): TypeBuilder

    fun lambdaType(vararg paramTypes: TypeBuilder, returnType: TypeBuilder): TypeBuilder
}