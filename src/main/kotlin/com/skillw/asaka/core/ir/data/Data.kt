package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.ast.Expression
import com.skillw.asaka.core.ir.ast.LambdaBlock
import com.skillw.asaka.core.ir.ast.LambdaExpression
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.type.LambdaType
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.TypeRef
import com.skillw.asaka.core.ir.type.toArgMap

data class Identifier(val name: String, val source: Span) {
    fun clone() = Identifier(name, source)
}

data class LambdaInfo(
    val source: Span,
    val params: List<TypeRef>,
    val returnType: TypeRef = TypeRef.unknown(source),
)

data class MethodKey(val name: String, val params: List<AsahiParameter>) {
    val paramTypes = params.associateTo(LinkedHashMap()) { it.name to it.type }
    val minArgCount = params.count { it.default == null }
}

data class Closure(
    val name: String,
    val params: List<AsahiParameter>,
    val type: LambdaType,
    val returnType: TypeInst,
    val body: LambdaBlock,
)

class InvokeData(
    private val args: Map<String, Expression>,
    val originArgTypes: Map<String, TypeInst> = args.mapValues { it.value.getType() }
) {
    constructor(argTypes: List<TypeInst>) : this(
        emptyMap(),
        argTypes.toArgMap()
    )

    private val argTypes = mutableMapOf<String, TypeInst>()

    // paramName -> argName;
    // in fact, runtime paramName will be replaced by "arg0,1,2,3..."
    // so we need to replace it back to the original name
    private val replacement = mutableMapOf<String, String>()

    /**
     * 类型替换表
     *
     * @param arg 实参名
     * @param param 形参名
     * @param argType 参数类型
     */
    fun type(arg: String, argType: TypeInst, param: String) {
        if (param != arg)
            replacement[param] = arg
        argTypes[arg] = argType
    }

    lateinit var generics: Map<String, TypeRef>
    fun generics(generics: Map<String, TypeRef>): InvokeData {
        this.generics = generics
        return this
    }

    fun completedTypes() = argTypes

    private fun <T> Map<String, T>.getArg(name: String, source: Span, default: T? = null): T {
        return this[name] ?: this[replacement[name]!!] ?: default ?: Err.type("Missing argument: $name", source)
    }

    // Why not complete the argument types directly?
    // Because the "getMethod" method in the MemberRefGetter class is only used to find the methods that meet the conditions.
    // If directly, there will be a lot of problems,
    //   Such as the argType could be completed by the paramType, but the method may not meet the conditions.
    //  So, we need to complete the argType when we are sure that the method is the one we want.
    // That's it.
    fun arguments(params: List<AsahiParameter>, source: Span): List<Expression> {
        val arguments = mutableListOf<Expression>()
        params.forEach { param ->
            val name = param.name
            val type = param.type
            val default = param.default
            val arg = args.getArg(name, source, default)
//            val completedType = argTypes.getArg(name, source, default?.getType())
//            arg.getType().completeWith(completedType)
            arguments.add(arg.also {
                if (it.getType().unknown()) it.expected = type
            })
        }
        return arguments
    }

    fun complete() {
        args.forEach { (name, arg) ->
            arg.getType().completeWith(argTypes.getArg(name, arg.source))
            if (arg is LambdaExpression) {
                arg.params.zip(arg.paramTypes).forEach { (param, type) ->
                    param.type.completeWith(type)
                }
            }
        }
    }


}