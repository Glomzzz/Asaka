package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.ir.type.A_OBJECT
import com.skillw.asaka.core.ir.type.A_VOID
import com.skillw.asaka.core.ir.type.toAsaka
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.ClassBlock
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMember
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.util.unsafeLazy

@Suppress("UNCHECKED_CAST")
open class AsakaClass(
    source: Span,
    name: String,
    override val modifiersSet: Set<Modifier>,
    override val superTypes: Set<TypeInst> = emptySet(),
    override val genericTypes: List<GenericType> = emptyList(),
) : AsahiMember(source, name), AsakaType {

    lateinit var body: ClassBlock
    override val fields by unsafeLazy {
        body.context.variables as MutableMap<String, AsahiField>
    }
    override val methods by unsafeLazy {
        body.context.methods
    }

    override fun isChildOf(type: AsakaType) = this == type || superTypes.any { it.isChildOf(type) }

    override fun display(): String {
        val genericsDisplay = if (genericTypes.isNotEmpty()) genericTypes.joinToString(
            prefix = "<",
            postfix = ">"
        ) { it.display() } else ""

        val superTypesDisplay =
            if (superTypes.isNotEmpty()) superTypes.joinToString(prefix = " : ", transform = { it.display() }) else ""

        val modifiersDisplay = modifiersSet.joinToString(prefix = "", postfix = "") { it.name.lowercase() }

        return "$modifiersDisplay $name$genericsDisplay$superTypesDisplay"
    }

    override fun serialize(): LinkedHashMap<String, Any> = linkedMapOf(
        "type" to "asaka-class",
        "modifiers" to modifiersSet.map { it.name.lowercase() },
        "name" to name,
        "fields" to fields.values.map { it.serialize() },
        "methods" to methods.values.map { it.serialize() },
    )

}

object ArrayType : AsakaType {
    override val genericTypes = listOf(GenericType("T"))
    override val fields = null

    override val methods = null
    override val superTypes = null
    override val modifiersSet = null
    override val name: String = "Array"
    override fun isChildOf(type: AsakaType) = this == type || type == A_OBJECT
    override fun serialize() = linkedMapOf(
        "type" to "array"
    )

}

class GenericType(
    override val name: String,
    val upper: MutableList<TypeRef> = ArrayList(),
    val variance: Variance = Variance.NONE,
    var reified: Boolean = false,
    var ownerType: AsakaType? = null
) : MixedType(upper.toSet()) {
    override val genericTypes = null
    override val modifiersSet = null
    var source = Span.EMPTY
    override fun display(): String {
        return when (variance) {
            Variance.NONE -> ""
            Variance.IN -> "in "
            Variance.OUT -> "out "
        } + name + if (upper.isNotEmpty()) " <: ${
            upper.joinToString(", ") {
                if (it.generic()) it.asGeneric().display() else it.display()
            }
        }" else ""
    }

    override fun isChildOf(type: AsakaType): Boolean {
        return when (type) {
            is GenericType -> type.upper.all { expect -> upper.any { actual -> expect.isAssignableBy(actual) } }
            else -> upper.any { type.toInst().isAssignableBy(it) }
        }
    }

    fun source(source: Span): GenericType {
        this.source = source
        return this
    }

    override fun serialize() = linkedMapOf(
        "type" to "generic",
        "name" to name,
        "upper" to upper.map(TypeInst::serialize),
        "variance" to variance.name,
        "reified" to reified,
    )

    fun isStar() = name == "*" || name == "?"

    fun check(actual: TypeInst) = when {
        // 通配符
        this.isStar() || this == actual.type -> true
        // 如果actual是泛型, 则检查actual的上界是否包含所有当前泛型的上界
        actual.generic() -> actual.asGeneric().let {
            upper.all { require ->
                require.any() || it.upper.any { upper -> require.isAssignableBy(upper) }
            }
        }

        // 如果actual是普通类型, 则检查actual是否同时是当前所有上界的子类型
        upper.all { it.isAssignableBy(actual) } -> true

        else -> false
    }


    fun clone() = GenericType(name, upper.map { it.clone() }.toMutableList(), variance, reified, ownerType).also {
        it.source = source
    }

    fun verify(actual: TypeInst) {
        if (!check(actual))
            Err.type("Type ${display()} is not assignable from ${actual.display()}", source)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericType) return false

        return when {
            name != other.name -> false
            variance != other.variance -> false
            reified != other.reified -> false
            ownerType != other.ownerType -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + variance.hashCode()
        result = 31 * result + reified.hashCode()
        result = 31 * result + (ownerType?.hashCode() ?: 0)
        return result
    }

    enum class Variance {
        IN, OUT, NONE
    }

}

interface JavaClass : AsakaType {
    val clazz: Class<*>
    val primitive: Boolean
    override val name: String
    override val genericTypes: List<GenericType>
    override val fields: MutableMap<String, AsahiField>
    override val methods: MutableMap<MethodKey, out AsahiMethod>
    override val superTypes: Set<TypeInst>

    companion object {
        fun with(clazz: Class<*>) = clazz.toAsaka()
    }
}

class LambdaType(
    val source: Span,
    val paramTypes: List<TypeRef>,
    val returnType: TypeRef,
) : AsakaType {
    override val genericTypes = null
    override val superTypes = null
    override val fields = null

    override val methods = null
    override val modifiersSet = null
    override val name
        get() = display()

    override fun isChildOf(type: AsakaType) = this == type || type is LambdaType

    override fun display(): String =
        "(${paramTypes.joinToString(", ") { it.display() }}) -> ${returnType.display()}"


    override fun serialize() = linkedMapOf(
        "type" to "lambda",
        "params" to paramTypes.map { it.serialize() },
        "return" to returnType.serialize(),
    )

    fun clone(): LambdaType {
        return LambdaType(source, paramTypes.map { it.clone() }, returnType.clone())
    }

}

open class MixedType(override val superTypes: Set<TypeInst>) : AsakaType {
    override val name by unsafeLazy {
        superTypes.joinToString(" | ") { it.name }
    }
    override val genericTypes = null

    override val fields: MutableMap<String, AsahiField> by unsafeLazy {
        val fields = mutableMapOf<String, AsahiField>()
        superTypes.forEach { fields.putAll(it.fields) }
        fields
    }
    override val methods: MutableMap<MethodKey, out AsahiMethod> by unsafeLazy {
        val methods = mutableMapOf<MethodKey, AsahiMethod>()
        superTypes.forEach { methods.putAll(it.methods) }
        methods
    }
    override val modifiersSet = null

    override fun isChildOf(type: AsakaType) = false

    override fun serialize() = linkedMapOf(
        "type" to "mixed",
        "name" to display(),
        "superTypes" to superTypes.map(TypeInst::serialize),
    )

    companion object {
        fun TypeInst.mix(other: TypeInst) = if (other.confirm() == A_VOID)
            A_OBJECT.toInst(other.source)
        else {
            val common = this.intersectSuperWith(other)
            if (common.isEmpty() || (common.size == 1 && common.first().confirm() == A_OBJECT)) {
                common.first()
            } else if (common.size == 1) {
                common.first()
            } else {
                MixedType(common).toInst(other.source)
            }
        }
    }
}

object Undefined : AsakaType {
    override val name: String = "Undefined"
    override val fields = null
    override val methods = null
    override val superTypes = null
    override val genericTypes = null
    override val modifiersSet = null

    override fun isChildOf(type: AsakaType) = true
    override fun serialize() = linkedMapOf("type" to name.lowercase())
}


