package com.skillw.asaka.impl.pass

import com.skillw.asaka.Asaka
import com.skillw.asaka.Asaka.creator
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.member.AsahiVariable
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.pass.AsakaPass
import com.skillw.asaka.core.pass.AutoRegisterPass
import com.skillw.asaka.core.pass.NodeReplacer
import com.skillw.asaka.core.pass.PassContext
import kotlin.math.pow


private inline fun <reified R> LiteralExpression.cast(): R {
    return this.value as? R?
        ?: Err.type("Expected type ${R::class.java.name} but got ${this.value?.javaClass?.name ?: "null"} ", source)
}


private fun Expression?.literal(): LiteralExpression? {
    return this as? LiteralExpression?
}

@AutoRegisterPass
object ConstantReplacer : AsakaPass("constant-replacer", InlineExpander), NodeReplacer<Node> {
    override val target = Node::class.java
    private fun numberType(a: Number, b: Number, source: Span): AsakaType {
        if (a is Double || b is Double)
            return A_DOUBLE
        else if (a is Float || b is Float)
            return A_FLOAT
        else if (a is Long || b is Long)
            return A_LONG
        else if (a is Int || b is Int)
            return A_INT
        else if (a is Short || b is Short)
            return A_SHORT
        else if (a is Byte || b is Byte)
            return A_BYTE
        else
            Err.syntax("Unexpected number type", source)
    }

    private fun LiteralExpression.opposite(): Number {
        return when (val value = cast<Number>()) {
            is Byte -> -value
            is Short -> -value
            is Int -> -value
            is Long -> -value
            is Float -> -value
            is Double -> -value
            else -> Err.syntax("Unexpected number type", source)
        }
    }

    private fun AsahiVariable.const(value: Any?) {
        const = creator.literal(value, source)
    }

    private fun Expression.literal(value: Any?): LiteralExpression {
        return creator.literal(value, source)
    }

