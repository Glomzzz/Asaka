package com.skillw.asaka.impl.builder

import com.skillw.asaka.Asaka
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.data.TypeSite
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.impl.GlobalTypeSite

internal class ModuleBuilderImpl(private val module: ModuleBlock, scope: BuilderScope = BuilderScopeImpl()) :
    BuilderImpl<ModuleBlock>(scope), ModuleBuilder, TypeSite by scope, MemberImportSite by scope {
    private val clazzes = ArrayList<ClassKindBuilder>()
    private val todo = ArrayList<() -> Unit>()
    override fun clazz(name: String, builder: ClassDefineBuilder.() -> Unit) {
        clazzes += ClassDefineBuilderImpl(scope, name).also {
            importClassAs(name, it)
            todo += { scope.clazz(it, builder) }
        }
    }

    override fun enum(name: String, builder: EnumDefineBuilder.() -> Unit) {
        clazzes += EnumDefineBuilderImpl(scope, name).also {
            importClassAs(name, it)
            todo += { scope.enum(it, builder) }
        }
    }

    override fun complete(): ModuleBuilder {
        todo.forEach { it() }
        return this
    }

    override fun buildTarget(context: BuildContext): ModuleBlock {
        return buildTarget()
    }

    override fun buildTarget(): ModuleBlock {
        val context = BuildContextImpl(module)
        clazzes.forEach {
            module.addClass(it.build(context))
        }
        return module
    }

}

class BuildContextImpl(private var current: ModuleBlock) : BuildContext {

    override fun block(): ModuleBlock {
        return current
    }

    override fun clazz(): ClassBlock {
        return (current as? ClassBlock)?.clazz() ?: error("Not in a class")
    }

    override fun method(): MethodBlock {
        return current as? MethodBlock ?: error("Not in a method block")
    }

    override fun loop(): LoopBlock {
        return (current as? MethodBlock)?.loop() ?: error("Not in a method block")
    }

    override fun block(label: Identifier, then: MethodBlock.() -> Unit): MethodBlock {
        val block = if (current is MethodBlock) (current as MethodBlock).child(label) else clazz().child(label)
        val temp = current
        current = block
        block.then()
        current = temp
        return block
    }

    override fun <B : ModuleBlock> block(block: B, then: B.() -> Unit): B {
        val temp = current
        current = block
        block.then()
        current = temp
        return block
    }
}

class BuilderScopeImpl : TypeHolder, BuilderScope, TypeSite by GlobalTypeSite() {


    private var lambdaCount = 0

    private var clazz: ClassKindBuilder? = null

    private var method: MethodDefineBuilder? = null

    private var block: BlockBuilder<*>? = null

    override fun clazz(builder: ClassDefineBuilder, then: ClassDefineBuilder.() -> Unit) {
        val temp = clazz
        clazz = builder
        then.invoke(builder)
        clazz = temp
    }

    override fun enum(builder: EnumDefineBuilder, then: EnumDefineBuilder.() -> Unit) {
        val temp = clazz
        clazz = builder
        then.invoke(builder)
        clazz = temp
    }

    override fun innerClass(builder: InnerClassBuilder, then: InnerClassBuilder.() -> Unit) {
        val temp = clazz
        clazz = builder
        then.invoke(builder)
        clazz = temp
    }

    override fun method(builder: MethodDefineBuilder, then: MethodDefineBuilder.() -> Unit) {
        val temp = method
        method = builder
        then.invoke(builder)
        method = temp
    }

    override fun <B : BlockBuilder<*>> block(builder: B, then: B.() -> Unit) {
        val temp = block
        block = builder
        then.invoke(builder)
        block = temp
    }

    override fun clazz(): ClassKindBuilder {
        return clazz ?: error("No class")
    }

    override fun method(): MethodDefineBuilder {
        return method ?: error("No method")
    }

    override fun block(): BlockBuilder<*> {
        return block ?: error("No block")
    }

    override fun nextLambdaName() = "\$lambda\$${method?.name}\$${lambdaCount++}"
    override fun nextBlockId() = "\$block\$${method?.name}\$${lambdaCount++}"


