package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.ir.type.AsahiModifier
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.data.GenericsTable
import com.skillw.asaka.core.ir.data.LambdaInfo
import com.skillw.asaka.core.ir.data.MemberRefGetter
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMethod

open class TypeRef(inst: TypeInst) : TypeInst {
    var check: (TypeRef.() -> Boolean)? = null
    override var type: AsakaType
        get() = inst.type
        set(value) {
            inst.type = value
        }
    override val inst: TypeInst
        get() = typeInst

    private var typeInst: TypeInst = inst

    fun setWithCheck(ref: TypeRef): Boolean {
        check?.let { if (ref.known() && !it.invoke(ref)) return false }
        this.typeInst = ref.inst
        return true
    }

    override var source: Span
        get() = inst.source
        set(value) {
            inst.source = value
        }
    override val name: String
        get() = inst.name
    override val superTypes: Set<TypeInst>
        get() = inst.superTypes
    override val modifiers: Set<AsahiModifier>
        get() = inst.modifiers
    override val fields: MutableMap<String, AsahiField>
        get() = inst.fields
    override val methods: MutableMap<MethodKey, out AsahiMethod>
        get() = inst.methods
    override val genericTypes: List<GenericType>
        get() = inst.genericTypes
    override var loose: Boolean
        get() = inst.loose
        set(value) {
            inst.loose = value
        }
    override var nullable: Boolean
        get() = inst.nullable
        set(value) {
            inst.nullable = value
        }
    override val generics: GenericsTable
        get() = inst.generics
    override val getter: MemberRefGetter
        get() = inst.getter


    // flatten types
    private fun types(): Set<TypeInst> {
        val types = HashSet<TypeInst>()
        when (type) {
            is LambdaType -> {
                val lambda = type as LambdaType
                types.addAll(lambda.paramTypes.flatMap { it.types() })
                types.addAll(lambda.returnType.types())
            }

            is GenericType -> {
                types.add(this)
                types.addAll(asGeneric().upper.flatMap { it.types() })
            }

            else -> types.add(this)
        }
        generics.values.forEach {
            types.addAll(it.types())
        }
        return types
    }

    fun loadGeneric(generics: Map<String, TypeRef>): TypeInst = loadGenericSafely(generics, true)!!


    fun loadGenericSafely(generics: Map<String, TypeRef>, err: Boolean = false): TypeInst? {
        return if (generic()) {
            val generic = type as GenericType
            if (generics.containsKey(name)) generics[name]!!.also {
                if (it.known()) {
                    if (err) generic.verify(it) else if (!generic.check(it)) return null
                }
            }
            else this
        } else
            clone().apply {
                types().onEach {
                    if (!it.generic() || it.asGeneric().isStar()) return@onEach
                    val genericName = it.name
                    it as? TypeRef? ?: Err.type("This is not a TypeRef: $genericName", source)
                    if (generics.containsKey(genericName)) {
                        if (!it.setWithCheck(generics[genericName]!!)) if (err) Err.type("Failed to set generic: $genericName") else return null
                    } else Err.type("Unknown generic: $genericName")
                }
            }
    }


    override fun display() = inst.display()

    override fun getMethods(
        name: String,
        argTypes: List<TypeInst>,
        args: Map<String, Expression>,
        argGenericTypes: List<TypeInst>
    ) = inst.getMethods(name, argTypes, args, argGenericTypes)

    override fun getField(name: String) = inst.getField(name)

    override fun validate() = inst.validate()

    override fun confirm() = inst.confirm()

    override fun clone(): TypeRef = TypeRef(inst.clone()).also { it.check = check }

    override fun serialize() = inst.serialize()

    override fun hasGenerics() = inst.hasGenerics()

    override fun known() = inst.known()

    override fun unknown() = inst.unknown()

    override fun void() = inst.void()

    override fun generic() = inst.generic()

    override fun asGeneric() = inst.asGeneric()

    override fun lambda() = inst.lambda()

    override fun asLambda() = inst.asLambda()

    override fun java() = inst.java()

    override fun asJava() = inst.asJava()

    override fun asaka() = inst.asaka()

    override fun asAsaka() = inst.asAsaka()

    override fun any() = inst.any()

    override fun completeWith(that: TypeInst) = inst.completeWith(that)

    override fun completeWith(typeA: TypeInst, typeB: TypeInst?) = inst.completeWith(typeA, typeB)


    override fun tryCompleteWith(that: TypeInst) = inst.tryCompleteWith(that)

    override fun isChildOf(superType: AsakaType) = inst.isChildOf(superType)

    override fun isChildOf(superType: TypeInst) = inst.isChildOf(superType)
    override fun assertType(other: AsakaType) = inst.assertType(other)

    override fun isAssignableBy(thatInst: TypeInst) = inst.isAssignableBy(thatInst)

    override fun intersectSuperWith(other: TypeInst) = inst.intersectSuperWith(other)

    override fun box() = inst.box()

    override fun unbox() = inst.unbox()

    override fun loose() = inst.loose()

    override fun equals(other: Any?) = inst == other

    override fun hashCode() = inst.hashCode()
    override fun searchSuperInst(type: AsakaType) = inst.searchSuperInst(type)

    companion object {
        fun unknown(source: Span = Span.EMPTY) = common(Undefined, source)
        fun common(type: AsakaType, source: Span = Span.EMPTY) =
            TypeRef(TypeInst.new(type, source))

        fun lambda(params: List<TypeRef>, returnType: TypeRef, source: Span = Span.EMPTY): TypeRef {
            return common(LambdaType(source, params.toMutableList(), returnType), source)
        }

        fun lambda(info: LambdaInfo, source: Span = Span.EMPTY): TypeRef {
            return common(LambdaType(source, info.params.toMutableList(), info.returnType), source)
        }
    }
}