    override fun Node.replace(context: PassContext): Node {
        return when (this) {
            is VarDefineStatement -> {
                val value = value.literal() ?: return this
                block.variable(this)?.const(value)
                this
            }

            is VarCallExpression -> {
                if ((owner as? BinaryExpression)?.operator == ASSIGN) this
                else block.variable(this)?.const ?: this
            }

            is UnaryExpression -> {
                val target = target.literal() ?: return this
                when (operator) {
                    NOT -> literal(!target.cast<Boolean>())
                    POS -> literal(target.cast<Number>())
                    NEG -> literal(target.opposite())
                    else -> Err.syntax("Unexpected operator ${operator.id}", source)
                }
            }

            is BinaryExpression -> {
                val right = right.literal()
                    ?: if (operator == ASSIGN) {
                        val left =
                            left as? VarCallExpression ?: Err.syntax("Unexpected left expression", left.source)
                        block.variable(left)?.const = null
                        return this
                    } else return this
                when (operator) {
                    ASSIGN -> {
                        val left = left as? VarCallExpression ?: Err.syntax("Unexpected left expression", left.source)
                        block.variable(left)?.const(right)
                        return right
                    }

                    DIV -> if (right.cast<Number>() == 0)
                        Err.type("Divide by zero", right.source)

                    else -> {}
                }

                val left = left.literal() ?: return this
                val leftValue = left.value
                val rightValue = right.value
                var literal = when (operator) {
                    EQ -> literal(leftValue == rightValue)
                    NE -> literal(leftValue != rightValue)
                    REF_EQ -> literal(leftValue === rightValue)
                    REF_NE -> literal(leftValue !== rightValue)
                    else -> null
                }
                if (literal != null) return literal
                if (leftValue == null)
                    Err.type("Unexpected null value", left.source)
                if (rightValue == null)
                    Err.type("Unexpected null value", right.source)
                literal = when (operator) {
                    AND -> literal(left.cast() && right.cast())
                    OR -> literal(left.cast() || right.cast())
                    else -> null
                }
                if (literal != null) return literal
                if (operator == ADD && (leftValue is String || leftValue is String))
                    return literal(leftValue.toString() + leftValue.toString())
                val leftNum = left.cast<Number>()
                val rightNum = right.cast<Number>()
                literal = if (leftNum is Long || rightNum is Long)
                    when (operator) {
                        BIT_AND -> literal(leftNum.toLong() and rightNum.toLong())
                        BIT_OR -> literal(leftNum.toLong() or rightNum.toLong())
                        BIT_XOR -> literal(leftNum.toLong() xor rightNum.toLong())
                        BIT_SHL -> literal(leftNum.toLong() shl rightNum.toInt())
                        BIT_SHR -> literal(leftNum.toLong() shr rightNum.toInt())
                        BIT_USHR -> literal(leftNum.toLong() ushr rightNum.toInt())
                        else -> null
                    }
                else
                    when (operator) {
                        BIT_AND -> literal(leftNum.toInt() and rightNum.toInt())
                        BIT_OR -> literal(leftNum.toInt() or rightNum.toInt())
                        BIT_XOR -> literal(leftNum.toInt() xor rightNum.toInt())
                        BIT_SHL -> literal(leftNum.toInt() shl rightNum.toInt())
                        BIT_SHR -> literal(leftNum.toInt() shr rightNum.toInt())
                        BIT_USHR -> literal(leftNum.toInt() ushr rightNum.toInt())
                        else -> null
                    }
                if (literal != null) return literal
                val type = numberType(leftNum, rightNum, source)
                val const = when (operator) {
                    ADD -> when (type) {
                        A_BYTE -> leftNum.toByte() + rightNum.toByte()
                        A_SHORT -> leftNum.toShort() + rightNum.toShort()
                        A_INT -> leftNum.toInt() + rightNum.toInt()
                        A_LONG -> leftNum.toLong() + rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() + rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() + rightNum.toDouble()
                        else -> null
                    }

                    SUB -> when (type) {
                        A_BYTE -> leftNum.toByte() - rightNum.toByte()
                        A_SHORT -> leftNum.toShort() - rightNum.toShort()
                        A_INT -> leftNum.toInt() - rightNum.toInt()
                        A_LONG -> leftNum.toLong() - rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() - rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() - rightNum.toDouble()
                        else -> null
                    }

                    MUL -> when (type) {
                        A_BYTE -> leftNum.toByte() * rightNum.toByte()
                        A_SHORT -> leftNum.toShort() * rightNum.toShort()
                        A_INT -> leftNum.toInt() * rightNum.toInt()
                        A_LONG -> leftNum.toLong() * rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() * rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() * rightNum.toDouble()
                        else -> null
                    }

                    DIV -> when (type) {
                        A_BYTE -> leftNum.toByte() / rightNum.toByte()
                        A_SHORT -> leftNum.toShort() / rightNum.toShort()
                        A_INT -> leftNum.toInt() / rightNum.toInt()
                        A_LONG -> leftNum.toLong() / rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() / rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() / rightNum.toDouble()
                        else -> null
                    }

                    MOD -> when (type) {
                        A_BYTE -> leftNum.toByte() % rightNum.toByte()
                        A_SHORT -> leftNum.toShort() % rightNum.toShort()
                        A_INT -> leftNum.toInt() % rightNum.toInt()
                        A_LONG -> leftNum.toLong() % rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() % rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() % rightNum.toDouble()
                        else -> null
                    }

                    POW -> leftNum.toDouble().pow(rightNum.toDouble())
                    GT -> when (type) {
                        A_BYTE -> leftNum.toByte() > rightNum.toByte()
                        A_SHORT -> leftNum.toShort() > rightNum.toShort()
                        A_INT -> leftNum.toInt() > rightNum.toInt()
                        A_LONG -> leftNum.toLong() > rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() > rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() > rightNum.toDouble()
                        else -> null
                    }

                    LT -> when (type) {
                        A_BYTE -> leftNum.toByte() < rightNum.toByte()
                        A_SHORT -> leftNum.toShort() < rightNum.toShort()
                        A_INT -> leftNum.toInt() < rightNum.toInt()
                        A_LONG -> leftNum.toLong() < rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() < rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() < rightNum.toDouble()
                        else -> null
                    }

                    GE -> when (type) {
                        A_BYTE -> leftNum.toByte() >= rightNum.toByte()
                        A_SHORT -> leftNum.toShort() >= rightNum.toShort()
                        A_INT -> leftNum.toInt() >= rightNum.toInt()
                        A_LONG -> leftNum.toLong() >= rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() >= rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() >= rightNum.toDouble()
                        else -> null
                    }

                    LE -> when (type) {
                        A_BYTE -> leftNum.toByte() <= rightNum.toByte()
                        A_SHORT -> leftNum.toShort() <= rightNum.toShort()
                        A_INT -> leftNum.toInt() <= rightNum.toInt()
                        A_LONG -> leftNum.toLong() <= rightNum.toLong()
                        A_FLOAT -> leftNum.toFloat() <= rightNum.toFloat()
                        A_DOUBLE -> leftNum.toDouble() <= rightNum.toDouble()
                        else -> null
                    }

                    else -> Err.syntax("Unexpected operator ${operator.id}", source)
                } ?: Err.syntax("Unexpected type ${type.display()}", source)
                literal(const)
            }

            else -> this
        }
    }


}


