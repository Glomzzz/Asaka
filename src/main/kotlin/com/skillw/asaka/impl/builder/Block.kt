package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.Undefined

abstract class BlockBuilderImpl<B : BlockNodeKind>(scope: BuilderScope, override var name: String) :
    BuilderImpl<B>(scope), BlockBuilder<B>, TypeHolder by scope {
    protected val list = ArrayList<Builder<out Any>>()
    protected val params = mutableListOf<ParamBuilder>()
    fun addParams(vararg builders: ParamBuilder) {
        params.addAll(builders)
    }

    override fun params() = params

    override fun add(builder: Builder<out Node>) {
        list.add(builder)
    }

    override fun addAll(vararg builders: Builder<out Node>) {
        list.addAll(builders)
    }

    override fun label(name: String) {
        this.name = name
    }

    override fun Block(name: String?, init: CommonBlockBuilder.() -> Unit) {
        val block = CommonBlockBuilderImpl(scope, name ?: scope.nextBlockId()).also { scope.block(it, init) }
        list.add(block)
    }

    override fun Define(name: String): VarDefineBuilder {
        val builder = VarDefineBuilderImpl(scope, name)
        list.add(builder)
        return builder
    }

    override fun If(condition: ExprBuilder<*>) = _if(condition).also { list.add(it) }

    override fun Try(init: CommonBlockBuilder.() -> Unit) = _try(init).also { list.add(it) }

    override fun When(target: ExprBuilder<*>, init: WhenBuilderWithTarget.() -> Unit) =
        _when(target, init).also { list.add(it) }

    override fun When(init: WhenBuilderWithoutTarget.() -> Unit) = _when(init).also { list.add(it) }
    override fun While(condition: ExprBuilder<*>): WhileBuilder {
        val builder = WhileBuilderImpl(scope, condition)
        list.add(builder)
        return builder
    }

    override fun Return(value: ExprBuilder<*>, label: String?) {
        val identifier = Identifier(label ?: scope.method().name, source)
        list.add(
            build {
                val valueExpr = value.build(it)
                creator.createReturn(identifier, valueExpr, source, valueExpr.getType())
            }
        )
    }

    override fun Break(label: String?) {
        val identifier = Identifier(label ?: scope.block().name, source)
        list.add(
            builder(creator.createBreak(identifier, source))
        )
    }

    override fun Continue(label: String?) {
        val identifier = Identifier(label ?: scope.block().name, source)
        list.add(
            builder(creator.createContinue(identifier, source))
        )
    }

    override fun Invoke(name: String) = invoke(name).also { list.add(it) }

    override fun Lit(value: Number) {
        list.add(lit(value))
    }

    override fun Lit(value: String) {
        list.add(lit(value))
    }

    override fun _if(condition: ExprBuilder<*>) = IfBuilderImpl(scope, condition)
    override fun _try(init: CommonBlockBuilder.() -> Unit) = TryCatchBuilderImpl(scope).then(init)
    override fun _when(target: ExprBuilder<*>, init: WhenBuilderWithTarget.() -> Unit) =
        WhenBuilderImpl(scope, target).apply(init)

    override fun _when(init: WhenBuilderWithoutTarget.() -> Unit) = WhenBuilderImpl(scope, null).apply(init)
    override fun none(): ExprBuilder<VoidExpression> = exprBuilder(creator.void(source)) as ExprBuilder<VoidExpression>

    override fun lambda(vararg params: String) = LambdaBuilderImpl(params = params, scope = scope)

    override fun lambda(vararg params: CommonParamBuilder) = LambdaBuilderImpl(params = params, scope = scope)

    override fun param(name: String, type: TypeBuilder) = CommonParamBuilderImpl(scope, name).apply { this.type = type }

    override fun array(vararg elements: ExprBuilder<*>) = ArrayBuilderImpl(scope, *elements)

    override fun self() = SelfCallBuilderImpl(scope)

    override fun lit(value: String) = exprBuilder(creator.literal(value, source))

    override fun lit(value: Number) = exprBuilder(creator.literal(value, source))

    override fun lit(bool: Boolean) = exprBuilder(creator.literal(bool, source))

    override fun variable(name: String) = scope.call(name, this) ?: VarCallBuilderImpl(scope, name)
    override fun invoke(name: String) = scope.invoke(name, this) ?: InvokeBuilderImpl(
        scope,
        scope.clazz().buildExpr { ClassCallExpression(source, clazz.toInst(source)) },
        name
    )

    override fun invokeSafety(name: String) = InvokeBuilderImpl(
        scope,
        scope.clazz().buildExpr { ClassCallExpression(source, clazz.toInst(source)) },
        name
    ).apply { safety(true) }

    override fun field(name: String) =
        VarCallBuilderImpl(scope, name, scope.clazz().buildExpr { creator.callClass(clazz.toInst(source)) })

    override fun fieldSafety(name: String) =
        VarCallBuilderImpl(scope, name, scope.clazz().buildExpr { creator.callClass(clazz.toInst(source)) }).apply {
            safety(true)
        }

    override fun reference(name: String) = buildExpr {
        creator.reference(
            ClassCallExpression(source, it.clazz().self().toInst(source)),
            name,
            source,
            Undefined.toInst(source),
        )
    }

    protected fun MethodBlock.build(builder: Builder<out Any>, context: BuildContext) {
        val any = builder.build(context)
        when (any) {
            is Node -> add(any)

            is MethodDefinition -> this.addMethod(any)
            is FieldDefinition -> addField(any)
            else -> Err.type("Unknown type: ${any::class.simpleName}")
        }
        if (any is VarDefineStatement) addVariable(any)
    }
}

open class CommonBlockBuilderImpl(scope: BuilderScope, name: String = scope.nextBlockId()) :
    BlockBuilderImpl<BlockNode>(scope, name), CommonBlockBuilder {
    override fun buildTarget(context: BuildContext): BlockNode {
        val block = context.block(Identifier(name, source)) {
            params.forEach { addVariable(it.build(context)) }
        }
        target = BlockNode(block)
        context.block(block) {
            list.forEach { target!!.build(it, context) }
        }
        return target!!
    }
}

class LambdaBlockBuilderImpl(
    scope: BuilderScope,
    name: String,
    params: List<CommonParamBuilder>,
    val returnType: Builder<TypeInst>
) :
    BlockBuilderImpl<LambdaBlockNode>(scope, name), LambdaBlockBuilder {
    init {
        this.params.addAll(params)
    }

    override fun buildTarget(context: BuildContext): LambdaBlockNode {
        val block = context.block(
            context.method()
                .lambda(
                    Identifier(name, source),
                    params.map { it.build(context) },
                    returnType.build(context)
                )
        ) {
            params.forEach { addVariable(it) }
        }
        target = LambdaBlockNode(block)
        context.block(block) {
            list.forEach { target!!.build(it, context) }
        }
        return target!!
    }
}

class LoopBlockBuilderImpl(scope: BuilderScope, name: String) :
    BlockBuilderImpl<LoopBlockNode>(scope, name), LoopBlockBuilder {
    override fun buildTarget(context: BuildContext): LoopBlockNode {
        val block = context.block(context.method().loop(Identifier(name, source))) {
            params.forEach { addVariable(it.build(context)) }
        }
        target = LoopBlockNode(block)
        context.block(block) {
            list.forEach { target!!.build(it, context) }
        }
        return target!!
    }
}