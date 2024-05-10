package com.skillw.asaka.impl.codegen

import com.skillw.asaka.asaka.jvm.functions.*
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.toType
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Modifier

private val functions = hashMapOf<String, FunctionLinker>()

internal fun Class<*>.getFunction() =
    if (isAnnotationPresent(FunctionalInterface::class.java))
        declaredMethods.firstOrNull { method -> !method.isDefault && Modifier.isAbstract(method.modifiers) }
    else null

internal fun Class<*>.funcLink(): FunctionLinker? = functions.getOrPut(name) {
    val method = getFunction() ?: return null
    val name = method.name
    val type = Type.getType(method)
    FunctionLinker(toType(), name, type)
}

internal fun function(params: Int, void: Boolean): FunctionLinker? {
    return if (void) when (params) {
        0 -> AFunctionV0::class.java.funcLink()
        1 -> AFunctionV1::class.java.funcLink()
        2 -> AFunctionV2::class.java.funcLink()
        3 -> AFunctionV3::class.java.funcLink()
        4 -> AFunctionV4::class.java.funcLink()
        5 -> AFunctionV5::class.java.funcLink()
        6 -> AFunctionV6::class.java.funcLink()
        7 -> AFunctionV7::class.java.funcLink()
        8 -> AFunctionV8::class.java.funcLink()
        9 -> AFunctionV9::class.java.funcLink()
        10 -> AFunctionV10::class.java.funcLink()
        11 -> AFunctionV11::class.java.funcLink()
        12 -> AFunctionV12::class.java.funcLink()
        13 -> AFunctionV13::class.java.funcLink()
        14 -> AFunctionV14::class.java.funcLink()
        15 -> AFunctionV15::class.java.funcLink()
        16 -> AFunctionV16::class.java.funcLink()
        17 -> AFunctionV17::class.java.funcLink()
        18 -> AFunctionV18::class.java.funcLink()
        19 -> AFunctionV19::class.java.funcLink()
        20 -> AFunctionV20::class.java.funcLink()
        21 -> AFunctionV21::class.java.funcLink()
        22 -> AFunctionV22::class.java.funcLink()
        else -> null
    } else when (params) {
        0 -> AFunction0::class.java.funcLink()
        1 -> AFunction1::class.java.funcLink()
        2 -> AFunction2::class.java.funcLink()
        3 -> AFunction3::class.java.funcLink()
        4 -> AFunction4::class.java.funcLink()
        5 -> AFunction5::class.java.funcLink()
        6 -> AFunction6::class.java.funcLink()
        7 -> AFunction7::class.java.funcLink()
        8 -> AFunction8::class.java.funcLink()
        9 -> AFunction9::class.java.funcLink()
        10 -> AFunction10::class.java.funcLink()
        11 -> AFunction11::class.java.funcLink()
        12 -> AFunction12::class.java.funcLink()
        13 -> AFunction13::class.java.funcLink()
        14 -> AFunction14::class.java.funcLink()
        15 -> AFunction15::class.java.funcLink()
        16 -> AFunction16::class.java.funcLink()
        17 -> AFunction17::class.java.funcLink()
        18 -> AFunction18::class.java.funcLink()
        19 -> AFunction19::class.java.funcLink()
        20 -> AFunction20::class.java.funcLink()
        21 -> AFunction21::class.java.funcLink()
        22 -> AFunction22::class.java.funcLink()
        else -> null
    }
}


internal inline fun MethodVisitor.ref(type: AsakaType, todo: () -> Unit) {
    val (ref, descriptor) = type.ref
    visitTypeInsn(Opcodes.NEW, ref)
    visitInsn(Opcodes.DUP)
    todo()
    visitMethodInsn(Opcodes.INVOKESPECIAL, ref, "<init>", "($descriptor)V", false)

}