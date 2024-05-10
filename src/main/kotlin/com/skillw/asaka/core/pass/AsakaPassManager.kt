package com.skillw.asaka.core.pass

import com.skillw.asaka.core.ir.ast.ClassBlock
import com.skillw.asaka.core.ir.ast.ModuleBlock

interface AsakaPassManager {

    fun pass(module:ModuleBlock)
    fun pass(node: ClassBlock)
    fun register(pass: AsakaPass)

    fun register(path: String)
}