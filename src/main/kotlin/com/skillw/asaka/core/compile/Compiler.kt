package com.skillw.asaka.core.compile

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.ModuleBlock

interface Compiler {
    fun module(name: String, source: Span): ModuleBlock

}