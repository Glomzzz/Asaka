package com.skillw.asaka.impl.codegen

import com.skillw.asaka.core.ir.type.TypeRef
import com.skillw.asaka.core.ir.type.isBoxed
import com.skillw.asaka.core.ir.type.isPrimitive
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal inline fun MethodVisitor.unboxing(internal: String, todo: () -> Unit) {
    todo()
    when (internal) {
        "java/lang/Integer", "I" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
        }

        "java/lang/Long", "J" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false)
        }

        "java/lang/Float", "F" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false)
        }

        "java/lang/Double", "D" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false)
        }

        "java/lang/Boolean", "Z" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
        }

        "java/lang/Byte", "B" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false)
        }

        "java/lang/Character", "C" -> {
            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character")
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false)
        }

        else -> {
            visitTypeInsn(Opcodes.CHECKCAST, internal)
        }
    }
}

internal inline fun MethodVisitor.boxing(type: String, todo: () -> Unit) {
    val boxType = type.boxType
    if (boxType != type) {
        visitTypeInsn(Opcodes.NEW, boxType)
        visitInsn(Opcodes.DUP)
    }
    todo()
    if (boxType != type)
        visitMethodInsn(Opcodes.INVOKESPECIAL, boxType, "<init>", "($type)V", false)
}

internal inline fun MethodVisitor.used(typeRef: TypeRef, used: Boolean, todo: () -> Unit) {
    todo()
    if (!used && !typeRef.void())
        visitInsn(Opcodes.POP)
    else if (used && typeRef.void())
        visitFieldInsn(
            Opcodes.GETSTATIC,
            "com/skillw/asaka/core/ast/type/Unit",
            "INSTANCE",
            "Lcom/skillw/asaka/core/ast/type/Unit;"
        )
}

internal inline fun MethodVisitor.autoBox(typeRef: TypeRef, box: Boolean, todo: () -> Unit) {
    val type = typeRef.confirm()
    when {
        box && type.isPrimitive ->
            boxing(type.internalName, todo)

        !box && type.isBoxed ->
            unboxing(type.internalName, todo)

        else ->
            todo()
    }
}

internal inline fun MethodVisitor.auto(typeRef: TypeRef, box: Boolean, used: Boolean = true, todo: () -> Unit) {
    used(typeRef, used) {
        autoBox(typeRef, box, todo)
    }
}

