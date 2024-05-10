package com.skillw.asaka.impl.codegen

import com.skillw.asaka.core.ir.data.MethodInfo
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.ast.MethodDefinition
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.error.Err
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

/**
 * ASM 相关工具方法
 *
 * 我知道ASM里面有很多重复的工具类，因为要改点东西，所以就自己写了
 */


internal fun BytecodeGenerator.cast(expr: Expression, from: AsakaType, to: AsakaType, safely: Boolean) {
    if (from == to) {
        code(expr)
        return
    }
    with(method) {
        if (to == A_STRING) {
            code(expr)
            visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false)
            return
        }
        if (from == A_STRING) {
            TODO() // 类型检查时已排除
        }
        if (to.isNative) {
            if (!from.isNative) Err.type("Unsupported type: ${from.display()} to ${to.display()}", expr.source)
            when {
                from.isPrimitive && to.isPrimitive -> {
                    val opcode = from.to(to, expr.source)
                    code(expr)
                    visitInsn(opcode)
                }

                from.isPrimitive && to.isBoxed && from.isBox(to) -> {
                    boxing(to.internalName) {
                        code(expr)
                    }
                }

                from.isBoxed && to.isPrimitive && to.isBox(from) -> {
                    unboxing(from.internalName) {
                        code(expr)
                    }
                }

                from.isBoxed && to.isBoxed && from.isBox(to) -> {
                    code(expr)
                }

                from.isBoxed && to.isBoxed -> {
                    val opcode = from.unboxType.to(to.unboxType, expr.source)
                    boxing(to.internalName) {
                        unboxing(from.internalName) {
                            code(expr)
                        }
                        visitInsn(opcode)
                    }
                }

                else -> Err.type("Unsupported type: ${from.display()} to ${to.display()}", expr.source)
            }
            return
        }
        code(expr)
        if (!safely)
            visitTypeInsn(CHECKCAST, to.internalName)
        else {
            val failed = Label()
            visitInsn(DUP)
            visitTypeInsn(INSTANCEOF, to.internalName)
            visitJumpInsn(IFEQ, failed)
            visitTypeInsn(CHECKCAST, to.internalName)
            visitLabel(failed)
            visitInsn(POP)
            visitInsn(ACONST_NULL)
        }
    }
}

internal enum class CompareType {
    EQ, NE, GT, LT, GE, LE
}

internal fun MethodVisitor.compare(type: AsakaType, compare: CompareType) {
    when (type) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> when (compare) {
            CompareType.EQ -> {
                val eq = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPEQ, eq)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(eq)
                visitInsn(ICONST_1)
                visitLabel(end)
            }

            CompareType.NE -> {
                val ne = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPNE, ne)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(ne)
                visitInsn(ICONST_1)
                visitLabel(end)
            }

            CompareType.GT -> {
                val gt = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPGT, gt)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(gt)
                visitInsn(ICONST_1)
                visitLabel(end)
            }

            CompareType.LT -> {
                val lt = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPLT, lt)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(lt)
                visitInsn(ICONST_1)
                visitLabel(end)
            }

            CompareType.GE -> {
                val ge = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPGE, ge)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(ge)
                visitInsn(ICONST_1)
                visitLabel(end)
            }

            CompareType.LE -> {
                val le = Label()
                val end = Label()
                visitJumpInsn(IF_ICMPLE, le)
                visitInsn(ICONST_0)
                visitJumpInsn(GOTO, end)
                visitLabel(le)
                visitInsn(ICONST_1)
                visitLabel(end)
            }
        }

        else -> {
            when (type) {
                A_LONG -> visitInsn(LCMP)
                A_FLOAT -> visitInsn(FCMPG)
                A_DOUBLE -> visitInsn(DCMPG)
                else -> throw UnsupportedOperationException("Unsupported type: ${type.display()}")
            }
            when (compare) {
                CompareType.EQ -> {
                    visitInsn(ICONST_1)
                    visitInsn(IADD)
                }

                CompareType.NE -> {
                    val ne = Label()
                    val end = Label()
                    visitJumpInsn(IFNE, ne)
                    visitInsn(ICONST_0)
                    visitJumpInsn(GOTO, end)
                    visitLabel(ne)
                    visitInsn(ICONST_1)
                    visitLabel(end)
                }

                CompareType.GT -> {}

                CompareType.LT -> visitInsn(INEG)

                CompareType.GE -> {
                    val ge = Label()
                    val end = Label()
                    visitJumpInsn(IFGE, ge)
                    visitInsn(ICONST_0)
                    visitJumpInsn(GOTO, end)
                    visitLabel(ge)
                    visitInsn(ICONST_1)
                    visitLabel(end)
                }

                CompareType.LE -> {
                    val le = Label()
                    val end = Label()
                    visitJumpInsn(IFLE, le)
                    visitInsn(ICONST_0)
                    visitJumpInsn(GOTO, end)
                    visitLabel(le)
                    visitInsn(ICONST_1)
                    visitLabel(end)
                }

            }
        }
    }
}

internal fun MethodVisitor.compareNull(nonnull: Boolean) {
    val nonNull = Label()
    val end = Label()
    visitJumpInsn(IFNONNULL, nonNull)
    visitInsn(if (nonnull) ICONST_0 else ICONST_1)
    visitJumpInsn(GOTO, end)
    visitLabel(nonNull)
    visitInsn(if (nonnull) ICONST_1 else ICONST_0)
    visitLabel(end)
}

internal fun MethodVisitor.const(value: Any?) {
    when (value) {
        is Int -> const(value)
        is Long -> const(value)
        is Float -> const(value)
        is Double -> const(value)
        is String -> const(value)
        is Boolean -> const(value)
        is Char -> const(value)
        is Byte -> const(value)
        null -> visitInsn(ACONST_NULL)
    }
}

