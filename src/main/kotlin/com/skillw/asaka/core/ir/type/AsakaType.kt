package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.Serializable
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.data.MethodKey
import com.skillw.asaka.core.ir.data.ModifierHolder
import com.skillw.asaka.core.ir.member.AsahiField
import com.skillw.asaka.core.ir.member.AsahiMethod

/**
 * @className Type
 * @author Glom
 * @date 2024/2/7 12:20
 * Copyright 2024 @Glom.
 */
sealed interface AsakaType : Serializable, ModifierHolder {
    val name: String
    val genericTypes: List<GenericType>?
    val fields: MutableMap<String, AsahiField>?
    val methods: MutableMap<MethodKey, out AsahiMethod>?
    val superTypes: Set<TypeInst>?

    fun display(): String = name

    fun toInst(source: Span = Span.EMPTY) = TypeInst.new(this, source)

    fun isChildOf(type: AsakaType): Boolean
}