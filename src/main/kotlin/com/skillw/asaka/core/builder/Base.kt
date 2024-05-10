package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.type.AsahiModifier
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.data.TypeSite
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.type.AsakaClass
import com.skillw.asaka.core.ir.type.GenericType
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.TypeRef
import com.skillw.asaka.impl.builder.SpanRef

interface BuildContext {

    fun block(): ModuleBlock
    fun clazz(): ClassBlock
    fun method(): MethodBlock

    fun loop(): LoopBlock

    fun block(
        label: Identifier,
        then: MethodBlock.() -> Unit
    ): MethodBlock

    fun <B : ModuleBlock> block(block: B, then: B.() -> Unit): B
}


interface MemberImportSite {
    fun importClassAs(name: String, type: Builder<ClassDefinition>)
    fun importInvokeAs(name: String, invoke: BlockExprTrait.() -> InvokeBuilder)
    fun invoke(name: String, block: BlockExprTrait): InvokeBuilder?
    fun importCallAs(name: String, call: BlockExprTrait.() -> VarCallBuilder)
    fun call(name: String, block: BlockExprTrait): VarCallBuilder?
}

interface BuilderScope : TypeHolder, MemberImportSite {

    fun clazz(builder: ClassDefineBuilder, then: ClassDefineBuilder.() -> Unit)
    fun enum(builder: EnumDefineBuilder, then: EnumDefineBuilder.() -> Unit)
    fun innerClass(builder: InnerClassBuilder, then: InnerClassBuilder.() -> Unit)
    fun method(builder: MethodDefineBuilder, then: MethodDefineBuilder.() -> Unit)
    fun <B : BlockBuilder<*>> block(builder: B, then: B.() -> Unit)
    fun clazz(): ClassKindBuilder
    fun method(): MethodDefineBuilder
    fun block(): BlockBuilder<*>
    fun nextLambdaName(): String
    fun nextBlockId(): String

}

interface ModuleBuilder : Builder<ModuleBlock>, TypeSite, MemberImportSite {
    fun clazz(name: String, builder: ClassDefineBuilder.() -> Unit)
    fun enum(name: String, builder: EnumDefineBuilder.() -> Unit)
    fun buildTarget(): ModuleBlock
    fun complete(): ModuleBuilder
}

interface Builder<T> {
    val source: Span
    infix fun at(source: Span): Builder<T>
    fun build(context: BuildContext): T

    fun <U> build(then: T.(BuildContext) -> U): Builder<U>
    fun <E : Expression> buildExpr(then: T.(BuildContext) -> E): ExprBuilder<E>
}

interface NodeCreator {

    fun defineClass(
        clazz: AsakaClass,
        source: Span,
    ): ClassDefinition

    fun defineField(
        name: String,
        value: Expression?,
        source: Span,
        type: TypeInst = TypeInst.unknown(source),
        modifiers: Set<AsahiModifier> = emptySet(),
    ): FieldDefinition

    fun defineMethod(
        type: TypeInst,
        name: String,
        params: List<AsahiParameter>,
        generics: List<GenericType>,
        modifiers: Set<Modifier>,
        body: MethodBlock,
        source: Span,
    ): MethodDefinition


    fun defineVar(
        isMutable: Boolean,
        name: String,
        value: Expression?,
        source: Span,
        type: TypeInst = TypeInst.unknown(source),
        used: Boolean = true,
    ): VarDefineStatement

    fun binary(
        operator: BinaryOpType,
        left: Expression,
        right: Expression,
        source: Span,
    ): BinaryExpression

    fun unary(
        operator: UnaryOperator,
        target: Expression,
        source: Span,
    ): UnaryExpression

    fun invokeMethod(
        self: Expression,
        name: String,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        nullSafety: Boolean,
        source: Span,
        type: TypeInst = TypeInst.unknown(source).also { it.nullable = nullSafety },
    ): MethodInvokeExpression

