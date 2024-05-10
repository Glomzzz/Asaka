package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.error.Err
import com.skillw.asaka.core.ir.data.Closure
import com.skillw.asaka.core.ir.data.MethodCallSite
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.data.RefSite
import com.skillw.asaka.core.ir.member.AsahiMethod
import com.skillw.asaka.core.ir.member.AsahiParameter
import com.skillw.asaka.core.ir.type.*

/**
 * Asahi 方法调用表达式
 *
 * @constructor 创建一个方法调用表达式
 * @property source 源码位置
 * @property self 方法所在对象
 * @property name 方法名
 * @property args 方法参数
 */
class ConstructorInvokeExpression(
    source: Span,
    val type: Type,
    argsMap: LinkedHashMap<String, Expression>,
) : InvokeExpression(source, VoidExpression(source), "<init>", argsMap, emptyList(), false, A_VOID.toInst(source)) {
    enum class Type {
        SUPER,
        THIS
    }


    lateinit var callSite: MethodCallSite


    override fun serialize() = linkedMapOf(
        "expression" to "constructor-invoke",
        "type" to type.name.lowercase(),
        "args" to args.mapValues { it.value.serialize() },
    )

    override fun cloneNode(blc: MethodBlock) = Err.type("Cannot clone a ConstructorInvokeExpression")

}


open class InvokeExpression(
    source: Span,
    self: Expression,
    val name: String,
    val args: LinkedHashMap<String, Expression>,
    generics: List<TypeInst>,
    override val nullSafety: Boolean,
    typeInst: TypeInst = TypeInst.unknown(source)
) : Expression(source, typeInst), NullSafety, Inlineable {

    override var inline: Boolean = false

    val generics = generics.toMutableList()
    override var self: Expression = self
        set(value) {
            (value as? NullSafety?)?.next = null
            field = value
            (field as? NullSafety?)?.next = this
        }
    override var next: Expression? = null

    var invoke: InvokeExpression? = null

    override fun serialize() = linkedMapOf(
        "expression" to "invoke",
        "self" to self.serialize(),
        "name" to name,
        "generics" to generics.map { it.display() },
        "args" to args.mapValues { it.value.serialize() },
        "type" to getType().display(),
        "null-safety" to nullSafety,
    )

    override fun cloneNode(blc: MethodBlock) =
        InvokeExpression(
            source,
            self.clone(blc),
            name,
            LinkedHashMap(args.mapValues { it.value.clone(blc) }),
            generics,
            nullSafety
        )
}

sealed interface Inlineable {
    val inline: Boolean
}

class LambdaInvokeExpression(
    source: Span,
    self: Expression,
    args: LinkedHashMap<String, Expression>,
    nullSafety: Boolean,
) :
    InvokeExpression(
        source,
        self,
        "lambda-invoke",
        args,
        emptyList(),
        nullSafety,
        (self.getType().confirm() as LambdaType).returnType
    ), NullSafety {

    override var self: Expression = self
        set(value) {
            (value as? NullSafety?)?.next = null
            field = value
            (field as? NullSafety?)?.next = this
        }
    override var next: Expression? = null

    override var inline: Boolean = false

    override fun serialize() = linkedMapOf(
        "expression" to "lambda-invoke",
        "type" to getType().display(),
        "lambda" to self.serialize(),
        "args" to args.mapValues { it.value.serialize() },
        "null-safety" to nullSafety,
        "inline" to inline,
    )

    override fun cloneNode(blc: MethodBlock) =
        LambdaInvokeExpression(
            source,
            self.clone(blc) as LambdaExpression,
            LinkedHashMap(args.mapValues { it.value.clone(blc) }),
            nullSafety
        ).also {
            it.inline = inline
        }

}

/**
 * Asahi Lambda 表达式
 *
 * @constructor 创建一个表达式
 * @property source 源码位置
 * @property params 参数列表
 * @property returnType 返回值类型
 */
class LambdaExpression(
    source: Span,
    val name: String,
    val params: List<AsahiParameter>,
    val returnType: TypeRef,
    var body: LambdaBlockNode,
    val lambdaType: LambdaType = LambdaType(source, params.map { it.type.toRef() }, returnType),
) :
    Expression(source, TypeInst.new(lambdaType, source)), SingleNodeHolder {
    val paramTypes = lambdaType.paramTypes

    override fun single() = body

    fun closure(): Boolean = body.usedVars.isNotEmpty()

    fun buildMethod() = MethodDefinition(
        source,
        returnType.confirm().boxType.toInst(returnType.source),
        name,
        params.map { it.also { p -> p.type = p.type.confirm().boxType.toInst(p.type.source) } },
        setOf(Modifier.PRIVATE),
        emptyList(),
        body
    )

    lateinit var closure: Closure
    lateinit var refSite: RefSite

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "expression" to "lambda",
            "name" to name,
            "lambda" to typeInst.confirm().display(),
            "params" to params.map { it.serialize() },
            "body" to body.serialize(),

            )
    }

    override fun cloneNode(blc: MethodBlock) =
        LambdaExpression(source, name, params.map(AsahiParameter::clone), returnType.clone(), body.clone(blc))

}

