package com.skillw.asaka.impl.codegen

import com.skillw.asaka.core.ir.type.AsakaType
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

internal class Address(initSize: Int) {
    internal val map = HashMap<Int, Int>()
    private val refs = HashSet<Int>()
    private var currentOffset = initSize

    fun address(id: Int, wide: Boolean = false): Int = map.getOrPut(id) {
        if (wide) {
            val current = currentOffset; currentOffset += 2; current
        } else currentOffset++
    }

    inline fun storeRef(method: MethodVisitor, id: Int, type: AsakaType, value: MethodVisitor.() -> Unit) {
        refs.add(id)
        method.value()
        method.visitVarInsn(ASTORE, address(id, type.wide))
    }

    inline fun store(method: MethodVisitor, id: Int, type: AsakaType, value: MethodVisitor.() -> Unit) {
        if (refs.contains(id)) {
            method.visitVarInsn(ALOAD, address(id))
            method.value()
            val (ref, descriptor) = type.ref
            method.visitFieldInsn(PUTFIELD, ref, "value", descriptor)
            return
        }
        method.value()
        method.visitVarInsn(type.store, address(id, type.wide))
    }

    fun load(method: MethodVisitor, id: Int, type: AsakaType) {
        if (refs.contains(id)) {
            method.visitVarInsn(ALOAD, address(id))
            val (ref, descriptor) = type.ref
            method.visitFieldInsn(GETFIELD, ref, "value", descriptor)
            return
        }
        method.visitVarInsn(type.load, address(id))
    }

    fun loadRaw(method: MethodVisitor, id: Int, type: AsakaType) {
        method.visitVarInsn(type.load, address(id))
    }

    fun has(id: Int): Boolean = map.containsKey(id)
}