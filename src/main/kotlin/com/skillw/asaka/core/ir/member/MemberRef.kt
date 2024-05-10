package com.skillw.asaka.core.ir.member

import com.skillw.asaka.core.ir.data.InvokeData
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.TypeRef

interface AsahiMemberRef<M : AsahiMember> {

    val member: M
}

class AsahiMethodRef(override val member: AsahiMethod, val invokeData: InvokeData) : AsahiMemberRef<AsahiMethod> {
    private val generics = invokeData.generics
    val name: String = member.name
    val returnType: TypeInst = member.returnType.toRef().loadGeneric(generics)
    val params: List<AsahiParameterRef> = member.params.map { it.toRef(generics) }
}

class AsahiParameterRef(
    override val member: AsahiParameter,
    generics: Map<String, TypeRef>
) : AsahiMemberRef<AsahiParameter> {
    val name = member.name
    var type = member.type.toRef().loadGeneric(generics)
}

class AsahiVariableRef(
    override val member: AsahiVariable,
    generics: Map<String, TypeRef>
) : AsahiMemberRef<AsahiVariable> {
    val name = member.name
    val returnType = member.type.toRef().loadGeneric(generics)
}