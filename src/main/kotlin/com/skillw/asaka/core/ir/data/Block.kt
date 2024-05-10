package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.ir.ast.MethodBlock
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.core.ir.member.AsahiVariable

open class BlockContext(
    val path: String,
    open val variables: MutableMap<String, AsahiVariable> = mutableMapOf(),
    open val methods: MutableMap<MethodKey, AsahiMethod> = mutableMapOf()
) : Serializable {
    override fun serialize() = linkedMapOf(
        "type" to "context",
        "variables" to variables.map { it.value.serialize() },
        "methods" to methods.values.map(Serializable::serialize),
    )

    fun clone(blc: MethodBlock): BlockContext {
        val context = BlockContext(blc.path)
        context.variables.putAll(variables.mapValues { it.value.clone(blc) })
        context.methods.putAll(methods)
        return context
    }
}