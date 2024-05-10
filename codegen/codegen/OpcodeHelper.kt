package com.skillw.asaka.impl.codegen

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.type.*
import org.objectweb.asm.Opcodes

internal val AsakaType.rtn: Int
    get() {
        return when (this) {
            A_INT -> Opcodes.IRETURN
            A_BYTE -> Opcodes.IRETURN
            A_CHAR -> Opcodes.IRETURN
            A_BOOLEAN -> Opcodes.IRETURN
            A_LONG -> Opcodes.LRETURN
            A_FLOAT -> Opcodes.FRETURN
            A_DOUBLE -> Opcodes.DRETURN
            A_VOID -> Opcodes.RETURN
            else -> Opcodes.ARETURN
        }
    }
internal val AsakaType.load: Int
    get() {
        return when (this) {
            A_INT -> Opcodes.ILOAD
            A_BOOLEAN -> Opcodes.ILOAD
            A_BYTE -> Opcodes.ILOAD
            A_CHAR -> Opcodes.ILOAD
            A_LONG -> Opcodes.LLOAD
            A_FLOAT -> Opcodes.FLOAD
            A_DOUBLE -> Opcodes.DLOAD
            else -> Opcodes.ALOAD
        }
    }
internal val AsakaType.store: Int
    get() {
        return when (this) {
            A_INT -> Opcodes.ISTORE
            A_BOOLEAN -> Opcodes.ISTORE
            A_BYTE -> Opcodes.ISTORE
            A_CHAR -> Opcodes.ISTORE
            A_LONG -> Opcodes.LSTORE
            A_FLOAT -> Opcodes.FSTORE
            A_DOUBLE -> Opcodes.DSTORE
            else -> Opcodes.ASTORE
        }
    }
internal val AsakaType.add: Int
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.IADD

        A_LONG -> Opcodes.LADD
        A_FLOAT -> Opcodes.FADD
        A_DOUBLE -> Opcodes.DADD
        else -> throw UnsupportedOperationException("Unsupported type: ${display()}")
    }
internal val AsakaType.sub: Int
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.ISUB

        A_LONG -> Opcodes.LSUB
        A_FLOAT -> Opcodes.FSUB
        A_DOUBLE -> Opcodes.DSUB
        else -> throw UnsupportedOperationException("Unsupported type: ${this.display()}")
    }
internal val AsakaType.mul: Int
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.IMUL

        A_LONG -> Opcodes.LMUL
        A_FLOAT -> Opcodes.FMUL
        A_DOUBLE -> Opcodes.DMUL
        else -> throw UnsupportedOperationException("Unsupported type: ${display()}")
    }
internal val AsakaType.div: Int
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.IDIV

        A_LONG -> Opcodes.LDIV
        A_FLOAT -> Opcodes.FDIV
        A_DOUBLE -> Opcodes.DDIV
        else -> throw UnsupportedOperationException("Unsupported type: ${display()}")
    }
internal val AsakaType.rem: Int
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.IREM

        A_LONG -> Opcodes.LREM
        A_FLOAT -> Opcodes.FREM
        A_DOUBLE -> Opcodes.DREM
        else -> throw UnsupportedOperationException("Unsupported type: ${display()}")
    }
internal val AsakaType.toD: Int?
    get() = when (this) {
        A_BYTE,
        A_CHAR,
        A_SHORT,
        A_INT,
        -> Opcodes.I2D

        A_LONG -> Opcodes.L2D
        A_FLOAT -> Opcodes.F2D
        A_DOUBLE -> null
        else -> throw UnsupportedOperationException("Unsupported type: ${display()}")
    }
internal val String.aaload: Int
    get() = when (this) {
        "I" -> Opcodes.IALOAD
        "J" -> Opcodes.LALOAD
        "F" -> Opcodes.FALOAD
        "D" -> Opcodes.DALOAD
        "S" -> Opcodes.SALOAD
        "B" -> Opcodes.BALOAD
        "C" -> Opcodes.CALOAD
        "Z" -> Opcodes.BALOAD
        else -> Opcodes.AALOAD
    }


internal fun AsakaType.to(to: AsakaType, source: Span): Int {
    return when (this) {
        A_INT -> when (to) {
            A_LONG -> Opcodes.I2L
            A_FLOAT -> Opcodes.I2F
            A_DOUBLE -> Opcodes.I2D
            A_BYTE -> Opcodes.I2B
            A_CHAR -> Opcodes.I2C
            A_SHORT -> Opcodes.I2S
            else -> null
        }

        A_LONG -> when (to) {
            A_INT -> Opcodes.L2I
            A_FLOAT -> Opcodes.L2F
            A_DOUBLE -> Opcodes.L2D
            else -> null
        }

        A_FLOAT -> when (to) {
            A_INT -> Opcodes.F2I
            A_LONG -> Opcodes.F2L
            A_DOUBLE -> Opcodes.F2D
            else -> null
        }

        A_DOUBLE -> when (to) {
            A_INT -> Opcodes.D2I
            A_LONG -> Opcodes.D2L
            A_FLOAT -> Opcodes.D2F
            else -> null
        }

        A_BYTE -> when (to) {
            A_INT -> Opcodes.I2B
            A_LONG -> Opcodes.I2L
            A_FLOAT -> Opcodes.I2F
            A_DOUBLE -> Opcodes.I2D
            A_SHORT -> Opcodes.I2S
            else -> null
        }

        A_CHAR -> when (to) {
            A_INT -> Opcodes.I2C
            A_LONG -> Opcodes.I2L
            A_FLOAT -> Opcodes.I2F
            A_DOUBLE -> Opcodes.I2D
            else -> null
        }

        A_SHORT -> when (to) {
            A_INT -> Opcodes.I2S
            A_LONG -> Opcodes.I2L
            A_FLOAT -> Opcodes.I2F
            A_DOUBLE -> Opcodes.I2D
            else -> null
        }

        else -> null
    } ?: Err.type("Unsupported type: ${this.display()} to ${to.display()}", source)
}

