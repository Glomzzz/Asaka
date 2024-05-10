package com.skillw.asaka

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.builder.ModuleBuilder
import com.skillw.asaka.core.builder.NodeCreator
import com.skillw.asaka.core.compile.Compiler
import com.skillw.asaka.core.ir.data.TypeSite
import com.skillw.asaka.core.pass.AsakaPassManager
import com.skillw.asaka.impl.GlobalTypeSite
import com.skillw.asaka.impl.builder.ModuleBuilderImpl
import com.skillw.asaka.impl.builder.NodeCreatorImpl
import com.skillw.asaka.impl.compile.CompilerImpl
import com.skillw.asaka.impl.pass.AsakaPassManagerImpl

/**
 * 1. nested 放到 pass √
 * 2. 加入 when 结构
 */
object Asaka {
    private var debug = true

    @JvmStatic
    val creator: NodeCreator = NodeCreatorImpl()

    @JvmStatic
    val global: TypeSite = GlobalTypeSite()

    @JvmStatic
    val passer: AsakaPassManager = AsakaPassManagerImpl()

    @JvmStatic
    val compiler: Compiler = CompilerImpl()


    fun boot() {
        passer.register("com.skillw.asaka")
    }

    fun module(name: String, source: Span, builder: ModuleBuilder.() -> Unit): ModuleBuilder =
        ModuleBuilderImpl(compiler.module(name, source)).apply(builder).complete()

    fun debug(): Boolean {
        return debug
    }

    fun debug(debug: Boolean) {
        Asaka.debug = debug
    }

}
