@file:Suppress("FunctionName")

package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.ast.*

interface LoopBlockBuilder : BlockBuilder<LoopBlockNode> {
    fun buildTarget(context: BuildContext): LoopBlockNode
}

interface LambdaBlockBuilder : BlockBuilder<LambdaBlockNode> {
    fun buildTarget(context: BuildContext): LambdaBlockNode
}

interface CommonBlockBuilder : BlockBuilder<BlockNode> {
    fun buildTarget(context: BuildContext): BlockNode
}

interface BlockBuilder<B : BlockNodeKind> : Builder<B>, BlockExprTrait {

    val name: String

    fun add(builder: Builder<out Node>)
    fun addAll(vararg builders: Builder<out Node>)
    fun label(name: String)
    fun Block(name: String? = null, init: CommonBlockBuilder.() -> Unit)

    fun Define(name: String): VarDefineBuilder

    fun If(condition: ExprBuilder<*>): IfBuilder

    fun Try(init: CommonBlockBuilder.() -> Unit): TryCatchBuilder

    fun When(init: WhenBuilderWithoutTarget.() -> Unit): WhenBuilder

    fun When(target: ExprBuilder<*>, init: WhenBuilderWithTarget.() -> Unit): WhenBuilder

    fun While(condition: ExprBuilder<*>): WhileBuilder

    fun Return(value: ExprBuilder<*>, label: String? = null)

    fun Break(label: String? = null)

    fun Continue(label: String? = null)

    fun Invoke(name: String): InvokeBuilder
    fun Lit(value: String)
    fun Lit(value: Number)

    fun params(): MutableList<ParamBuilder>
}

interface BlockExprTrait : TypeHolder, ExprHolder {

    fun none(): ExprBuilder<VoidExpression>

    fun _if(condition: ExprBuilder<*>): IfBuilder

    fun _try(init: CommonBlockBuilder.() -> Unit): TryCatchBuilder

    fun _when(init: WhenBuilderWithoutTarget.() -> Unit): WhenBuilder
    fun _when(target: ExprBuilder<*>, init: WhenBuilderWithTarget.() -> Unit): WhenBuilder
    fun lambda(vararg params: String): LambdaBuilder

    fun lambda(vararg params: CommonParamBuilder): LambdaBuilder

    fun param(name: String, type: TypeBuilder): CommonParamBuilder

    fun array(vararg elements: ExprBuilder<*>): ArrayBuilder

    fun self(): SelfCallBuilder


    infix fun variable(name: String): VarCallBuilder
    fun lit(value: String): ExprBuilder<*>
    fun lit(value: Number): ExprBuilder<*>

    fun lit(bool: Boolean): ExprBuilder<*>
}