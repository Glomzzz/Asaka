package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.ast.ConstructorInvokeExpression
import com.skillw.asaka.core.ir.ast.InvokeExpression
import com.skillw.asaka.core.ir.ast.ArrayNewExpression

interface ArrayBuilder : ExprBuilder<ArrayNewExpression> {
    infix fun type(type: TypeBuilder)
}

interface ConstructorInvokeBuilder : Builder<ConstructorInvokeExpression>
interface InvokeBuilder : ExprBuilder<InvokeExpression> {

    infix fun generics(generics: Array<TypeBuilder>): InvokeBuilder
    infix fun with(args: Array<ExprBuilder<*>>): InvokeBuilder
    infix fun with(args: LinkedHashMap<String, ExprBuilder<*>>): InvokeBuilder

    infix fun safety(nullSafety: Boolean): InvokeBuilder

}

interface LambdaBuilder {
    infix fun then(block: LambdaBlockBuilder.() -> Unit): ExprBuilder<*>
}