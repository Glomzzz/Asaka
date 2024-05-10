package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.ast.*

interface WhileBuilder : Builder<WhileStatement> {
    infix fun then(block: LoopBlockBuilder.() -> Unit): WhileBuilder
}

interface TryCatchBuilder : Builder<TryCatchStatement>, NestableBuilder {
    infix fun catch(exception: CommonParamBuilder): TryCatchBuilder
    infix fun then(block: CommonBlockBuilder.() -> Unit): TryCatchBuilder
    infix fun finally(block: CommonBlockBuilder.() -> Unit): TryCatchBuilder
}

interface NestVarCallBuilder : ExprBuilder<VarNestedCallExpression>

interface NestableBuilder {
    fun asExpr(): NestVarCallBuilder
}

interface IfBuilder : Builder<IfStatement>, NestableBuilder {
    infix fun then(block: CommonBlockBuilder.() -> Unit): IfBuilder
    infix fun orElse(block: CommonBlockBuilder.() -> Unit): IfBuilder
    infix fun orElseIf(condition: ExprBuilder<*>): IfBuilder

}

interface WhenBuilder : Builder<WhenStatement>, NestableBuilder {
    fun otherwise(block: CommonBlockBuilder.() -> Unit): WhenBuilder
}

interface WhenBuilderWithoutTarget : WhenBuilder {
    fun case(
        a: ExprBuilder<*>,
        comparison: Comparison,
        b: ExprBuilder<*>,
        block: CommonBlockBuilder.() -> Unit
    ): WhenBuilderWithoutTarget
}


interface WhenBuilderWithTarget : WhenBuilder {
    fun case(comparison: Comparison, other: ExprBuilder<*>, block: CommonBlockBuilder.() -> Unit): WhenBuilderWithTarget
}

