package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.TypeInst

/**
 * Asahi 一元表达式
 *
 * 一般是前缀或后缀表达式, 例如 `++x` 或 `x++`
 *
 * @constructor 创建一个一元表达式
 * @property source 源码位置
 * @property operator 操作符
 * @property target 目标表达式
 */
class UnaryExpression(source: Span, val operator: UnaryOperator, var target: Expression) :
    Expression(source, TypeInst.unknown(source)), SingleNodeHolder {

    override fun single() = target
    override fun serialize() = linkedMapOf(
        "expression" to "unary",
        "operator" to operator.id,
        "target" to target.serialize(),
        "type" to getType().display(),
    )

    override fun cloneNode(blc: MethodBlock) =
        UnaryExpression(source, operator, target.clone(blc))
}

/**
 * Asahi 二元表达式
 *
 * 一般是中缀表达式, 例如 `1 + 1`
 *
 * @constructor 创建一个二元表达式
 * @property source 源码位置
 * @property operator 操作符
 * @property left 左表达式
 * @property right 右表达式
 */
open class BinaryExpression(source: Span, val operator: BinaryOpType, var left: Expression, var right: Expression) :
    Expression(source, if (operator == BinaryOperator.AS) right.getType() else TypeInst.unknown(source)), ConstExpr {
    override fun serialize() = linkedMapOf(
        "expression" to "binary",
        "left" to left.serialize(),
        "operator" to operator.id,
        "right" to right.serialize(),
        "type" to getType().display(),
    )

    override fun cloneNode(blc: MethodBlock) = BinaryExpression(source, operator, left.clone(blc), right.clone(blc))
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryExpression) return false
        if (!super.equals(other)) return false

        if (operator != other.operator) return false
        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + operator.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }


}

sealed class OpType(val id: String)
sealed class UnaryOpType(id: String) : OpType(id)
sealed class BinaryOpType(id: String) : OpType(id)

class UnaryOperator private constructor(id: String) : UnaryOpType(id) {
    companion object {
        val NOT = UnaryOperator("!")
        val POS = UnaryOperator("+")
        val NEG = UnaryOperator("-")
        val PRE_INC = UnaryOperator("++")
        val PRE_DEC = UnaryOperator("--")
        val POST_INC = UnaryOperator("++")
        val POST_DEC = UnaryOperator("--")
    }
}

class BinaryOperator private constructor(id: String) : BinaryOpType(id) {
    companion object {
        val ADD = BinaryOperator("+")
        val SUB = BinaryOperator("-")
        val MUL = BinaryOperator("*")
        val DIV = BinaryOperator("/")
        val MOD = BinaryOperator("%")
        val POW = BinaryOperator("^")


        val BIT_AND = BinaryOperator("&")
        val BIT_OR = BinaryOperator("|")
        val BIT_XOR = BinaryOperator("^")
        val BIT_NOT = BinaryOperator("~")
        val BIT_SHL = BinaryOperator("<<")
        val BIT_SHR = BinaryOperator(">>")
        val BIT_USHR = BinaryOperator(">>>")
        val AS = BinaryOperator("as")

        val ASSIGN = BinaryOperator("=")

    }
}

class Comparison private constructor(id: String) : BinaryOpType(id) {
    companion object {


        /** 地址比较 */
        val REF_EQ = Comparison("===")
        val REF_NE = Comparison("!==")

        /** 比较运算符 */
        val EQ = Comparison("==")
        val NE = Comparison("!=")
        val GT = Comparison(">")
        val LT = Comparison("<")
        val GE = Comparison(">=")
        val LE = Comparison("<=")

        /** 逻辑运算符 */
        val AND = Comparison("&&")
        val OR = Comparison("||")

        /** 判断是否为某种类型 */
        val IS = Comparison("is")
    }
}

