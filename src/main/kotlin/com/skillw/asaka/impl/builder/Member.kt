package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.ir.ast.ConstructorInvokeExpression
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.member.Constructor
import com.skillw.asaka.core.ir.type.TypeInst

class MethodParamBuilderImpl(scope: BuilderScope, blockExprTrait: BlockExprTrait, override var name: String) :
    BuilderImpl<AsahiParameter>(scope), MethodParamBuilder, BlockExprTrait by blockExprTrait {

    override var type: Builder<TypeInst> = TypeBuilderImpl(scope)
    var inline: Boolean = false
    var default: ExprBuilder<*>? = null

    override fun type(type: Builder<TypeInst>): MethodParamBuilder {
        this.type = type
        return this
    }

    override fun inline(inline: Boolean): MethodParamBuilder {
        this.inline = inline
        return this
    }

    override fun default(default: ExprBuilder<*>): MethodParamBuilder {
        this.default = default
        return this
    }

    override fun buildTarget(context: BuildContext): AsahiParameter {
        return AsahiParameter(name, type.build(context), source, inline, default?.build(context))
    }
}

class CommonParamBuilderImpl(scope: BuilderScope, override var name: String) :
    BuilderImpl<AsahiParameter>(scope), CommonParamBuilder {

    override var type: Builder<TypeInst> = TypeBuilderImpl(scope)

    override fun type(type: Builder<TypeInst>): CommonParamBuilder {
        this.type = type
        return this
    }

    override fun buildTarget(context: BuildContext): AsahiParameter {
        return AsahiParameter(name, type.build(context), source)
    }
}

class ConstructorBuilderImpl(scope: BuilderScope) :
    BuilderImpl<Constructor>(scope), TypeHolder by scope, ConstructorBuilder {

    val modifiers = ModifiersBuilderImpl(scope)
    val block = CommonBlockBuilderImpl(scope, "<init>").apply {
        none()
    }
    var constructorInvoke: ConstructorInvokeBuilderImpl? = null

    override fun params(builder: MethodParamsBuilder.() -> Unit) {
        ParamsBuilderImpl(scope, block).builder()
    }

    override fun superInit(vararg args: ExprBuilder<*>) {
        init(ConstructorInvokeExpression.Type.SUPER, *args)
    }

    override fun superInit(args: LinkedHashMap<String, ExprBuilder<*>>) {
        init(ConstructorInvokeExpression.Type.SUPER, args)
    }

    override fun thisInit(vararg args: ExprBuilder<*>) {
        init(ConstructorInvokeExpression.Type.THIS, *args)
    }

    override fun thisInit(args: LinkedHashMap<String, ExprBuilder<*>>) {
        init(ConstructorInvokeExpression.Type.THIS, args)
    }

    private fun init(type: ConstructorInvokeExpression.Type, vararg args: ExprBuilder<*>) {
        this.constructorInvoke = ConstructorInvokeBuilderImpl(
            scope,
            type, LinkedHashMap(
                args.mapIndexed { index, exprBuilder ->
                    "arg$index" to exprBuilder
                }.toMap()
            )
        )
    }


    private fun init(type: ConstructorInvokeExpression.Type, args: LinkedHashMap<String, ExprBuilder<*>>) {
        this.constructorInvoke = ConstructorInvokeBuilderImpl(scope, type, args)
    }

    override fun body(block: CommonBlockBuilder.() -> Unit) {
        this.block.block()
    }

    override fun modifiers(init: ModifiersBuilder.() -> Unit) {
        modifiers.init()
    }

    override fun buildTarget(context: BuildContext): Constructor {
        return Constructor(
            source,
            block.params().map { it.build(context) },
            modifiers.build(context),
            context.clazz().self(),
            block.build(context),
        ).apply {
            constructorInvoke?.build(context)?.also {
                body?.set(0, it)
            }
        }
    }
}