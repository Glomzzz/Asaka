package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.data.GenericsTable
import com.skillw.asaka.core.ir.data.IMemberRefGetter
import com.skillw.asaka.core.ir.data.MemberRefGetter
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.core.ir.member.AsahiMethodRef
import com.skillw.asaka.core.ir.member.AsahiVariableRef
import com.skillw.asaka.core.ir.type.MixedType.Companion.mix
import com.skillw.asaka.util.unsafeLazy

interface TypeInst : Serializable, IMemberRefGetter {
    var type: AsakaType
    var source: Span
    val name: String
    val superTypes: Set<TypeInst>
    val modifiers: Set<AsahiModifier>
    val fields: MutableMap<String, AsahiField>
    val methods: MutableMap<MethodKey, out AsahiMethod>
    val genericTypes: List<GenericType>
    var loose: Boolean
    var nullable: Boolean
    val generics: GenericsTable
    val getter: MemberRefGetter
    fun display(): String

    val inst: TypeInst
        get() {
            return if (this is TypeRef) this.inst else this
        }

    fun toRef() = if (this is TypeRef) this else TypeRef(this)

    override fun getMethods(
        name: String,
        argTypes: List<TypeInst>,
        args: Map<String, Expression>,
        argGenericTypes: List<TypeInst>
    ): Set<AsahiMethodRef>

    override fun getField(name: String): AsahiVariableRef?
    fun validate(): Boolean
    fun confirm(): AsakaType
    fun clone(): TypeInst

    override fun serialize(): Map<String, Any>
    fun hasGenerics(): Boolean
    fun known(): Boolean
    fun unknown(): Boolean
    fun void(): Boolean
    fun generic(): Boolean
    fun asGeneric(): GenericType
    fun lambda(): Boolean
    fun asLambda(): LambdaType
    fun java(): Boolean
    fun asJava(): JavaClass
    fun asaka(): Boolean
    fun asAsaka(): AsakaClass
    fun any(): Boolean
    fun completeWith(that: TypeInst)
    fun tryCompleteWith(that: TypeInst): String?
    fun isChildOf(superType: AsakaType): Boolean

    // 不检查泛型
    fun isChildOf(superType: TypeInst): Boolean
    fun completeWith(typeA: TypeInst, typeB: TypeInst?)
    fun assertType(other: AsakaType)
    fun isAssignableBy(thatInst: TypeInst): Boolean
    fun intersectSuperWith(other: TypeInst): Set<TypeInst>
    fun box(): TypeInst
    fun unbox(): TypeInst
    fun loose(): TypeInst

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int


    companion object {
        fun new(type: AsakaType, source: Span = Span.EMPTY) = TypeInstImpl(type, source).bootstrapGenerics()
        fun unknown(source: Span = Span.EMPTY) = new(Undefined, source)
        fun lambda(params: List<TypeInst>, returnType: TypeInst, source: Span) =
            new(LambdaType(source, params.map { it.toRef() }, returnType.toRef()), source)
    }

    fun searchSuperInst(type: AsakaType): TypeInst?
}

/**
 * @className TypeBuilder
 * @author Glom
 * @date 2024/2/7 18:04
 * Copyright 2024 @Glom.
 */
