package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.type.TypeInst

internal class NestVarNestedCallBuilderImpl<S>(scope: BuilderScope, val nestableBuilder: NestedBuilderImpl<S>) :
    ExprBuilderImpl<VarNestedCallExpression>(scope), NestVarCallBuilder
        where S : Nestable, S : TypeInferable {

    override fun buildTarget(context: BuildContext): VarNestedCallExpression {
        val nestable = nestableBuilder.build(context)
        return creator.callVarNested(nestable, source)
    }

}

sealed class NestedBuilderImpl<S>(scope: BuilderScope) : BuilderImpl<S>(scope), NestableBuilder
        where S : Nestable, S : TypeInferable {
    override fun asExpr(): NestVarCallBuilder = NestVarNestedCallBuilderImpl(scope, this)
}

class IfBuilderImpl(
    scope: BuilderScope,
    val condition: ExprBuilder<*>,
    var parent: IfBuilderImpl? = null
) :
    NestedBuilderImpl<IfStatement>(scope),
    IfBuilder {

    var thenBlock = CommonBlockBuilderImpl(scope)
    var elseBlock = CommonBlockBuilderImpl(scope)
    var child: IfBuilderImpl? = null
    override fun then(block: CommonBlockBuilder.() -> Unit): IfBuilder {
        scope.block(thenBlock, block)
        return this
    }

    override fun orElse(block: CommonBlockBuilder.() -> Unit): IfBuilder {
        scope.block(elseBlock, block)
        return this
    }

    override fun orElseIf(condition: ExprBuilder<*>): IfBuilder {
        return IfBuilderImpl(scope, condition, this).also {
            child = it
        }
    }

    private fun root(): IfBuilderImpl {
        return parent?.root() ?: this
    }

    override fun buildTarget(context: BuildContext): IfStatement {
        return if (parent != null)
            root().build(context)
        else {
            val condition = this.condition.build(context)
            val then = thenBlock.build(context)
            val otherwise = if (child != null)
                child!!.let {
                    it.parent = null
                    BlockNode(context.block(Identifier(scope.nextBlockId(), source)) {
                        add(it.build(context))
                    })
                }
            else elseBlock.build(context)
            creator.createIf(condition, then, otherwise, source)
        }
    }
}

class TryCatchBuilderImpl(scope: BuilderScope) :
    NestedBuilderImpl<TryCatchStatement>(scope), TryCatchBuilder {
    val tryBlock = CommonBlockBuilderImpl(scope)
    val catchBlocks =
        ArrayList<Pair<Builder<TypeInst>, CommonBlockBuilder>>()
    var exception: CommonParamBuilder? = null
    var catchBlock: CommonBlockBuilderImpl? = null
    var finallyBlock: CommonBlockBuilderImpl? = null

    override fun catch(exception: CommonParamBuilder): TryCatchBuilder {
        this.exception = exception
        this.catchBlock = CommonBlockBuilderImpl(scope)
        return this
    }

    override fun then(block: CommonBlockBuilder.() -> Unit): TryCatchBuilder {
        if (catchBlock != null) {
            catchBlocks.add(
                Pair(
                    exception!!.type,
                    catchBlock!!.also { it.addParams(exception!!);scope.block(it, block) })
            )
            catchBlock = null
            return this
        }
        tryBlock.block()
        return this
    }

    override fun finally(block: CommonBlockBuilder.() -> Unit): TryCatchBuilder {
        finallyBlock = CommonBlockBuilderImpl(scope)
        finallyBlock!!.also { scope.block(it, block) }
        return this
    }

    override fun buildTarget(context: BuildContext): TryCatchStatement {
        val catchs = LinkedHashMap<TypeInst, BlockNode>()
        catchBlocks.forEach {
            catchs[it.first.build(context)] = it.second.build(context)
        }
        return creator.createTryCatch(
            tryBlock.build(context),
            catchs,
            finallyBlock?.build(context),
            source,
        )
    }

}

class WhileBuilderImpl(
    scope: BuilderScope,
    val condition: ExprBuilder<*>
) : BuilderImpl<WhileStatement>(scope),
    WhileBuilder {
    val body = LoopBlockBuilderImpl(scope, scope.nextBlockId())
    override fun then(block: LoopBlockBuilder.() -> Unit): WhileBuilder {
        block.invoke(body)
        return this
    }

    override fun buildTarget(context: BuildContext) = creator.createWhile(
        condition.build(context),
        body.build(context),
        source,
    )
}

class WhenBuilderImpl(
    scope: BuilderScope,
    val targetExpr: ExprBuilder<*>?
) : NestedBuilderImpl<WhenStatement>(scope), WhenBuilderWithoutTarget, WhenBuilderWithTarget {
    val cases = LinkedHashMap<ExprBuilder<*>, CommonBlockBuilder>()
    var otherwiseBlock: CommonBlockBuilderImpl? = null

    override fun case(
        a: ExprBuilder<*>,
        comparison: Comparison,
        b: ExprBuilder<*>,
        block: CommonBlockBuilder.() -> Unit
    ): WhenBuilderWithoutTarget {
        if (targetExpr != null) Err.syntax("target expression is non-null!", source)
        val caseBlock = CommonBlockBuilderImpl(scope)
        scope.block(caseBlock, block)
        cases[buildExpr {
            creator.binary(comparison, a.build(it), b.build(it), source)
        }] = caseBlock
        return this
    }

    override fun case(
        comparison: Comparison,
        other: ExprBuilder<*>,
        block: CommonBlockBuilder.() -> Unit
    ): WhenBuilderWithTarget {
        targetExpr ?: Err.syntax("target expression is null", source)
        val caseBlock = CommonBlockBuilderImpl(scope)
        scope.block(caseBlock, block)
        cases[buildExpr {
            creator.binary(comparison, targetExpr.build(it), other.build(it), source)
        }] = caseBlock
        return this
    }

    override fun otherwise(block: CommonBlockBuilder.() -> Unit): WhenBuilder {
        otherwiseBlock = CommonBlockBuilderImpl(scope)
        scope.block(otherwiseBlock!!, block)
        return this
    }

    override fun buildTarget(context: BuildContext): WhenStatement {
        target = creator.createWhen(
            targetExpr?.build(context),
            otherwise = otherwiseBlock?.build(context),
            source = source,
        )
        cases.forEach {
            target!!.cases[it.key.build(context)] = it.value.build(context)
        }
        return target!!
    }
}