internal fun MethodVisitor.const(int: Int) {
    when (int) {
        in -32768..-129 -> visitIntInsn(SIPUSH, int)
        in -128..-2 -> visitIntInsn(BIPUSH, int)
        -1 -> visitInsn(ICONST_M1)
        0 -> visitInsn(ICONST_0)
        1 -> visitInsn(ICONST_1)
        2 -> visitInsn(ICONST_2)
        3 -> visitInsn(ICONST_3)
        4 -> visitInsn(ICONST_4)
        5 -> visitInsn(ICONST_5)
        in 6..127 -> visitIntInsn(BIPUSH, int)
        in 128..32767 -> visitIntInsn(SIPUSH, int)
        else -> visitLdcInsn(int)
    }
}

internal fun MethodVisitor.const(value: Double) {
    when (value) {
        0.0 -> visitInsn(DCONST_0)
        1.0 -> visitInsn(DCONST_1)
        else -> visitLdcInsn(value)
    }
}

internal fun MethodVisitor.const(value: Float) {
    when (value) {
        0.0f -> visitInsn(FCONST_0)
        1.0f -> visitInsn(FCONST_1)
        2.0f -> visitInsn(FCONST_2)
        else -> visitLdcInsn(value)
    }
}

internal fun MethodVisitor.const(value: Long) {
    when (value) {
        0L -> visitInsn(LCONST_0)
        1L -> visitInsn(LCONST_1)
        else -> visitLdcInsn(value)
    }
}

internal fun MethodVisitor.const(value: String) {
    visitLdcInsn(value)
}

internal fun MethodVisitor.const(bool: Boolean) {
    if (bool) {
        visitInsn(ICONST_1)
    } else {
        visitInsn(ICONST_0)
    }
}

internal fun MethodVisitor.const(char: Char) {
    const(char.code)
}

internal fun MethodVisitor.const(byte: Byte) {
    const(byte.toInt())
}

internal val AsakaType.defaultValue: Any?
    get() {
        return when (this) {
            A_INT -> 0
            A_LONG -> 0L
            A_FLOAT -> 0.0f
            A_DOUBLE -> 0.0
            A_BOOLEAN -> false
            A_VOID -> null
            A_BYTE -> 0.toByte()
            A_CHAR -> 0.toChar()
            else -> null
        }
    }

internal fun String.toInternalName(): String {
    return this.replace(".", "/")
}

internal val TypeRef.descriptor: String
    get() = confirm().descriptor


internal val TypeRef.internalName: String
    get() = confirm().internalName

internal val AsakaType.descriptor: String
    get() {
        return when (this) {
            A_INT -> "I"
            A_LONG -> "J"
            A_FLOAT -> "F"
            A_DOUBLE -> "D"
            A_BOOLEAN -> "Z"
            A_VOID -> "V"
            A_BYTE -> "B"
            A_CHAR -> "C"
            else -> "L${name.toInternalName()};"
        }
    }

internal val String.boxType: String
    get() = when (this) {
        "I" -> "java/lang/Integer"
        "J" -> "java/lang/Long"
        "F" -> "java/lang/Float"
        "D" -> "java/lang/Double"
        "S" -> "java/lang/Short"
        "B" -> "java/lang/Byte"
        "C" -> "java/lang/Character"
        "Z" -> "java/lang/Boolean"
        else -> this
    }
internal val String.boxDescriptor: String
    get() = when (this) {
        "I" -> "Ljava/lang/Integer;"
        "J" -> "Ljava/lang/Long;"
        "F" -> "Ljava/lang/Float;"
        "D" -> "Ljava/lang/Double;"
        "S" -> "Ljava/lang/Short;"
        "B" -> "Ljava/lang/Byte;"
        "C" -> "Ljava/lang/Character;"
        "Z" -> "Ljava/lang/Boolean;"
        else -> this
    }
internal val MethodDefinition.descriptor: String
    get() {
        val builder = StringBuilder("(")
        this.params.forEach { builder.append(it.type.confirm().descriptor) }
        builder.append(")")
        builder.append(confirmedType().descriptor)
        return builder.toString()
    }

internal val MethodInfo.descriptor: String
    get() {
        val builder = StringBuilder("(")
        this.paramTypes.forEach { builder.append(it.descriptor) }
        builder.append(")")
        builder.append(returnType.descriptor)
        return builder.toString()
    }

internal fun commonHandle() = Handle(
    H_INVOKESTATIC,
    "java/lang/invoke/LambdaMetafactory",
    "metafactory",
    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
    false
)

const val path = "asaka/jvm/internal/Ref"

internal val AsakaType.wide: Boolean
    get() {
        return when (this) {
            A_LONG,
            A_DOUBLE,
            -> true

            else -> false
        }
    }

internal val AsakaType.ref: Pair<String, String>
    get() = when (this) {
        A_INT -> "$path\$IntRef" to "I"
        A_BYTE -> "$path\$ByteRef" to "B"
        A_CHAR -> "$path\$CharRef" to "C"
        A_SHORT -> "$path\$ShortRef" to "S"
        A_LONG -> "$path\$LongRef" to "J"
        A_FLOAT -> "$path\$FloatRef" to "F"
        A_DOUBLE -> "$path\$DoubleRef" to "D"
        A_BOOLEAN -> "$path\$BooleanRef" to "Z"
        else -> "$path\$ObjectRef" to "Ljava/lang/Object;"
    }


fun String.self(selfName: String) = if (this == "self") selfName else this



