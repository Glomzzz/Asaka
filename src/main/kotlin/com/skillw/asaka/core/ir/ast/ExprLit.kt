package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Serializable.Companion.javaRaw
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.A_VOID
import com.skillw.asaka.core.ir.type.TypeInst

class VoidExpression(source: Span) : Expression(source, A_VOID) {
    override fun serialize() = linkedMapOf(
        "expression" to "void",
        "type" to getType().display(),
        "value" to "void"
    )

    override fun cloneNode(blc: MethodBlock) = VoidExpression(source)
}

/**
 * Asahi 字面量表达式
 *
 * @constructor 创建一个字面量表达式
 * @property source 源码位置
 * @property typeInst 常量类型
 * @property value 常量值
 */
open class LiteralExpression(source: Span, type: TypeInst, var value: Any?) : Expression(source, type) {
    override fun serialize() = linkedMapOf(
        "expression" to "literal",
        "type" to getType().display(),
        "value" to (value?.javaRaw() ?: "null"),
    )

    override fun cloneNode(blc: MethodBlock) = LiteralExpression(source, typeInst.clone(), value)

}