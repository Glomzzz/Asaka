package com.skillw.asaka.core.ir.data

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.AsakaType
import com.skillw.asaka.core.ir.type.TypeInst

interface TypeSite {

    fun alias(name: String, type: TypeInst)

    fun importAs(name: String, clazz: Class<*>)

    fun importAs(name: String, type: AsakaType)

    fun findType(name: String, source: Span = Span.EMPTY): TypeInst?

    fun findType(clazz: Class<*>): AsakaType

    fun clone(): TypeSite
}