    fun callClass(
        type: TypeInst,
        source: Span = type.source,
    ): ClassCallExpression

    fun callVar(
        name: String,
        type: TypeInst,
        source: Span,
    ): VarCallExpression

    fun callVarNested(
        nestable: Nestable,
        source: Span,
    ): VarNestedCallExpression


    fun callField(
        self: Expression,
        name: String,
        nullSafety: Boolean,
        source: Span,
        type: TypeInst = TypeInst.unknown(source).also { it.nullable = nullSafety },
    ): FieldCallExpression

    fun invokeLambda(
        self: Expression,
        args: LinkedHashMap<String, Expression>,
        nullSafety: Boolean,
        source: Span,
    ): LambdaInvokeExpression

    fun newObject(
        type: TypeInst,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        source: Span,
    ): ObjNewExpression

    fun reference(
        self: Expression,
        name: String,
        source: Span,
        returnType: TypeInst = TypeInst.unknown(source),
    ): ReferenceExpression

    fun lambda(
        name: String,
        params: List<AsahiParameter>,
        returnType: TypeRef,
        body: LambdaBlockNode,
        source: Span,
    ): LambdaExpression

    fun literal(
        value: Any?,
        source: Span,
    ): LiteralExpression

    fun createIf(
        condition: Expression,
        then: BlockNode,
        otherwise: BlockNode? = null,
        source: Span,
    ): IfStatement

    fun createTryCatch(
        tryBlock: BlockNode,
        catchBlocks: LinkedHashMap<TypeInst, BlockNode>,
        finallyBlock: BlockNode?,
        source: Span,
    ): TryCatchStatement

    fun createWhile(
        condition: Expression,
        body: LoopBlockNode,
        source: Span,
    ): WhileStatement

    fun createBreak(
        label: Identifier,
        source: Span,
    ): BreakStatement

    fun createContinue(
        label: Identifier,
        source: Span,
    ): ContinueStatement

    fun createReturn(
        label: Identifier,
        value: Expression,
        source: Span,
        type: TypeInst = value.getType(),
    ): ReturnStatement

    fun invoke(
        self: Expression,
        name: String,
        args: LinkedHashMap<String, Expression>,
        generics: List<TypeInst>,
        nullSafety: Boolean,
        source: Span,
    ): InvokeExpression

    fun newArray(
        type: TypeRef,
        args: LinkedHashMap<String, Expression>,
        source: Span
    ): ArrayNewExpression

    fun void(source: Span): VoidExpression
    fun createWhen(
        target: Expression? = null,
        cases: LinkedHashMap<Expression, BlockNode> = LinkedHashMap(),
        otherwise: BlockNode? = null,
        source: SpanRef
    ): WhenStatement

}

interface ExprNullSafetyBuilder<E : Expression> : ExprBuilder<E> {
    infix fun safety(safety: Boolean): ExprNullSafetyBuilder<E>
}

interface ExprBuilder<E : Expression> : Builder<E> {
    infix fun plus(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun minus(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun times(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun div(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun mod(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun pow(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun refEq(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun refNeq(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>
    infix fun eq(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun neq(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun gt(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun lt(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun gte(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun lte(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun and(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun or(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun xor(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun shl(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun shr(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun ushr(other: ExprBuilder<out Expression>): ExprBuilder<out Expression>

    infix fun instanceOf(type: TypeBuilder): ExprBuilder<out Expression>

    infix fun cast(type: TypeBuilder): ExprBuilder<out Expression>

    infix fun invoke(name: String): InvokeBuilder

    fun not(): ExprBuilder<out Expression>

    fun inc(): ExprBuilder<out Expression>

    fun dec(): ExprBuilder<out Expression>

    fun negative(): ExprBuilder<out Expression>

    fun preInc(): ExprBuilder<out Expression>

    fun preDec(): ExprBuilder<out Expression>
}