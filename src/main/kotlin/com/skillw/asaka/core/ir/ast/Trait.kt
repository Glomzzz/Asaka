package com.skillw.asaka.core.ir.ast

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.TypeInst

interface ConstExpr {
    val source: Span
}

sealed interface NullSafety {
    val nullSafety: Boolean
    val self: Expression
    var next: Expression?
}

/** 可以进行类型推断的节点 */
sealed interface TypeInferable {
    fun getType(): TypeInst

    fun confirmedType(): AsakaType {
        return getType().confirm()
    }
}

sealed interface Unusable<N> {
    var used: Boolean
    fun used(): N
}

sealed interface SingleNodeHolder {
    fun single(): Node?
}