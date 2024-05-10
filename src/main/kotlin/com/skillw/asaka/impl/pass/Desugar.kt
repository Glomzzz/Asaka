package com.skillw.asaka.impl.pass

import com.skillw.asaka.Asaka
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.type.Undefined
import com.skillw.asaka.core.pass.AsakaPass
import com.skillw.asaka.core.pass.AutoRegisterPass
import com.skillw.asaka.core.pass.NodeReplacer
import com.skillw.asaka.core.pass.PassContext


@AutoRegisterPass
object NestedExpander :
    AsakaPass("nested-expander", MutableVarCapture, LambdaPasser),
    NodeReplacer<VarNestedCallExpression> {
    override val target = VarNestedCallExpression::class.java
    override fun VarNestedCallExpression.replace(context: PassContext): Expression {
        if (varCall != null) return varCall!!
        var name: String? = null
        val used = owner !is MethodBlock
        var rtn = false
        var after = false
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
            add((nestable as Node).convertLast(name, rtn))
        }
        else context.before {
            definition?.let { add(it) }
            add((nestable as Node).convertLast(name, rtn))
        }
        val node = if (used && !rtn && !after) creator.callVar(name, getType(), source).also {
            varCall = it
        } else creator.void(source)
        if (after) node.getType().apply { type = Undefined;completeWith(nestable.getType()) }
        return node
    }

    private val creator = Asaka.creator

    // Convert it to a assign expr if name is nonnull, otherwise a expr
    private fun Node.convertLast(name: String?, rtn: Boolean): Node {
        when (this) {

            is IfStatement -> {
                then.convertLast(name, rtn)
                otherwise?.convertLast(name, rtn)
            }

            is TryCatchStatement -> {
                tryBlock.convertLast(name, rtn)
                catchBlocks.values.forEach { it.convertLast(name, rtn) }
            }

            is WhenStatement -> {
                cases.values.forEach { it.convertLast(name, rtn) }
            }

            is BlockNodeKind -> {
                val last = lastOrNull() ?: Err.syntax("Unexpected empty block", source)
                set(lastIndex, last.convertLast(name, rtn))
            }

            is Expression -> {
                return if (name != null) creator.binary(
                    ASSIGN,
                    creator.callVar(name, getType(), source),
                    this,
                    source
                ) else if (rtn)
                    creator.createReturn(block.label, this, source)
                else this
            }

            else -> Err.type("Unexpected node type: $this", source)
        }
        return this
    }
}