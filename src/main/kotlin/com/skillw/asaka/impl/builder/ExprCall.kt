package com.skillw.asaka.impl.builder

import com.skillw.asaka.core.builder.*
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.Undefined


class VarCallBuilderImpl(scope: BuilderScope, val name: String, val self: ExprBuilder<out Expression>? = null) :
    VarCallBuilder,
    ExprNullSafetyBuilderImpl<VarCallExpression>(scope) {
    override fun assignTo(expr: ExprBuilder<out Expression>): ExprBuilder<*> {
        return if (expr is NestVarNestedCallBuilderImpl<*>) expr.apply { assignTo(this) } else buildExpr {
            creator.binary(ASSIGN, this, expr.build(it), expr.source)
        }
    }

    override fun Invoke(name: String): InvokeBuilder {
        return invoke(name).also {
            scope.block().add(it)
        }
    }

    override fun invoke(name: String): InvokeBuilderImpl {
        return InvokeBuilderImpl(scope, this, name)
    }

    override fun invokeSafety(name: String): InvokeBuilderImpl {
        return InvokeBuilderImpl(scope, this, name).apply { safety(true) }
    }

    override fun field(name: String): VarCallBuilder {
        return VarCallBuilderImpl(scope, name, this)
    }

    override fun fieldSafety(name: String): VarCallBuilder {
        return VarCallBuilderImpl(scope, name, this).apply { safety(true) }
    }

    override fun reference(name: String): ExprBuilder<*> {
        return buildExpr {
            ReferenceExpression(source, build(it), name, Undefined.toInst(source))
        }
    }

    override fun buildTarget(context: BuildContext): VarCallExpression {
        val self = self?.build(context)
        if (self is ClassCallExpression) return creator.callField(self, name, safety, source)
        val tempCall = creator.callVar(name, TypeInst.unknown(source), source)
        val variable = context.method().variable(tempCall) ?: Err.syntax("Variable $name not found", source)
        return if (variable is AsahiField) {
            val static = variable.modifiers.isStatic
            val self1 = if (!static) self ?: Err.type("Field $name is not static", source)
            else creator.callClass(context.method().self().toInst(source), source)
            creator.callField(self1, name, safety, source, variable.type)
        } else creator.callVar(name, variable.type, source)
    }

}

class SelfCallBuilderImpl(scope: BuilderScope) : ExprBuilderImpl<ClassCallExpression>(scope), SelfCallBuilder {
    override fun invoke(name: String): InvokeBuilderImpl {
        return InvokeBuilderImpl(scope, this, name)
    }

    override fun invokeSafety(name: String): InvokeBuilderImpl {
        return InvokeBuilderImpl(scope, this, name).apply { safety(true) }
    }

    override fun field(name: String): VarCallBuilder {
        return VarCallBuilderImpl(scope, name, this)
    }

    override fun fieldSafety(name: String): VarCallBuilder {
        return VarCallBuilderImpl(scope, name, this).apply { safety(true) }
    }

    override fun reference(name: String): ExprBuilder<*> {
        return buildExpr {
            creator.reference(
                ClassCallExpression(source, it.clazz().self().toInst(source)),
                name,
                source,
                Undefined.toInst(source),
            )
        }
    }

    override fun buildTarget(context: BuildContext): ClassCallExpression {
        return creator.callClass(context.method().self().toInst(source), source)
    }
}