private class TypeInstImpl(
    type: AsakaType = Undefined, override var source: Span, val id: Int = count++
) : TypeInst {

    companion object {
        private var count: Int = 0
    }

    override var type: AsakaType = type
        set(value) {
            field = value
            generics.clear()
            bootstrapGenerics()
        }
    override val name
        get() = type.name
    override val superTypes: Set<TypeInst>
        get() = type.superTypes ?: Err.type(
            "TypeInst $name (${type.javaClass.simpleName}) has not super types",
            source
        )
    override val modifiers: Set<AsahiModifier>
        get() = type.modifiersSet ?: Err.type(
            "TypeInst $name (${type.javaClass.simpleName}) has not modifiers",
            source
        )
    override val fields: MutableMap<String, AsahiField>
        get() = type.fields ?: Err.type("Type $name (${type.javaClass.simpleName}) has not fields", source)

    override val methods: MutableMap<MethodKey, out AsahiMethod>
        get() = type.methods ?: Err.type("TypeInst $name (${type.javaClass.simpleName}) has not methods", source)

    override val genericTypes: List<GenericType>
        get() = type.genericTypes ?: Err.type(
            "TypeInst $name (${type.javaClass.simpleName}) has not generic types",
            source
        )


    override var loose = false
    override var nullable = false
    override val generics = GenericsTable()

    fun bootstrapGenerics(): TypeInst {
        type.genericTypes?.forEach {
            val name = it.name
            generics.expects[name] = (generics[name]?.type as? GenericType?) ?: it
            if (generics.containsKey(name)) return@forEach
            generics[name] = it.toInst().toRef()
        }
        return this
    }

    override val getter by unsafeLazy {
        MemberRefGetter(fields, methods, generics)
    }

    override fun display(): String =
        name + (if (generics.isNotEmpty()) "<${generics.values.joinToString(", ") { it.display() }}>" else "")

    override fun getMethods(
        name: String,
        argTypes: List<TypeInst>,
        args: Map<String, Expression>,
        argGenericTypes: List<TypeInst>
    ) = getter.getMethods(name, argTypes, args, argGenericTypes)

    override fun getField(name: String): AsahiVariableRef? = getter.getField(name)

    override fun validate(): Boolean {
        return type != Undefined && genericTypes.size == generics.size
    }

    override fun confirm(): AsakaType {
        if (unknown()) Err.type("Type is not confirmed")
        if (type.genericTypes != null && genericTypes.size > generics.size) Err.type("Generics are not matched")
        return type
    }

    override fun clone(): TypeInst {
        return TypeInstImpl(
            when {
                generic() -> asGeneric().clone()
                lambda() -> (type as LambdaType).clone()
                else -> type
            }, source
        ).bootstrapGenerics().apply {
            generics.putAll(this@TypeInstImpl.generics.mapValues { it.value.clone() })
            nullable = this@TypeInstImpl.nullable
        }
    }

    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "type-inst" to "#$id",
            "type" to type.serialize(),
            "generics" to generics.mapValues { it.value.serialize() },
        )

    }


    override fun hasGenerics() = type.genericTypes != null
    override fun known() = type != Undefined
    override fun unknown() = type == Undefined
    override fun void() = type == A_VOID
    override fun generic() = type is GenericType
    override fun asGeneric() = type as GenericType
    override fun lambda() = type is LambdaType
    override fun asLambda() = type as LambdaType
    override fun java() = type is JavaClass

    override fun asJava() = type as JavaClass
    override fun asaka() = type is AsakaClass

    override fun asAsaka() = type as AsakaClass
    override fun any() = type == A_OBJECT

    override fun completeWith(that: TypeInst) {
        tryCompleteWith(that)?.let {
            Err.type(it, source)
        }
    }

    override fun tryCompleteWith(that: TypeInst): String? {
        if (unknown()) {
            nullable = that.nullable
            type = that.type
            generics.completeWith(that.generics)
            return null
        } else if (lambda() && that.lambda()) {
            val thizType = asLambda()
            val thatType = that.asLambda()
            if (thizType.paramTypes.size != thatType.paramTypes.size) return "Params are not matched"
            fun lambdaComplete(a: TypeRef, b: TypeRef): String? {
                return if (a.unknown()) {
                    if (!a.setWithCheck(b)) "Params are not matched"
                    else null
                } else if (a.lambda())
                    a.tryCompleteWith(b)
                else "Params are not matched"
            }
            thizType.paramTypes.zip(thatType.paramTypes).forEach { (a, b) ->
                lambdaComplete(a, b)?.let { return it }
            }
            lambdaComplete(thizType.returnType, thatType.returnType)?.let { return it }
            return null
        }
        if (!that.isChildOf(this)) return "Type is not matched: expected ${this.display()} but got ${that.display()}"
        if (generics.size != that.generics.size) return "Generics are not matched"
        generics.filter { it.value.unknown() }.forEach { (name, ref) ->
            val check = ref.setWithCheck(that.generics[name] ?: return "Unknown generic: $name")
            if (!check) return "Generic $name is not matched"
        }
        return null
    }

    override fun isChildOf(superType: AsakaType) = type.isChildOf(superType)

    // 不检查泛型
    override fun isChildOf(superType: TypeInst) = type.isChildOf(superType.type)

    override fun completeWith(typeA: TypeInst, typeB: TypeInst?) {
        if (typeB != null) completeWith(typeA.mix(typeB)) else completeWith(typeA)
    }

    override fun assertType(other: AsakaType) {
        val thiz = confirm()
        if (thiz == other) return
        if (thiz != A_OBJECT) return
        Err.type("Type mismatch: ${this.display()} cannot be converted to ${other.display()}", source)
    }

    override fun searchSuperInst(type: AsakaType): TypeInst? {
        if (type == A_OBJECT || type == this.type) return this
        return superTypes.firstNotNullOfOrNull { it.searchSuperInst(type) }
    }

    override fun isAssignableBy(thatInst: TypeInst): Boolean {
        // 空安全
        if (!nullable && thatInst.nullable) return false
        // 如果本身是泛型
        if (generic()) return asGeneric().check(thatInst)
        val thiz = this.confirm()
        return when (val that = thatInst.confirm()) {
            is JavaClass -> {
                if (that.primitive && any()) return true
                if (!that.isChildOf(thiz)) return false
                val genericsHolder = thatInst.searchSuperInst(thiz) ?: return false
                generics.forEach { (name, expect) ->
                    genericsHolder.generics[name]?.let { actual ->
                        //注意这里,用 actual 推断 expect (if it is an unknown generic type)
                        if (expect.unknown() && expect.tryCompleteWith(actual) != null) return false
                        else if (!expect.isAssignableBy(actual)) return false
                    }
                }
                true
            }

            is GenericType -> any() || that.upper.any { isAssignableBy(it) }

            is LambdaType -> {
                if (thiz !is LambdaType) return false
                if (loose) return true
                if (thiz.paramTypes.size != that.paramTypes.size) return false

                thiz.paramTypes.zip(that.paramTypes)
                    .forEach { (a, b) -> if (!b.unknown() && !a.isAssignableBy(b)) return false }
                that.returnType.unknown() || thiz.returnType.isAssignableBy(that.returnType)
            }

            else -> Err.type("Unknown type $that", source)
        }
    }

    override fun intersectSuperWith(other: TypeInst): Set<TypeInst> {
        if (this == other) return setOf(this)
        val thizs = superTypes
        val others = other.superTypes
        thizs.intersect(others).let {
            if (it.isNotEmpty()) return it
        }
        return setOf(A_OBJECT.toInst(other.source))
    }


    override fun box(): TypeInst {
        type = type.boxType
        generics.values.forEach(TypeInst::box)
        return this
    }

    override fun unbox(): TypeInst {
        type = type.unboxType
        generics.values.forEach(TypeInst::unbox)
        return this

    }

    override fun loose(): TypeInst {
        loose = true
        return this
    }

    override fun equals(other: Any?): Boolean {
        return (other is TypeInst && name == other.name && type == other.type && nullable == other.nullable && generics == other.generics)
                || (other is AsakaType && type == other)
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}
