package com.skillw.asaka.core.ir.member

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.*
import com.skillw.asaka.core.ir.data.*
import com.skillw.asaka.core.ir.type.*

/**
 * @className AsahiMethod
 * @author Glom
 * @date 2024/2/7 18:16
 * Copyright 2024 @Glom.
 */
open class AsahiField(
    source: Span,
    name: String,
    type: TypeInst,
    val self: AsakaType,
    override val modifiersSet: Set<Modifier>,
) : Serializable, AsahiVariable(
    source,
    name,
    !modifiersSet.contains(Modifier.FINAL),
    type,
    true,
    path = (self as? AsakaClass?)?.name ?: "java"
), ModifierHolder {

    override fun display(): String {
        val modifiersDisplay = modifiersSet.joinToString { it.name.lowercase() }
        return "$modifiersDisplay $name: ${type.display()}"
    }

    override fun serialize() = linkedMapOf<String, Any>(
        "type" to "field",
        "name" to name,
        "mutable" to mutable,
        "type" to type.display(),
    )

    fun toCallSite(self: Expression): FieldCallSite {
        return FieldCallSite(self, name, type, modifiers.isStatic, this.self)
    }

    fun toPutSite(self: Expression, value: Expression): FieldPutSite {
        return FieldPutSite(self, name, type, modifiers.isStatic, value, this.self)
    }

    fun toRefSite(self: Expression): FieldRefSite {
        return FieldRefSite(self, name, type, modifiers.isStatic, this.self)
    }

}

abstract class AsahiMember(
    private val givenSource: Span,
    var name: String
) {
    val source: Span
        get() {
            if (givenSource.isEmpty) givenSource.native = display()
            return givenSource
        }

    abstract fun display(): String
}

/**
 * @className AsahiMethod
 * @author Glom
 * @date 2024/2/7 18:16
 * Copyright 2024 @Glom.
 */
open class AsahiMethod(
    source: Span,
    name: String,
    val returnType: TypeInst,
    val params: List<AsahiParameter>,
    val generics: List<GenericType>,
    override val modifiersSet: Set<Modifier>,
    val self: AsakaType,
    var body: MethodBlock? = null
) : AsahiMember(source, name), Serializable, ModifierHolder {

    override fun display(): String {
        val genericsDisplay = if (generics.isNotEmpty())
            generics.joinToString(prefix = "<", separator = ",", postfix = ">") { it.display() }
        else ""
        val paramsDisplay = params.joinToString(prefix = "(", separator = ", ", postfix = ")") {
            it.type.display()
        }
        val modifiersDisplay = modifiersSet.joinToString { it.name.lowercase() }

        return "$modifiersDisplay fun$genericsDisplay $name$paramsDisplay: ${returnType.display()}"
    }

    override fun serialize() = linkedMapOf(
        "type" to "method",
        "name" to name,
        "flags" to modifiersSet.map { it.name.lowercase() },
        "returnType" to returnType.display(),
        "generics" to generics.map { it.serialize() },
        "parameters" to params.map(Serializable::serialize),
        if (body != null) "body" to body!!.serialize() else "body" to "undefined"
    )

    fun toCallSite(self: Expression, args: List<Expression>) = MethodCallSite(
        self,
        name,
        this.params.map { it.type },
        args,
        returnType,
        modifiers.isStatic,
        this.self
    )

    fun toRefSite(self: Expression) = MethodRefSite(
        self,
        name,
        this.params.map { it.type },
        returnType,
        modifiers.isStatic,
        this.self
    )

    fun toRef(argData: InvokeData) = AsahiMethodRef(this, argData)
}

class AsahiParameter(
    name: String,
    var type: TypeInst,
    source: Span = type.source,
    var noinline: Boolean = false,
    val default: Expression? = null,
) : Serializable, AsahiMember(source, name) {

    override fun display(): String {
        val noinlineDisplay = if (noinline) "noinline " else ""
        return "$noinlineDisplay$name: ${type.display()}"
    }

    fun hashcode(blc: Block) = "${blc.path}/$name".hashCode()

    override fun hashCode(): Int {
        return name.hashCode() + type.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AsahiParameter) return false
        return name == other.name && type == other.type
    }

    override fun serialize() = linkedMapOf(
        "type" to "parameter",
        "name" to name,
        "type" to type.display(),
    )

    fun toVar(block: Block): AsahiVariable {
        return AsahiVariable(source, name, false, type, false, path = block.path)
    }

    fun toRef(generics: Map<String, TypeRef>) = AsahiParameterRef(this, generics)

    fun clone() = AsahiParameter(name, type.clone(), source)
}

open class AsahiVariable(
    source: Span,
    name: String,
    val mutable: Boolean,
    val type: TypeInst,
    val field: Boolean,
    val value: Expression? = null,
    var ref: Boolean = false,
    var inline: Boolean = false,
    val path: String,
) : AsahiMember(source, name), Serializable, ConstExpr {

    var const: LiteralExpression? = null

    override fun display(): String {
        val mutableDisplay = if (mutable) "var" else "val"
        val fieldDisplay = if (field) "field " else ""
        val refDisplay = if (ref) "ref " else ""
        val inlineDisplay = if (inline) "inline " else ""
        return "$mutableDisplay $fieldDisplay$refDisplay$inlineDisplay$name: ${type.display()}, path: $path"

    }

    open fun clone(blc: MethodBlock) =
        AsahiVariable(source, name, mutable, type.clone(), field, value, ref, inline, blc.path)

    override fun serialize() = linkedMapOf<String, Any>(
        "type" to "variable",
        "name" to name,
        "mutable" to mutable,
        "type" to type.display(),
        "inline" to inline
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AsahiVariable) return false

        if (source != other.source) return false
        if (name != other.name) return false
        if (mutable != other.mutable) return false
        if (type != other.type) return false
        if (field != other.field) return false
        if (ref != other.ref) return false
        if (inline != other.inline) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return (path + name).hashCode()
    }


    fun toRef(generics: Map<String, TypeRef>) = AsahiVariableRef(this, generics)

}

class Constructor(
    source: Span,
    params: List<AsahiParameter>,
    modifiers: Set<Modifier>,
    self: AsakaType,
    body: MethodBlock,
) : AsahiMethod(
    source, "<init>", self.toInst(source), params, emptyList(), modifiers, self, body
)

open class InlineLambda(
    source: Span,
    name: String,
    mutable: Boolean,
    type: TypeInst,
    field: Boolean,
    ref: Boolean = false,
    val lambda: LambdaExpression,
    block: Block,
) : AsahiVariable(source, name, mutable, type, field, lambda, ref, true, block.path) {

    override fun clone(blc: MethodBlock) = InlineLambda(
        source, name, mutable, type.clone(), field, ref,
        lambda.clone(blc) as LambdaExpression, blc
    )

    override fun serialize(): LinkedHashMap<String, Any> = linkedMapOf(
        "type" to "variable",
        "name" to name,
        "mutable" to mutable,
        "type" to type.display()
    )
}