    override fun type(name: String): TypeBuilder {
        return classes[name]?.let {
            TypeBuilderImpl(this, it)
        }
            ?: findType(name)?.let { TypeBuilderImpl(this) }
            ?: Err.type("Cannot find type $name")
    }

    override fun generic(name: String): TypeBuilder {
        if (name == "*") return TypeBuilderImpl(this).of(GenericBuilderImpl(this, "*", clazz))
        val generic = method?.generics?.firstOrNull { it.name == name }
            ?: (clazz as? GenericsHolderBuilder<*>)?.generics?.firstOrNull { it.name == name }
            ?: Err.type("Cannot find generic $name")
        return TypeBuilderImpl(this).of(generic)
    }

    override fun type(clazz: Class<*>): TypeBuilder {
        return TypeBuilderImpl(this, findType(clazz).toInst())
    }

    override fun type(type: AsakaType): TypeBuilder {
        return TypeBuilderImpl(this, type.toInst())
    }

    override fun type(type: TypeInst): TypeBuilder {
        return TypeBuilderImpl(this, type)
    }

    override fun lambdaType(vararg paramTypes: TypeBuilder, returnType: TypeBuilder): TypeBuilder {
        return TypeBuilderImpl(this).of(object : BuilderImpl<LambdaType>(scope = this) {
            override fun buildTarget(context: BuildContext): LambdaType {
                return LambdaType(
                    source,
                    paramTypes.map { it.build(context).toRef() },
                    returnType.build(context).toRef()
                )
            }
        })
    }


    private val classes = mutableMapOf<String, Builder<ClassDefinition>>()
    private val invokes = mutableMapOf<String, BlockExprTrait.() -> InvokeBuilder>()
    private val calls = mutableMapOf<String, BlockExprTrait.() -> VarCallBuilder>()
    override fun importClassAs(name: String, type: Builder<ClassDefinition>) {
        classes[name] = type
    }

    override fun importCallAs(name: String, call: BlockExprTrait.() -> VarCallBuilder) {
        calls[name] = call
    }

    override fun invoke(name: String, block: BlockExprTrait): InvokeBuilder? {
        return invokes[name]?.invoke(block)
    }

    override fun importInvokeAs(name: String, invoke: BlockExprTrait.() -> InvokeBuilder) {
        invokes[name] = invoke
    }

    override fun call(name: String, block: BlockExprTrait): VarCallBuilder? {
        return calls[name]?.invoke(block)
    }

}

abstract class ExprNullSafetyBuilderImpl<E : Expression>(scope: BuilderScope) : ExprBuilderImpl<E>(scope),
    ExprNullSafetyBuilder<E> {
    protected var safety = false
    override fun safety(safety: Boolean): ExprNullSafetyBuilder<E> {
        this.safety = safety
        return this
    }
}

abstract class ExprBuilderImpl<E : Expression>(scope: BuilderScope) : BuilderImpl<E>(scope), ExprBuilder<E> {


