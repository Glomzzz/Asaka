package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.FieldCallSite
import com.skillw.asaka.core.ir.member.AsahiVariable
import com.skillw.asaka.core.ir.type.TypeInst

/**
 * Asahi 变量调用表达式
 *
 * @constructor 创建一个变量调用表达式
 * @property source 源码位置
 * @property name 变量名
 * @property typeInst 类型
 */
open class VarCallExpression(source: Span, var name: String, type: TypeInst) :
    Expression(source, type) {

    override fun serialize(): Map<String, Any> = linkedMapOf(
        "expression" to "var-call",
        "name" to name,
        "type" to getType().display(),
    )


    override fun cloneNode(blc: MethodBlock) = VarCallExpression(source, name, typeInst.clone())
}

/**
 * Asahi 字段获取表达式
 *
 * @constructor 创建一个字段获取表达式
 * @property source 源码位置
 * @property self 字段所在对象
 * @property name 字段名
 */
class FieldCallExpression(
    source: Span,
    self: Expression,
    name: String,
    override val nullSafety: Boolean,
    type: TypeInst,
) : VarCallExpression(source, name, type),
    TypeInferable, NullSafety, SingleNodeHolder {
    override var self: Expression = self
        set(value) {
            (value as? NullSafety?)?.next = null
            field = value
            (field as? NullSafety?)?.next = this
        }

    override fun single() = self

    lateinit var callSite: FieldCallSite
    override var next: Expression? = null

    override fun serialize() = linkedMapOf(
        "expression" to "field-get",
        "self" to self.serialize(),
        "name" to name,
        "type" to getType().display(),
        "null-safety" to nullSafety,
    )

    override fun cloneNode(blc: MethodBlock) =
        FieldCallExpression(source, self.clone(blc), name, nullSafety, typeInst.clone())
}

/**
 * Asahi 类调用表达式
 *
 * @constructor 创建一个类调用表达式
 * @property source 源码位置
 * @property typeInst 类型
 */
class ClassCallExpression(source: Span, type: TypeInst) : Expression(source, type) {

    override fun serialize() = linkedMapOf(
        "expression" to "class-call",
        "type" to getType().display(),
    )

    override fun cloneNode(blc: MethodBlock) = ClassCallExpression(source, typeInst.clone())
}

open class VarNestedCallExpression(source: Span, val nestable: Nestable) :
    Expression(source, nestable.getType()) {

    var varCall: VarCallExpression? = null
    override fun serialize(): Map<String, Any> = linkedMapOf(
        "expression" to "nested-call",
        "nested" to nestable.serialize(),

        )

    override fun cloneNode(blc: MethodBlock) =
        VarNestedCallExpression(source, (nestable as Node).clone(blc) as Nestable)
}