package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.data.Modifier
import com.skillw.asaka.core.ir.data.ModifierHolder
import com.skillw.asaka.core.ir.member.*
import com.skillw.asaka.core.ir.type.AsakaClass
import com.skillw.asaka.core.ir.type.GenericType
import com.skillw.asaka.core.ir.type.TypeInst
import com.skillw.asaka.core.ir.type.Undefined
import com.skillw.asaka.util.unsafeLazy


interface Definition<M : AsahiMember> : Serializable {
    val source: Span
    fun toMember(): M
}

open class ClassDefinition(
    override val source: Span,
    val clazz: AsakaClass,
) : Definition<AsakaClass> {
    override fun serialize() = linkedMapOf(
        "statement" to "define-class",
        "clazz" to clazz.serialize(),
    )

    override fun toMember() = clazz
}

class FieldDefinition(
    override val source: Span,
    val name: String,
    var value: Expression?,
    private val type: TypeInst,
    override val modifiersSet: Set<Modifier>,
) : ModifierHolder, Definition<AsahiField>, TypeInferable, AsakaRepresent {


    lateinit var block: ClassBlock

    override fun serialize(): Map<String, Any> {
        return linkedMapOf(
            "statement" to "define-field",
            "name" to name,
            "value" to (value?.serialize() ?: "undefined"),
            "mutable" to !modifiers.isFinal,
            "type" to type.display(),
        )
    }

    val isMutable
        get() = !modifiers.isFinal

    private val member by unsafeLazy {
        val self = block.self()
        AsahiField(source, name, type, self, modifiersSet)
    }

    override fun toMember() = member

    override fun getType() = type
}

/**
 * Asahi 函数定义语句
 *
 * 若在方法体内定义则会移到此方法体外
 *
 * @constructor 创建一个函数定义语句
 * @property source 源码位置
 * @property TypeInst 函数返回值类型
 * @property name 函数名
 * @property params 函数参数
 * @property body 函数体
 */
class MethodDefinition(
    override val source: Span,
    private val type: TypeInst,
    var name: String,
    val params: List<AsahiParameter>,
    override val modifiersSet: Set<Modifier>,
    val generics: List<GenericType>,
    var body: MethodBlock,
) : ModifierHolder, Definition<AsahiMethod>, TypeInferable {

    val key = MethodKey(name, params)
    fun isLambda() = name.contains("\$lambda\$")

    init {
        body.getType().type = Undefined
        body.getType().completeWith(type)
    }

    override fun serialize() = linkedMapOf(
        "statement" to "define-func",
        "name" to name,
        "generics" to generics.map { it.serialize() },
        "params" to params.map { linkedMapOf("name" to it.name, "type" to it.type.serialize()) },
        "body" to body.serialize(),
        "return-type" to type.display(),
    )

    fun toKey() = MethodKey(name, params)

    private val member by unsafeLazy {
        val self = body.self()
        AsahiMethod(source, name, type, params, generics, modifiersSet, self, body)
    }

    override fun toMember() = member

    override fun getType() = type
}

/**
 * Asahi 变量定义语句
 *
 * @constructor 创建一个变量定义语句
 * @property source 源码位置
 * @property typeInst 变量类型
 * @property isMutable 是否可变
 * @property name 变量名
 * @property value 变量值 (可为 null)
 */
open class VarDefineStatement(
    source: Span,
    val isMutable: Boolean,
    var name: String,
    var value: Expression?,
    type: TypeInst,
    override var used: Boolean = true,
) : TypeStatement(source, type), Unusable<VarDefineStatement>, Definition<AsahiVariable>, SingleNodeHolder {


    override fun single() = value
    override fun serialize() = linkedMapOf(
        "statement" to "define-var",
        "name" to name,
        "value" to (value?.serialize() ?: "undefined"),
        "mutable" to isMutable,
        "type" to typeInst.display(),
    )

    override fun toMember(): AsahiVariable = AsahiVariable(source, name, isMutable, typeInst, false, path = block.path)

    override fun used(): VarDefineStatement {
        this.used = true
        return this
    }

    override fun cloneNode(blc: MethodBlock) =
        VarDefineStatement(source, isMutable, name, value?.clone(blc), typeInst.clone(), used)

    override fun clone(blc: MethodBlock): VarDefineStatement {
        return super.clone(blc) as VarDefineStatement
    }

}