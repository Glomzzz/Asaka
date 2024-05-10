package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.TypeInst

sealed interface RefSite : Serializable {
    val self: Expression
    val name: String
    val static: Boolean
    val source: AsakaType
    val returnType: TypeInst
    val isInterface: Boolean
}

data class FieldRefSite(
    override val self: Expression,
    override val name: String,
    override val returnType: TypeInst,
    override val static: Boolean,
    override val source: AsakaType = self.getType().confirm(),
    override val isInterface: Boolean = source.modifiers.isInterface
) : RefSite {
    override fun serialize() = linkedMapOf(
        "ref" to "field-get",
        "self" to self.serialize(),
        "name" to name,
        "static" to static,
        "source" to source.serialize(),
        "returnType" to returnType.serialize(),
    )
}

data class MethodRefSite(
    override val self: Expression,
    override val name: String,
    override val paramTypes: List<TypeInst>,
    override val returnType: TypeInst,
    override val static: Boolean,
    override val source: AsakaType = self.getType().confirm(),
    override val isInterface: Boolean = source.modifiers.isInterface
) : RefSite, MethodInfo {
    override fun serialize() = linkedMapOf(
        "ref" to "method-get",
        "self" to self.serialize(),
        "name" to name,
        "paramTypes" to paramTypes.map { it.serialize() },
        "returnType" to returnType.serialize(),
        "static" to static,
        "source" to source.serialize(),
    )
}