package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.type.TypeRef
import com.skillw.asaka.util.unsafeLazy

class LambdaBuilderImpl private constructor(
    scope: BuilderScope,
    val returnType: TypeBuilder = TypeBuilderImpl(scope)
) :
    ExprBuilderImpl<LambdaExpression>(scope),
    LambdaBuilder {
    init {
        returnType.toRef()
    }

    constructor(
        vararg params: String,
        scope: BuilderScope,
        returnType: TypeBuilder = TypeBuilderImpl(scope).toRef()
    ) : this(scope, returnType) {
        params.forEach {
            this.params.add(
                CommonParamBuilderImpl(scope, it)
            )
        }
    }

    constructor(
        vararg params: CommonParamBuilder,
        scope: BuilderScope,
        returnType: TypeBuilder = TypeBuilderImpl(scope)
    ) : this(
        scope, returnType
    ) {
        this.params.addAll(params)
    }

    val name = scope.nextLambdaName()
    val block by unsafeLazy {
        LambdaBlockBuilderImpl(scope, name, params, returnType)
    }
    val params = ArrayList<CommonParamBuilder>()
    override fun then(block: LambdaBlockBuilder.() -> Unit): ExprBuilder<*> {
        scope.block(this.block, block)
        return this
    }

    override fun buildTarget(context: BuildContext): LambdaExpression {
        val params = this.params.map { it.build(context) }
        return creator.lambda(
            name,
            params,
            returnType.build(context) as TypeRef,
            block.build(context),
            source
        )
    }

}

class InvokeBuilderImpl(scope: BuilderScope, val self: ExprBuilder<*>, val name: String) :
    ExprBuilderImpl<InvokeExpression>(scope),
    InvokeBuilder {

    val args = LinkedHashMap<String, ExprBuilder<out Expression>>()
    val generics = ArrayList<TypeBuilder>()
    var nullSafety: Boolean = false

    override fun safety(nullSafety: Boolean): InvokeBuilder {
        this.nullSafety = nullSafety
        return this
    }

    override fun generics(generics: Array<TypeBuilder>): InvokeBuilder {
        generics.forEach {
            this.generics.add(it)
        }
        return this
    }

    override fun with(args: Array<ExprBuilder<*>>): InvokeBuilder {
        this.args.clear()
        args.forEachIndexed { e, it ->
            this.args["arg$e"] = it
        }
        return this
    }

    override fun with(args: LinkedHashMap<String, ExprBuilder<*>>): InvokeBuilder {
        this.args.clear()
        this.args.putAll(args)
        return this
    }

    override fun buildTarget(context: BuildContext) = creator.invoke(
        self.build(context),
        name,
        LinkedHashMap(args.mapValues { it.value.build(context) }),
        generics.map { it.build(context) },
        nullSafety, source,
    )
}

class ConstructorInvokeBuilderImpl(
    scope: BuilderScope,
    val type: ConstructorInvokeExpression.Type,
    val args: LinkedHashMap<String, ExprBuilder<*>>
) : BuilderImpl<ConstructorInvokeExpression>(scope), ConstructorInvokeBuilder {
    override fun buildTarget(context: BuildContext): ConstructorInvokeExpression {
        return ConstructorInvokeExpression(source, type, LinkedHashMap(args.mapValues { it.value.build(context) }))
    }
}

class ArrayBuilderImpl(
    scope: BuilderScope, vararg val args: ExprBuilder<*>
) :
    ExprBuilderImpl<ArrayNewExpression>(scope), ArrayBuilder {

    lateinit var type: TypeBuilder
    override fun buildTarget(context: BuildContext) = creator.newArray(
        type.build(context).toRef(),
        args.mapIndexed { index, exprBuilder -> "arg$index" to exprBuilder.build(context) }
            .associateTo(LinkedHashMap()) { it },
        source,
    )

    override fun type(type: TypeBuilder) {
        this.type = type
    }
}