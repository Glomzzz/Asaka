package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.data.ModifierHolder
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.util.unsafeLazy
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.lang.reflect.*


fun Class<*>.toAsaka() = ClassAdaptor.adapt(this)
private fun Type.toAsaka() = TypeAdaptor.adapt(this)
private fun Method.toAsaka() = MethodAdaptor.adapt(this)
private fun Constructor<*>.toAsaka() = ConstructorAdaptor.adapt(this)
private fun Field.toAsaka() = FieldAdaptor.adapt(this)

private abstract class JavaAdaptor<J, A> {
    private val cache = mutableMapOf<J, A>()
    abstract fun J.adaptJava(): A

    fun adapt(j: J) = cache.getOrPut(j) { j.adaptJava() }
}

private object TypeAdaptor : JavaAdaptor<Type, TypeInst>() {
    override fun Type.adaptJava(): TypeInst {
        return when (this) {
            is Class<*> -> ClassAdaptor.adapt(this).toInst()
            is ParameterizedType -> rawType.adaptJava().apply {
                genericTypes.forEachIndexed { index, generic ->
                    generics[generic.name] = actualTypeArguments[index].adaptJava().toRef()
                }
            }

            is TypeVariable<*> -> TypeInst.new(GenericType(name).also {
                it.upper.addAll(bounds.map { bound -> bound.adaptJava().toRef() })
            })

            is GenericArrayType -> TypeInst.new(
                GenericType(genericComponentType.typeName).also {
                    it.upper.add(genericComponentType.adaptJava().toRef())
                })

            is WildcardType -> TypeInst.new(GenericType("?").also {
                it.upper.addAll(upperBounds.map { bound -> bound.adaptJava().toRef() })
            })

            else -> throw IllegalArgumentException("Unsupported type: ${this.javaClass.name}")
        }
    }
}

private object ClassAdaptor : JavaAdaptor<Class<*>, JavaClass>() {
    override fun Class<*>.adaptJava(): JavaClass {
        return when (this) {
            Int::class.java,
            Long::class.java,
            Short::class.java,
            Byte::class.java,
            Char::class.java,
            Float::class.java,
            Double::class.java,
            Boolean::class.java -> true

            else -> false
        }.let {
            JavaClassImpl(this, it)
        }
    }
}

private object ConstructorAdaptor : JavaAdaptor<Constructor<*>, AsahiMethod>() {
    override fun Constructor<*>.adaptJava(): AsahiMethod {
        val key = MethodKey("<init>", parameters.map { param ->
            val type = param.parameterizedType.toAsaka()
            AsahiParameter(param.name, type.also {
                it.nullable = param.getAnnotation(NotNull::class.java) == null
            })
        })
        val clazz = declaringClass.toInst()
        return AsahiMethod(
            Span.EMPTY,
            "<init>",
            clazz,
            key.params,
            clazz.genericTypes,
            modifiers.toAsahiModifiers(),
            clazz.asJava()
        )
    }

}

private object MethodAdaptor : JavaAdaptor<Method, AsahiMethod>() {
    override fun Method.adaptJava(): AsahiMethod {
        val rtnNullable = annotatedReturnType.getAnnotation(Nullable::class.java) != null
        val generics = typeParameters.mapNotNull { it.toAsaka().asGeneric() }
        return AsahiMethod(
            Span.EMPTY,
            name,
            genericReturnType.toAsaka().also { it.nullable = rtnNullable },
            parameters.map { param ->
                val type = param.parameterizedType.toAsaka()
                AsahiParameter(param.name, type.also {
                    it.nullable = param.getAnnotation(NotNull::class.java) == null
                })
            },
            generics,
            modifiers.toAsahiModifiers(),
            declaringClass.toType()
        )
    }
}

private object FieldAdaptor : JavaAdaptor<Field, AsahiField>() {
    override fun Field.adaptJava(): AsahiField {
        val type = genericType.toAsaka()
        return AsahiField(
            Span.EMPTY,
            name,
            type.also {
                it.nullable = getAnnotation(Nullable::class.java) != null
            },
            declaringClass.toType(),
            modifiers.toAsahiModifiers()
        )
    }
}


private class JavaClassImpl(override val clazz: Class<*>, override val primitive: Boolean = false) : JavaClass {
    override val name: String = clazz.name

