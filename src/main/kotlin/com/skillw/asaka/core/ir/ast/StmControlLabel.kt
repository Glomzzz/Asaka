package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.Identifier
import com.skillw.asaka.core.ir.type.TypeInst

/** Asahi Break 语句 */
class BreakStatement(
    source: Span,
    label: Identifier,
) : LabelHolder(source, label, LabelType.END) {
    override fun cloneNode(blc: MethodBlock) = BreakStatement(source, label.clone())
}

/** Asahi Continue 语句 */
class ContinueStatement(
    source: Span,
    label: Identifier,
) : LabelHolder(source, label, LabelType.START) {
    override fun cloneNode(blc: MethodBlock) = ContinueStatement(source, label.clone())
}

/** Asahi 标签持有者语句 */
sealed class LabelHolder(source: Span, val label: Identifier, val labelType: LabelType) : Statement(source) {
    val labelId = label.name

    enum class LabelType {
        START, END, NONE
    }

    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "statement" to javaClass.simpleName.replace("Statement", "").lowercase(),
            "label" to labelId,
        )
    }
}

/** Asahi Return 语句 */
class ReturnStatement(
    source: Span,
    label: Identifier,
    private val type: TypeInst,
    var value: Expression,
) :
    LabelHolder(source, label, LabelType.END), TypeInferable, SingleNodeHolder {

    override fun single() = value

    override fun getType() = type

    override fun cloneNode(blc: MethodBlock) =
        ReturnStatement(source, label.clone(), getType().clone(), value.clone(blc))

    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "statement" to "return",
            "label" to labelId,
            "value" to value.serialize(),
        )
    }
}