    override fun plus(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, ADD, this, other.build(it))
    }

    override fun minus(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, SUB, this, other.build(it))
    }

    override fun times(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, MUL, this, other.build(it))
    }

    override fun div(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, DIV, this, other.build(it))
    }

    override fun mod(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, MOD, this, other.build(it))
    }

    override fun pow(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, POW, this, other.build(it))
    }

    override fun refEq(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, REF_EQ, this, other.build(it))
    }

    override fun refNeq(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, REF_NE, this, other.build(it))
    }

    override fun eq(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, EQ, this, other.build(it))
    }

    override fun neq(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, NE, this, other.build(it))
    }

    override fun gt(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, GT, this, other.build(it))
    }

    override fun lt(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, LT, this, other.build(it))
    }

    override fun gte(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, GE, this, other.build(it))
    }

    override fun lte(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, LE, this, other.build(it))
    }

    override fun and(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, AND, this, other.build(it))
    }

    override fun or(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, OR, this, other.build(it))
    }

    override fun xor(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, BIT_XOR, this, other.build(it))
    }

    override fun shl(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, BIT_SHL, this, other.build(it))
    }

    override fun shr(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, BIT_SHR, this, other.build(it))
    }

    override fun ushr(other: ExprBuilder<out Expression>) = buildExpr {
        BinaryExpression(other.source, BIT_USHR, this, other.build(it))
    }

    override fun instanceOf(type: TypeBuilder) = buildExpr {
        BinaryExpression(type.source, IS, this, ClassCallExpression(type.source, type.build(it)))
    }

    override fun cast(type: TypeBuilder) = buildExpr {
        BinaryExpression(type.source, AS, this, ClassCallExpression(type.source, type.build(it)))
    }

    override fun invoke(name: String) =
        InvokeBuilderImpl(scope, buildExpr { ClassCallExpression(source, it.method().self().toInst(source)) }, name)

    override fun not() = buildExpr {
        UnaryExpression(source, NOT, this)
    }

    override fun inc() = buildExpr {
        UnaryExpression(source, POST_INC, this)
    }

    override fun dec() = buildExpr {
        UnaryExpression(source, POST_DEC, this)
    }

    override fun negative() = buildExpr {
        UnaryExpression(source, NEG, this)
    }

    override fun preInc() = buildExpr {
        UnaryExpression(source, PRE_INC, this)
    }

    override fun preDec() = buildExpr {
        UnaryExpression(source, PRE_DEC, this)
    }

}

abstract class BuilderImpl<T>(val scope: BuilderScope) : Builder<T> {
    val creator = Asaka.creator

    override val source = SpanRef.EMPTY
    override fun at(source: Span): Builder<T> {
        this.source.span = source
        return this
    }

    protected var target: T? = null
    final override fun build(context: BuildContext): T {
        return target ?: buildTarget(context).also { target = it }
    }

    protected abstract fun buildTarget(context: BuildContext): T


    override fun <U> build(then: T.(BuildContext) -> U): Builder<U> {
        return object : BuilderImpl<U>(scope) {
            override fun buildTarget(context: BuildContext): U {
                return this@BuilderImpl.build(context).then(context)
            }
        }
    }

    override fun <E : Expression> buildExpr(then: T.(BuildContext) -> E): ExprBuilder<E> {
        return object : ExprBuilderImpl<E>(scope) {
            override fun buildTarget(context: BuildContext): E {
                return this@BuilderImpl.build(context).then(context)
            }
        }
    }
}

class SpanRef(var span: Span) : Span {
    override val index: IntRange
        get() = span.index
    override val line: Int
        get() = span.line
    override val script: String
        get() = span.script
    override val path: String
        get() = span.path
    override val isEmpty: Boolean
        get() = span.isEmpty

    override var native: String?
        get() = span.native
        set(value) {
            span.native = value
        }

    override fun rangeTo(other: Span) = span.rangeTo(other)

    override fun rangeTo(other: Int) = span.rangeTo(other)

    companion object {
        val EMPTY = SpanRef(Span.EMPTY)
    }

}


class BuilderInstance(scope: BuilderScope, val node: Node) : BuilderImpl<Node>(scope) {
    override fun buildTarget(context: BuildContext): Node {
        return node
    }
}

class ExprBuilderInstance(scope: BuilderScope, val expr: Expression) : ExprBuilderImpl<Expression>(scope) {
    override fun buildTarget(context: BuildContext): Expression {
        return expr
    }
}

fun BuilderImpl<*>.builder(node: Node): Builder<Node> {
    return BuilderInstance(scope, node)
}

fun BuilderImpl<*>.exprBuilder(expr: Expression): ExprBuilder<Expression> {
    return ExprBuilderInstance(scope, expr)
}

class NodeCreatorImpl internal constructor() : NodeCreator {
    override fun defineVar(
        isMutable: Boolean,
        name: String,
        value: Expression?,
        source: Span,
        type: TypeInst,
        used: Boolean,
    ) = VarDefineStatement(source, isMutable, name, value, type, used)

    override fun defineClass(clazz: AsakaClass, source: Span) = ClassDefinition(source, clazz)

    override fun defineField(
        name: String,
        value: Expression?,
        source: Span,
        type: TypeInst,
        modifiers: Set<AsahiModifier>,
    ) = FieldDefinition(source, name, value, type, modifiers)