@AutoRegisterPass
object DeadCodeCleaner : AsakaPass("dead-code-cleaner", ConstantReplacer), NodeReplacer<ConditionHolder> {
    override val target = ConditionHolder::class.java
    override fun ConditionHolder.replace(context: PassContext): Node {
        this as Statement
        val literal = condition.literal() ?: return this
        return if (!literal.cast<Boolean>()) VoidExpression(source) else this
    }


}

@AutoRegisterPass
object InlineExpander :
    AsakaPass("inline-expander", MutableVarCapture, LambdaPasser),
    NodeReplacer<Inlineable> {
    override val target = Inlineable::class.java
    override fun Inlineable.replace(context: PassContext): Node {
        this as Expression
        if (!inline) return this
        var name: String? = null
        val used = owner !is MethodBlock
        var after = false
        var rtn = false
        when (owner) {
            is BinaryExpression -> {
                val binary = owner as BinaryExpression
                if (binary.operator == ASSIGN) {
                    val left =
                        binary.left as? VarCallExpression ?: Err.syntax(
                            "Unexpected left expression",
                            binary.left.source
                        )
                    name = left.name
                }
            }


            is VarDefineStatement -> {
                val define = owner as VarDefineStatement
                name = define.name
                after = true
            }

            is FieldDefinition -> {
                val field = owner as FieldDefinition
                name = field.name
                after = true
            }

            is ReturnStatement -> rtn = true
            else -> {}
        }
        var definition: VarDefineStatement? = null
        if (name == null) {
            name = block.nextCountedName()
            definition = creator.defineVar(true, name, null, source, getType())
        }
        if (after) context.after {
            definition?.let { add(it) }
            add(BlockNode(inlineTo(name, rtn)))
        }
        else context.before {
            definition?.let { add(it) }
            add(BlockNode(inlineTo(name, rtn)))
        }
        val node = if (used && !rtn) creator.callVar(name, getType(), source) else creator.void(source)
        return node
    }

    private val creator = Asaka.creator

    // Convert it to a assign expr if name is nonnull, otherwise a expr
    private fun Node.convertReturn(name: String?, rtn: Boolean): Node {
        when (this) {
            is ReturnStatement -> {
                return if (name != null) creator.binary(
                    ASSIGN,
                    creator.callVar(name, value.getType(), source),
                    value,
                    source
                ) else if (rtn) this else value
            }

            is IfStatement -> {
                then.convertReturn(name, rtn)
                otherwise?.convertReturn(name, rtn)
            }

            is TryCatchStatement -> {
                tryBlock.convertReturn(name, rtn)
                catchBlocks.values.forEach { it.convertReturn(name, rtn) }
            }

            is WhenStatement -> {
                cases.values.forEach { it.convertReturn(name, rtn) }
            }

            is BlockNodeKind -> replaceAll { it.convertReturn(name, rtn) }

            else -> {}
        }
        return this
    }

    private fun Inlineable.inlineTo(name: String? = null, rtn: Boolean): MethodBlock {
        return when (this) {

            is LambdaInvokeExpression -> {
                val lambda = self as? LambdaExpression? ?: Err.syntax("Unexpected null lambda", source)
                val body = block.child(lambda.body.label)
                val args = args.values.toList()
                lambda.params.forEachIndexed { index, param ->
                    val define = creator.defineVar(false, param.name, args[index], source, param.type)
                    body.add(define)
                    body.addVariable(define)
                }
                lambda.body.forEach {
                    body.add(it.clone(body).convertReturn(name, rtn))
                }
                body
            }

            is MethodInvokeExpression -> {
                val method = method
                val args = args.values.toList()
                val params = method.params
                val body = block.child(method.body!!.label)
                params.forEachIndexed { index, param ->
                    val define = creator.defineVar(false, param.name, args[index], source, param.type)
                    body.add(define)
                    body.addVariable(define)
                }
                method.body!!.forEach {
                    body.add(it.clone(body).convertReturn(name, rtn))
                }
                body
            }

            is InvokeExpression -> {
                invoke?.inlineTo(name, rtn) ?: Err.syntax("Unexpected null invoke", source)
            }
        }
    }
}