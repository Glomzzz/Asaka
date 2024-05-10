package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.TypeInst

sealed interface MethodInfo {
    val self: Expression
    val name: String
    val paramTypes: List<TypeInst>
    val returnType: TypeInst
}

sealed interface CallSite

data class FieldCallSite(
    val self: Expression,
    val name: String,
    val type: TypeInst,
    val static: Boolean,
    val source: AsakaType = self.getType().confirm(),
) : CallSite {
    fun toPutSite(value: Expression) = FieldPutSite(self, name, type, static, value, source)
}

data class FieldPutSite(
    val self: Expression,
    val name: String,
    val type: TypeInst,
    val static: Boolean,
    val value: Expression,
    val source: AsakaType = self.getType().confirm(),
) : CallSite

data class MethodCallSite(
    override val self: Expression,
    override val name: String,
    override val paramTypes: List<TypeInst>,
    val args: List<Expression>,
    override val returnType: TypeInst,
    val static: Boolean,
    val source: AsakaType = self.getType().confirm(),
    val isInterface: Boolean = source.modifiers.isInterface
) : CallSite, MethodInfo