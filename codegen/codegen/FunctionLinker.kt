package com.skillw.asaka.impl.codegen

import com.skillw.asaka.core.ir.data.FieldRefSite
import com.skillw.asaka.core.ir.data.MethodRefSite
import com.skillw.asaka.core.ir.data.RefSite
import com.skillw.asaka.core.ir.type.AsakaType
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

data class FunctionLinker(val type: AsakaType, val method: String, val asmType: Type) {
    val clazzName: String = type.name
    val internalName: String = type.internalName
    fun descriptor(params: String): String = "($params)L$internalName;"

    fun generateHandle(site: RefSite, selfName: String): Handle = when (site) {
        is FieldRefSite -> generateHandle(site, selfName)
        is MethodRefSite -> generateHandle(site, selfName)
    }

    fun generateType(site: RefSite): Type = when (site) {
        is FieldRefSite -> generateType(site)
        is MethodRefSite -> generateType(site)
    }

    fun generateHandle(site: MethodRefSite, selfName: String): Handle {
        val (_, name, _, _, static, source, isInterface) = site
        val tag =
            if (static) Opcodes.H_INVOKESTATIC else if (isInterface) Opcodes.H_INVOKEINTERFACE else Opcodes.H_INVOKEVIRTUAL

        return Handle(
            tag,
            source.name.self(selfName).toInternalName(),
            name,
            site.descriptor,
            isInterface
        )
    }

    fun generateType(site: MethodRefSite): Type {
        val (_, _, params, rtn, _, _, _) = site
        val paramTypes = params.map { Type.getType(it.descriptor.boxDescriptor) }.toTypedArray()
        val returnType = Type.getType(rtn.descriptor.boxDescriptor)
        return Type.getMethodType(returnType, *paramTypes)
    }

    fun generateHandle(site: FieldRefSite, selfName: String): Handle {
        val (_, name, type, static, source, isInterface) = site
        val tag = if (static) Opcodes.H_GETSTATIC else Opcodes.H_GETFIELD
        return Handle(
            tag,
            source.name.self(selfName).toInternalName(),
            name,
            type.descriptor,
            isInterface
        )
    }

    fun generateType(site: FieldRefSite): Type {
        val (_, _, type, _, _, _) = site
        val paramTypes = emptyArray<Type>()
        val returnType = Type.getType(type.internalName.boxDescriptor)
        return Type.getMethodType(returnType, *paramTypes)
    }

    inline fun invoke(visit: (Int, String, String, String, Boolean) -> Unit) {
        visit(Opcodes.INVOKEINTERFACE, internalName, method, asmType.descriptor, true)
    }
}

