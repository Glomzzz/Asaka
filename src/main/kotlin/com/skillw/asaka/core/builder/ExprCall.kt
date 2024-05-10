package com.skillw.asaka.core.builder

import com.skillw.asaka.core.ir.ast.ClassCallExpression
import com.skillw.asaka.core.ir.ast.VarCallExpression


interface VarCallBuilder : ExprBuilder<VarCallExpression>, ExprHolder {
    infix fun assignTo(expr: ExprBuilder<*>): ExprBuilder<*>

    override infix fun invoke(name: String): InvokeBuilder

    infix fun Invoke(name: String): InvokeBuilder
}

interface SelfCallBuilder : ExprBuilder<ClassCallExpression>, ExprHolder