    override fun display(): String {
        return (if (primitive) name.lowercase() else name.replaceFirstChar { it.uppercase() }) + if (genericTypes.isNotEmpty()) "<${
            genericTypes.joinToString(
                ", "
            ) { it.name }
        }>" else ""
    }

    override val genericTypes by unsafeLazy {
        clazz.typeParameters.map { it.toAsaka().asGeneric().apply { ownerType = this@JavaClassImpl } }
    }

    private val declaredFields by unsafeLazy {
        clazz.declaredFields.filter {
            !java.lang.reflect.Modifier.isPrivate(it.modifiers)
        }.associate { field -> field.name to field.toAsaka() }
    }

    //对于java的空安全, 采取宽松策略, 方法参数可null, 返回值默认不为null
    private val declaredMethods by unsafeLazy {
        clazz.declaredMethods.filter {
            !java.lang.reflect.Modifier.isPrivate(it.modifiers)
        }.map { method ->
            method.toAsaka()
        } + declaredConstructors
    }

    private val declaredConstructors by unsafeLazy {
        clazz.declaredConstructors.filter {
            !java.lang.reflect.Modifier.isPrivate(it.modifiers)
        }.map { constructor -> constructor.toAsaka() }
    }

    override val superTypes by unsafeLazy {
        val set = mutableSetOf<TypeInst>()
        if (clazz.hasParent()) {
            val superClass = clazz.genericSuperclass.toAsaka()
            set.add(superClass)
            superClass.confirm().superTypes?.let { set.addAll(it) }
        }
        for (i in clazz.genericInterfaces) {
            val interfaceType = i.toAsaka()
            set.add(interfaceType)
            interfaceType.confirm().superTypes?.let { set.addAll(it) }
        }
        set
    }

    override val fields by unsafeLazy {
        HashMap<String, AsahiField>().apply {
            superTypes.forEach { it.confirm().fields?.let { it1 -> putAll(it1) } }
            putAll(declaredFields)
        }
    }

    override val methods by unsafeLazy {
        HashMap<MethodKey, AsahiMethod>().apply {
            superTypes.forEach { it.confirm().methods?.let { it1 -> putAll(it1) } }
            declaredMethods.forEach {
                val key = MethodKey(it.name, it.params)
                put(key, it)
            }
        }
    }

    override val modifiersSet: Set<Modifier> by unsafeLazy {
        clazz.modifiers.toAsahiModifiers()
    }

    override fun isChildOf(type: AsakaType) = when (type) {
        is JavaClass -> !(primitive xor type.primitive) && type.clazz.isAssignableFrom(clazz)
        else -> this == type || superTypes.any { it.isChildOf(type) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JavaClass) return false

        if (clazz != other.clazz) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun serialize() =
        linkedMapOf("type" to "java-class", "name" to display())
}


typealias AsahiModifier = Modifier

private fun Int.toAsahiModifiers(): Set<AsahiModifier> {
    val modifiers = mutableSetOf<AsahiModifier>()
    if (java.lang.reflect.Modifier.isInterface(this)) modifiers.add(AsahiModifier.INTERFACE)
    else if (java.lang.reflect.Modifier.isAbstract(this)) modifiers.add(AsahiModifier.ABSTRACT)
    if (java.lang.reflect.Modifier.isFinal(this)) modifiers.add(AsahiModifier.FINAL)
    if (java.lang.reflect.Modifier.isStatic(this)) modifiers.add(AsahiModifier.STATIC)
    if (java.lang.reflect.Modifier.isPublic(this)) modifiers.add(AsahiModifier.PUBLIC)
    else if (java.lang.reflect.Modifier.isProtected(this)) modifiers.add(AsahiModifier.PROTECTED)
    else if (java.lang.reflect.Modifier.isPrivate(this)) modifiers.add(AsahiModifier.PRIVATE)
    if (java.lang.reflect.Modifier.isSynchronized(this)) modifiers.add(AsahiModifier.SYNCHRONIZED)
    if (java.lang.reflect.Modifier.isVolatile(this)) modifiers.add(AsahiModifier.VOLATILE)
    if (java.lang.reflect.Modifier.isTransient(this)) modifiers.add(AsahiModifier.TRANSIENT)
    if (java.lang.reflect.Modifier.isNative(this)) modifiers.add(AsahiModifier.NATIVE)
    return modifiers
}

private fun Int.toModifiersHolder(): ModifierHolder {
    return object : ModifierHolder {
        override val modifiersSet: Set<Modifier> =
            this@toModifiersHolder.toAsahiModifiers()
    }
}
