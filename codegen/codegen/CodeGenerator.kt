package com.skillw.asaka.core.codegen

import com.skillw.asaka.core.ir.type.toInternalName


abstract class CodeGenerator<T>(
    protected val name: String,
    protected val clazz: String,
) {
    protected val internalName: String = clazz.toInternalName()
    protected val simpleName: String = clazz.substring(clazz.lastIndexOf(".") + 1)
    private var method = false


    protected fun intoMethod() {
        method = true
    }

    protected fun method(): Boolean {
        return method
    }

    protected fun outMethod() {
        method = false
    }

    abstract fun generate(): T

    companion object {

//        fun generateBytecode(name: String, clazz: String, block: ClassBlock): Map<String, ByteArray> {
//            return BytecodeGenerator(name, clazz, block).generate()
//        }
    }
}
