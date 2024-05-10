package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.TypeInst

/**
 * @className Node
 * @author Glom
 * @date 2024/2/6 14:22
 * Copyright 2024 @Glom.
 */

sealed interface AsakaRepresent

sealed class Node(var source: Span) : Serializable, AsakaRepresent {
    open lateinit var block: MethodBlock
    var passed: Boolean = false
    override fun toString(): String = "${javaClass.simpleName} ${serialize()}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        if (toString() != other.toString()) return false
        return true
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    open fun clone(blc: MethodBlock): Node {
        return cloneNode(blc).also {
            it.block = blc
        }
    }

    protected abstract fun cloneNode(blc: MethodBlock): Node
}

/**
 * Asahi 语句
 *
 * @constructor 创建一个语句
 * @property source 源码位置
 */
sealed class TypeStatement(source: Span, protected var typeInst: TypeInst = TypeInst.unknown(source)) :
    Statement(source),
    TypeInferable {

    override fun getType(): TypeInst = typeInst
}

/**
 * Asahi 语句
 *
 * @constructor 创建一个语句
 * @property source 源码位置
 */
sealed class Statement(source: Span) : Node(source)
sealed class Expression(source: Span, protected var typeInst: TypeInst) : Node(source), TypeInferable, ConstExpr {

    constructor(source: Span, type: AsakaType) : this(source, TypeInst.new(type, source))

    open lateinit var owner: AsakaRepresent
    override fun getType(): TypeInst = typeInst

    var expected: TypeInst? = null

    var box: Boolean = false
    fun box(): Expression {
        box = true
        return this
    }

    fun unbox(): Expression {
        box = false
        return this
    }

    abstract override fun cloneNode(blc: MethodBlock): Expression
    override fun clone(blc: MethodBlock): Expression {
        return (super.clone(blc) as Expression).also {
            it.box = box
        }
    }

}