    override fun defineMethod(
        type: TypeInst,
        name: String,
        params: List<AsahiParameter>,
        generics: List<GenericType>,
        modifiers: Set<AsahiModifier>,
        body: MethodBlock,
        source: Span,
    ) = MethodDefinition(source, type, name, params, modifiers, generics, body)

    override fun binary(
        operator: BinaryOpType,
        left: Expression,
        right: Expression,
        source: Span,
    ) = BinaryExpression(source, operator, left, right)

    override fun unary(
        operator: UnaryOperator,
        target: Expression,
        source: Span,
    ) =
        UnaryExpression(source, operator, target)

    override fun invoke(
        self: Expression,
        name: String,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        nullSafety: Boolean,
        source: Span,
    ) = InvokeExpression(source, self, name, args.onEach { it.value }, generics, nullSafety)

    override fun invokeMethod(
        self: Expression,
        name: String,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        nullSafety: Boolean,
        source: Span,
        type: TypeInst,
    ) = MethodInvokeExpression(
        source,
        self,
        name,
        args.onEach { it.value },
        generics,
        nullSafety,
        type
    )

    override fun invokeLambda(
        self: Expression,
        args: LinkedHashMap<String, Expression>,
        nullSafety: Boolean,
        source: Span,
    ) = LambdaInvokeExpression(source, self, args.onEach { it.value }, nullSafety)

    override fun callClass(
        type: TypeInst,
        source: Span,
    ) = ClassCallExpression(source, type)

    override fun callVar(
        name: String,
        type: TypeInst,
        source: Span,
    ) = VarCallExpression(source, name, type)


    override fun callVarNested(nestable: Nestable, source: Span) = VarNestedCallExpression(source, nestable)

    override fun callField(
        self: Expression,
        name: String,
        nullSafety: Boolean,
        source: Span,
        type: TypeInst,
    ) = FieldCallExpression(source, self, name, nullSafety, type)

    override fun newObject(
        type: TypeInst,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        source: Span,
    ) = ObjNewExpression(source, type, args.onEach { it.value }, generics)

    override fun reference(
        self: Expression,
        name: String,
        source: Span,
        returnType: TypeInst,
    ) = ReferenceExpression(source, self, name, returnType)

    override fun lambda(
        name: String,
        params: List<AsahiParameter>,
        returnType: TypeRef,
        body: LambdaBlockNode,
        source: Span,
    ) = LambdaExpression(source, name, params, returnType, body)


    override fun literal(
        value: Any?,
        source: Span,
    ) = LiteralExpression(
        source,
        value?.let { it::class.java.toInst().unbox() } ?: A_OBJECT.nullable(source),
        value)

    override fun createIf(
        condition: Expression,
        then: BlockNode,
        otherwise: BlockNode?,
        source: Span,
    ) = IfStatement(source, condition, then, otherwise)

    override fun createTryCatch(
        tryBlock: BlockNode,
        catchBlocks: LinkedHashMap<TypeInst, BlockNode>,
        finallyBlock: BlockNode?,
        source: Span
    ) = TryCatchStatement(source, tryBlock, catchBlocks, finallyBlock)

    override fun createWhile(
        condition: Expression,
        body: LoopBlockNode,
        source: Span,
    ) = WhileStatement(source, condition, body)

    override fun createBreak(
        label: Identifier,
        source: Span,
    ) = BreakStatement(source, label)

    override fun createContinue(
        label: Identifier,
        source: Span,
    ) = ContinueStatement(source, label)

    override fun createReturn(
        label: Identifier,
        value: Expression,
        source: Span,
        type: TypeInst,
    ) = ReturnStatement(source, label, type, value)

    override fun newArray(
        type: TypeRef,
        args: LinkedHashMap<String, Expression>,
        source: Span,
    ) = ArrayNewExpression(source, type, args.onEach { it.value })

    override fun void(source: Span) = VoidExpression(source)

    override fun createWhen(
        target: Expression?,
        cases: LinkedHashMap<Expression, BlockNode>,
        otherwise: BlockNode?,
        source: SpanRef
    ) = WhenStatement(source, target, cases, otherwise)

}