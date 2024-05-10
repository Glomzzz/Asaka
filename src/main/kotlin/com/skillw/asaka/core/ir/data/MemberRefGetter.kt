package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.core.ir.member.AsahiMethodRef
import com.skillw.asaka.core.ir.member.AsahiVariable
import com.skillw.asaka.core.ir.member.AsahiVariableRef
import com.skillw.asaka.core.ir.type.GenericType
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.TypeRef
import com.skillw.asaka.core.ir.type.toArgMap
import com.skillw.asaka.print

interface IMemberRefGetter {

    fun getMethods(
        name: String,
        argTypes: List<TypeInst>,
        args: Map<String, Expression> = emptyMap(),
        argGenericTypes: List<TypeInst> = emptyList()
    ): Set<AsahiMethodRef>

    fun getMethods(
        name: String,
        args: List<Expression>,
        argGenericTypes: List<TypeInst> = emptyList()
    ): Set<AsahiMethodRef> =
        getMethods(
            name,
            args.toArgMap(),
            argGenericTypes
        )

    fun getMethods(
        name: String,
        args: Map<String, Expression>,
        argGenericTypes: List<TypeInst> = emptyList()
    ): Set<AsahiMethodRef> =
        getMethods(
            name,
            emptyList(),
            args,
            argGenericTypes
        )

    fun getField(name: String): AsahiVariableRef?
}

class MemberRefGetter(
    val fields: Map<String, AsahiVariable>,
    val methods: Map<MethodKey, AsahiMethod>,
    val generics: GenericsTable,
) : IMemberRefGetter {

    /**
     * 其实是双向类型检查
     * 从形参中查找对应的实参
     *  - 对于 形参 中的 泛型 , 会通过实参推导并补全其中的类型 (或直接用给出的类型参数)
     *  - 对于 实参 中的 LambdaType, 会通过形参推导并补全其中的类型
     */
    private fun Map<String, TypeInst>.find(
        name: String,
        argType: TypeInst,
        generics: GenericsTable,
    ): Pair<String, TypeInst>? {
        // 如果是标注了参数名的, 则直接查找
        return if (!name.startsWith("arg"))
            get(name)?.let { name to it }
        // 否则, 从 arg 0, 1, 2... 中查找
        else {
            val index = name.substring(3).toInt()
            if (index >= size) return null
            val param = keys.elementAt(index)
            val type = values.elementAt(index)
            param to type
        }?.let { (paramName, paramType2) ->
            paramType2.toRef().loadGeneric(generics).toRef().let inner@{ paramType ->
                // 尝试补全Lambda实参, 如果补全失败, 则返回false
                if (argType.lambda() && argType.tryCompleteWith(paramType) != null) return@inner null
                // 如果参数类型未知, 则直接补全
                if (paramType.unknown() && paramType.tryCompleteWith(argType) != null) return@inner null
                if (paramType.isAssignableBy(argType)) paramName to paramType else null
            }
        }
    }


    override fun getMethods(
        name: String,
        argTypes: List<TypeInst>,
        args: Map<String, Expression>,
        argGenericTypes: List<TypeInst>
    ): Set<AsahiMethodRef> {
        // 筛选出符合条件的方法
        return methods.mapNotNull { (key, method) ->
            // 方法名
            if (key.name != name || args.size < key.minArgCount) return@mapNotNull null
            // 操作备份的数据, 如果失败可以直接跳过
            val invokeData = if (args.isNotEmpty()) InvokeData(args) else InvokeData(argTypes)
            val generics = generics.clone()
            // 首先把已知的泛型参数加入到泛型表中 若没有则先放个空的
            method.generics.forEachIndexed { index, it ->
                val argGeneric = argGenericTypes.getOrNull(index)
                if (argGeneric == null) {
                    generics[it.name] = TypeRef(TypeInst.unknown(it.source))
                    return@forEachIndexed
                }
                generics.put(it.name, TypeRef(argGeneric)) ?: return@mapNotNull null
            }
            val paramTypes = key.paramTypes
            // 遍历实参, 并推导类型
            for (i in 0 until args.size) {
                val argName = args.keys.elementAt(i)
                // 参数类型 (clone过的)
                val argType = invokeData.originArgTypes[argName]?.clone() ?: return@mapNotNull null
                // 在形参类型中查找, 并推导未知的类型.
                val (paramName, _) = paramTypes.find(argName, argType, generics) ?: return@mapNotNull null
                // 记录参数类型
                invokeData.type(argName, argType, paramName)
            }
            method.toRef(invokeData.generics(generics))
        }.toSet()
    }

    override fun getField(name: String): AsahiVariableRef? {
        return fields[name]?.toRef(generics)
    }
}

class GenericsTable : HashMap<String, TypeRef>() {
    val expects = HashMap<String, GenericType>()

    operator fun set(key: String, value: TypeRef) = put(key, value)

    override fun put(key: String, value: TypeRef): TypeRef? {
        val check: TypeRef.() -> Boolean = a@{
            if (key == "?" || key == "*") return@a true
            val expect = expects[key]
            expect?.check(this)
                ?: Err.type("Unknown generic: $key, expect ${expects.keys}", source)
        }
        if (value.known() && !value.check()) return null
        return super.put(key, value)?.apply {
            this.check = check
        }
    }

    override fun clear() {
        expects.clear()
        super.clear()
    }

    override fun putAll(from: Map<out String, TypeRef>) {
        from.forEach { (name, ref) ->
            put(name, ref)
        }
    }

    fun completeWith(generics: GenericsTable): GenericsTable {
        expects.putAll(generics.expects)
        putAll(generics.mapValues { it.value.clone() })
        return this
    }

    override fun clone() = GenericsTable().completeWith(this)
}