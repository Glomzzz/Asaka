package com.skillw.asaka.impl

import com.skillw.asaka.core.ir.type.*
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.TypeSite
import com.skillw.asaka.core.ir.type.*
import java.util.concurrent.ConcurrentHashMap

class GlobalTypeSite : TypeSite {
    private val types = ConcurrentHashMap<String, AsakaType>().apply {
        put("Int", A_INT)
        put("Long", A_LONG)
        put("Float", A_FLOAT)
        put("Double", A_DOUBLE)
        put("Boolean", A_BOOLEAN)
        put("String", A_STRING)
        put("Any", A_OBJECT)
        put("Unit", A_VOID)
    }
    private val alias = ConcurrentHashMap<String, TypeInst>()

    override fun alias(name: String, type: TypeInst) {
        alias[name] = type
    }

    override fun importAs(name: String, clazz: Class<*>) {
        types[name] = JavaClass.with(clazz)
    }

    override fun importAs(name: String, type: AsakaType) {
        types[name] = type
    }

    override fun findType(name: String, source: Span): TypeInst? {
        return alias[name] ?: types[name]?.toInst(source)
    }

    override fun findType(clazz: Class<*>): AsakaType {
        return JavaClass.with(clazz)
    }

    override fun clone() = GlobalTypeSite().apply {
        types.putAll(types)
    }
}