package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.TypeInst


sealed interface ConditionHolder {
    var condition: Expression
    var condConst: Boolean?
}

sealed interface Nestable : TypeInferable, Serializable


/**
 * Asahi if 语句
 *
 * @constructor 创建一个 if 语句
 * @property source 源码位置
 * @property condition 条件
 * @property then 条件为真时执行的代码块
 * @property otherwise 条件为假时执行的代码块
 */
class IfStatement(source: Span, override var condition: Expression, var then: BlockNode, var otherwise: BlockNode?) :
    TypeStatement(source, TypeInst.unknown(source)), ConditionHolder, Nestable {
    override var condConst: Boolean? = null
    override fun serialize() = linkedMapOf(
        "statement" to "if",
        "condition" to condition.serialize(),
        "then" to then.serialize(),
        "else" to (otherwise?.serialize() ?: "empty")
    )

    override fun cloneNode(blc: MethodBlock) =
        IfStatement(source, condition.clone(blc), then.clone(blc), otherwise?.clone(blc))
}

/**
 * Asahi try...catch... 语句
 *
 * @constructor 创建一个 try...catch... 语句
 */
class TryCatchStatement(
    source: Span,
    var tryBlock: BlockNode,
    val catchBlocks: LinkedHashMap<TypeInst, BlockNode>,
    val finallyBlock: BlockNode?
) :
    TypeStatement(source, TypeInst.unknown(source)), Nestable {

    override fun serialize() = linkedMapOf(
        "statement" to "try-catch",
        "try" to tryBlock.serialize(),
        "catch" to catchBlocks.map { it.key.display() to it.value.serialize() },
        "finally" to (finallyBlock?.serialize() ?: "empty")
    )

    override fun cloneNode(blc: MethodBlock) =
        TryCatchStatement(
            source,
            tryBlock.clone(blc),
            LinkedHashMap(catchBlocks.mapKeys { it.key.clone() }.mapValues { it.value.clone(blc) }),
            finallyBlock?.clone(blc)
        )
}

class WhenStatement
    (
    source: Span, val target: Expression? = null, val cases: LinkedHashMap<Expression, BlockNode> = LinkedHashMap(),
    val otherwise: BlockNode?,
) :
    TypeStatement(source, TypeInst.unknown(source)), Nestable {


    class WhenCase(
        source: Span,
        type: Comparison,
        left: Expression,
        right: Expression
    ) : BinaryExpression(source, type, left, right) {
        override fun serialize() = linkedMapOf(
            "left" to left.serialize(),
            "comparison" to operator.id,
            "right" to right.serialize()
        )

        override fun clone(blc: MethodBlock): WhenCase {
            return cloneNode(blc)
        }

        override fun cloneNode(blc: MethodBlock): WhenCase {
            return WhenCase(source, operator as Comparison, left.clone(blc), right.clone(blc))
        }


    }

    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "statement" to "when",
            "target" to (target?.serialize() ?: "empty"),
            "cases" to cases.map { it.key.serialize() to it.value.serialize() },
            "otherwise" to (otherwise?.serialize() ?: "empty")
        )
    }

    override fun cloneNode(blc: MethodBlock) =
        WhenStatement(
            source,
            target?.clone(blc),
            LinkedHashMap(cases.mapKeys { it.key.clone(blc) }.mapValues { it.value.clone(blc) }),
            otherwise?.clone(blc)
        )
}


/**
 * Asahi while 语句
 *
 * @constructor 创建一个 while 语句
 * @property source 源码位置
 * @property condition 条件
 * @property body 循环体
 */
class WhileStatement
    (source: Span, override var condition: Expression, var body: LoopBlockNode) : Statement(source), ConditionHolder {

    override var condConst: Boolean? = null
    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "statement" to "while",
            "condition" to condition.serialize(),
            "body" to body.serialize(),
        )
    }

    override fun cloneNode(blc: MethodBlock) = WhileStatement(source, condition.clone(blc), body.clone(blc))
}


