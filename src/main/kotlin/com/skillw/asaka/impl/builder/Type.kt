package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.ClassCallExpression
import com.skillw.asaka.core.ir.ast.ClassDefinition
import com.skillw.asaka.core.ir.ast.ReferenceExpression
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.ir.type.Undefined.name

class GenericBuilderImpl(scope: BuilderScope, override val name: String, val ownerClass: ClassKindBuilder?) :
    BuilderImpl<GenericType>(scope),
    MethodGenericBuilder,
    TypeHolder by scope {
    private val uppers = ArrayList<TypeBuilder>()
    private var variance: GenericType.Variance = GenericType.Variance.NONE
    override var reified: Boolean = false
    override fun upper(types: Array<TypeBuilder>): GenericBuilder {
        uppers.addAll(types.onEach { it.toRef() })
        return this
    }

    override fun variance(variance: GenericType.Variance): GenericBuilder {
        this.variance = variance
        return this
    }

    override fun reified(reified: Boolean): GenericBuilder {
        this.reified = reified
        return this
    }


    override fun buildTarget(context: BuildContext): GenericType {
        target = GenericType(
            name,
            uppers.map { it.build(context).toRef() }.toMutableList(),
            variance,
            reified
        ).source(
            source
        )
        target!!.ownerType = ownerClass?.build(context)?.clazz
        return target!!
    }
}

class TypeBuilderImpl(
    scope: BuilderScope,
) :
    BuilderImpl<TypeInst>(scope), TypeBuilder {

    var type: TypeInst = Undefined.toInst()
    private val callBuilder = buildExpr {
        ClassCallExpression(source, buildTarget(it))
    }
    private var generic: GenericBuilder? = null
    private var builder: Builder<out AsakaType>? = null
    private var clazz: Builder<ClassDefinition>? = null

    constructor(
        scope: BuilderScope,
        type: TypeInst = Undefined.toInst()
    ) : this(scope) {
        this.type = type
    }

    constructor(
        scope: BuilderScope,
        clazz: Builder<ClassDefinition>
    ) : this(scope) {
        this.clazz = clazz
    }

    private var ref = false
    override fun toRef(): TypeBuilder {
        ref = true
        return this
    }

    override fun invoke(name: String): InvokeBuilder {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to invoke")
        return InvokeBuilderImpl(scope, callBuilder, name)
    }

    override fun invokeSafety(name: String): InvokeBuilder {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to invoke")
        return InvokeBuilderImpl(scope, callBuilder, name).apply { safety(true) }
    }

    override fun field(name: String): VarCallBuilder {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to call")
        return VarCallBuilderImpl(scope, name, callBuilder)
    }

    override fun fieldSafety(name: String): VarCallBuilder {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to call")
        return VarCallBuilderImpl(scope, name, callBuilder).apply { safety(true) }
    }

    override fun reference(name: String): ExprBuilder<*> {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to use as reference")
        return buildExpr {
            ReferenceExpression(source, callBuilder.build(it), name, Undefined.toInst(source))
        }
    }

    private val generics = ArrayList<TypeBuilder>()

    override fun generics(generics: Array<TypeBuilder>): TypeBuilder {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to define generics")
        this.generics.addAll(generics.onEach { it.toRef() })
        return this
    }

    override fun nullable(nullable: Boolean): TypeBuilder {
        type.nullable = nullable
        return this
    }

    override fun of(generic: GenericBuilder): TypeBuilder {
        this.generic = generic
        return this
    }

    override fun of(type: AsakaType): TypeBuilder {
        this.type.type = type
        return this
    }

    override fun of(builder: Builder<out AsakaType>): TypeBuilder {
        this.builder = builder
        return this
    }

    override fun of(clazz: Class<*>): TypeBuilder {
        this.type.type = clazz.toType()
        return this
    }

    override fun of(name: String): TypeBuilder {
        scope.findType(name)?.let { this.type.completeWith(it) }
        return this
    }

    override fun new(): InvokeBuilder {
        return invoke("<init>")
    }

    override fun defineMethod(name: String, builder: MethodDefineBuilder.() -> Unit) {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to define a method")
        val method = MethodDefineBuilderImpl(scope, name).also { scope.method(it, builder) }
        method.builder()
    }

    override fun defineField(name: String, builder: FieldDefineBuilder.() -> Unit) {
        if (generic != null && !generic!!.reified) Err.type("generic type $name must be reified to define a method")
        val field = FieldDefineBuilderImpl(scope, name)
        field.builder()
    }

    override fun at(source: Span): Builder<TypeInst> {
        return super.at(source).also { type.source = source }
    }

    override fun buildTarget(context: BuildContext): TypeInst {
        target = if (ref) type.toRef() else type
        builder?.build(context)?.let {
            type.type = it
        }
        clazz?.build(context)?.let {
            type.type = it.clazz
        }
        generic?.build(context)?.let {
            type.type = it
        }
        generics.forEachIndexed { index, generic ->
            type.generics[type.genericTypes[index].name] = generic.build(context).toRef()
        }
        return target!!
    }
}