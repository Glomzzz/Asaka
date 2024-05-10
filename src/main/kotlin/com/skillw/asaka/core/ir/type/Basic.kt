package com.skillw.asaka.core.ir.type

import com.skillw.asaka.core.ir.type.JavaClass
import com.skillw.asaka.util.unsafeLazy

val A_VOID by unsafeLazy { JavaClass.with(Void.TYPE) }
val A_OBJECT by unsafeLazy { JavaClass.with(Any::class.java) }
val A_STRING by unsafeLazy { JavaClass.with(String::class.java) }
val A_BOOLEAN by unsafeLazy { JavaClass.with(Boolean::class.javaPrimitiveType!!) }
val A_DOUBLE by unsafeLazy { JavaClass.with(Double::class.javaPrimitiveType!!) }
val A_FLOAT by unsafeLazy { JavaClass.with(Float::class.javaPrimitiveType!!) }
val A_LONG by unsafeLazy { JavaClass.with(Long::class.javaPrimitiveType!!) }
val A_INT by unsafeLazy { JavaClass.with(Int::class.javaPrimitiveType!!) }
val A_SHORT by unsafeLazy { JavaClass.with(Short::class.javaPrimitiveType!!) }
val A_BYTE by unsafeLazy { JavaClass.with(Byte::class.javaPrimitiveType!!) }
val A_CHAR by unsafeLazy { JavaClass.with(Char::class.javaPrimitiveType!!) }
val A_UNIT by unsafeLazy { JavaClass.with(Unit::class.javaObjectType) }
val BOX_BOOLEAN by unsafeLazy { JavaClass.with(Boolean::class.javaObjectType) }
val BOX_DOUBLE by unsafeLazy { JavaClass.with(Double::class.javaObjectType) }
val BOX_FLOAT by unsafeLazy { JavaClass.with(Float::class.javaObjectType) }
val BOX_LONG by unsafeLazy { JavaClass.with(Long::class.javaObjectType) }
val BOX_INT by unsafeLazy { JavaClass.with(Int::class.javaObjectType) }
val BOX_SHORT by unsafeLazy { JavaClass.with(Short::class.javaObjectType) }
val BOX_BYTE by unsafeLazy { JavaClass.with(Byte::class.javaObjectType) }
val BOX_CHAR by unsafeLazy { JavaClass.with(Char::class.javaObjectType) }