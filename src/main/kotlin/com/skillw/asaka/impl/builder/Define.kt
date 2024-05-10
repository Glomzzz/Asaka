package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.ir.type.A_VOID
import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.ir.ast.FieldDefinition
import com.skillw.asaka.core.ir.ast.MethodDefinition
import com.skillw.asaka.core.ir.ast.Node
import com.skillw.asaka.core.ir.ast.VarDefineStatement
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.Undefined

class FieldDefineBuilderImpl(scope: BuilderScope, val name: String) :
    BuilderImpl<FieldDefinition>(scope),
    FieldDefineBuilder,
    TypeHolder by scope {
    var type: TypeBuilder = TypeBuilderImpl(scope)
    var value: ExprBuilder<*>? = null
    val modifiers = ModifiersBuilderImpl(scope)
    override fun type(type: TypeBuilder): FieldDefineBuilder {
        this.type = type
        return this
    }

    override fun assignTo(value: ExprBuilder<*>): FieldDefineBuilder {
        this.value = value
        return this
    }

    override fun modifiers(init: FieldModifiersBuilder.() -> Unit): FieldModifiersBuilder {
        modifiers.init()
        return modifiers
    }

    override fun buildTarget(context: BuildContext) = creator.defineField(
        name,
        value?.build(context),
        source,
        type.build(context),
        modifiers.build(context),
    )

}

class ParamsBuilderImpl(val scope: BuilderScope, val block: BlockBuilderImpl<*>) : MethodParamsBuilder,
    BlockExprTrait by block {
    override fun name(name: String) = MethodParamBuilderImpl(scope, block, name).also {
        block.addParams(it)
    }

}

class GenericsBuilderImpl(
    val scope: BuilderScope,
    val generics: MutableList<GenericBuilder>,
    val ownerClass: ClassKindBuilder? = null
) :
    GenericsBuilder, MethodGenericsBuilder, TypeHolder by scope {
    override fun name(name: String) = GenericBuilderImpl(scope, name, ownerClass).also { generics.add(it) }
}

class MethodDefineBuilderImpl(
    scope: BuilderScope,
    override val name: String,
) :
    BuilderImpl<MethodDefinition>(scope),
    MethodDefineBuilder,
    TypeHolder by scope {

    val modifiers = ModifiersBuilderImpl(scope)
    var returnType: TypeBuilder = TypeBuilderImpl(scope).of(A_VOID)
    override val generics = mutableListOf<GenericBuilder>()
    val block = CommonBlockBuilderImpl(scope, name)


    override fun params(builder: MethodParamsBuilder.() -> Unit) {
        ParamsBuilderImpl(scope, block).builder()
    }

    override fun generics(builder: MethodGenericsBuilder.() -> Unit) {
        GenericsBuilderImpl(scope, generics).apply(builder)
    }

    override fun body(body: CommonBlockBuilder.() -> Unit) {
        scope.block(block, body)
    }

    override fun assignTo(value: Builder<out Node>) {
        block.add(value)
        returnType.of(Undefined)
    }

    override fun returnType(type: TypeBuilder) {
        returnType = type
    }

    override fun modifiers(init: MethodModifiersBuilder.() -> Unit) {
        modifiers.init()
    }

    override fun buildTarget(context: BuildContext) = creator.defineMethod(
        returnType.build(context),
        name,
        block.params().map { it.build(context) },
        generics.map { it.build(context) },
        modifiers.build(context),
        block.build(context).body,
        source,
    )

}

class VarDefineBuilderImpl(scope: BuilderScope, val name: String) : BuilderImpl<VarDefineStatement>(scope),
    VarDefineBuilder {

    var type: Builder<TypeInst> = TypeBuilderImpl(scope)
    var mutable: Boolean = false
    var value: ExprBuilder<*>? = null
    override fun type(type: Builder<TypeInst>): VarDefineBuilder {
        this.type = type
        return this
    }

    override fun mutable(mutable: Boolean): VarDefineBuilder {
        this.mutable = mutable
        return this
    }

    override fun assignTo(value: ExprBuilder<*>) {
        this.value = value
    }


    override fun buildTarget(context: BuildContext): VarDefineStatement {
        target = creator.defineVar(
            mutable,
            name,
            null,
            source,
            type.build(context),
        )
        target?.value = value?.build(context)
        return target!!
    }
}