/**
 * Asahi 方法调用表达式
 *
 * @constructor 创建一个方法调用表达式
 * @property source 源码位置
 * @property self 方法所在对象
 * @property name 方法名
 * @property args 方法参数
 */
class MethodInvokeExpression(
    source: Span,
    self: Expression,
    name: String,
    argsMap: LinkedHashMap<String, Expression>,
    generics: List<TypeInst>,
    nullSafety: Boolean,
    type: TypeInst,
) : InvokeExpression(source, self, name, argsMap, generics, nullSafety, type) {


    override var self: Expression = self
        set(value) {
            (value as? NullSafety?)?.next = null
            field = value
            (field as? NullSafety?)?.next = this
        }

    lateinit var method: AsahiMethod
    lateinit var callSite: MethodCallSite
    override var next: Expression? = null

    override fun serialize() = linkedMapOf(
        "expression" to "method-invoke",
        "self" to self.serialize(),
        "name" to name,
        "args" to args.mapValues { it.value.serialize() },
        "type" to getType().display(),
        "null-safety" to nullSafety,
    )

    override fun cloneNode(blc: MethodBlock) =
        MethodInvokeExpression(
            source,
            self.clone(blc),
            name,
            LinkedHashMap(args.mapValues { it.value.clone(blc) }),
            generics,
            nullSafety,
            typeInst.clone()
        )

}

/**
 * Asahi 类调用表达式
 *
 * @constructor 创建一个类调用表达式
 * @property source 源码位置
 * @property self 类型
 * @property name 类名
 */
class ReferenceExpression(
    source: Span,
    var self: Expression,
    val name: String,
    val returnType: TypeInst,
) : Expression(source, TypeInst.lambda(ArrayList(), returnType, source).loose()), SingleNodeHolder {

    override fun single() = self
    fun toFieldCall(): FieldCallExpression = FieldCallExpression(source, self, name, false, returnType)

    lateinit var refSite: RefSite

    override fun serialize() = linkedMapOf(
        "expression" to "reference",
        "self" to self.serialize(),
        "name" to name,
    )

    override fun cloneNode(blc: MethodBlock) =
        ReferenceExpression(source, self.clone(blc), name, returnType.clone())

}

/**
 * Asahi 对象创建表达式
 *
 * @constructor 创建一个对象创建表达式
 * @property source 源码位置
 * @property typeInst 类型
 * @property args 参数
 */
class ObjNewExpression(
    source: Span,
    type: TypeInst,
    args: LinkedHashMap<String, Expression>,
    generics: List<TypeInst>,
) :
    InvokeExpression(source, ClassCallExpression(source, type), "<init>", args, generics, false, type) {

    lateinit var callSite: MethodCallSite
    override fun serialize() = linkedMapOf(
        "expression" to "obj_new",
        "type" to getType().display(),
        "args" to args.mapValues { it.value.serialize() },
    )


    override fun cloneNode(blc: MethodBlock) =
        ObjNewExpression(
            source,
            typeInst.clone(),
            LinkedHashMap(args.mapValues { it.value.clone(blc) }),
            generics.map { it.clone() })

}

/**
 * Asahi 对象创建表达式
 *
 * @constructor 创建一个对象创建表达式
 * @property source 源码位置
 * @property typeInst 类型
 * @property args 参数
 */
class ArrayNewExpression(
    source: Span,
    val type: TypeRef,
    args: LinkedHashMap<String, Expression>,
    array: TypeInst = ArrayType.toInst(source).apply {
        generics["T"] = type
    }
) :
    InvokeExpression(source, ClassCallExpression(source, array), "<init>", args, listOf(type), false, array) {
    override fun serialize() = linkedMapOf(
        "expression" to "array_new",
        "type" to getType().display(),
        "args" to args.values.map { it.serialize() },
    )

    override fun cloneNode(blc: MethodBlock) =
        ArrayNewExpression(
            source,
            type.clone(),
            LinkedHashMap(args.mapValues { it.value.clone(blc) })
        )

}