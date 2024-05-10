package com.skillw.asaka.impl.compile

import com.skillw.asaka.core.compile.Compiler
import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.ModuleBlock
import com.skillw.asaka.core.ir.ast.module
import com.skillw.asaka.core.ir.data.Identifier

class CompilerImpl : Compiler {
    private val modules = mutableMapOf<String, ModuleBlock>()
    override fun module(name: String, source: Span): ModuleBlock {
        return modules.getOrPut(name) { module(Identifier(name, source)) }
    }
}