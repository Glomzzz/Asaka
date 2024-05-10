package com.skillw.asaka

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.ast.EQ
import kotlin.test.Test

object IfExpr {
    @Test
    fun test3() {

        Asaka.boot()
        val source = Span.of(0..4, 0, "local", "/")

        val module = Asaka.module("TestIfExpr", Span.EMPTY) {
            importInvokeAs("println") {
                type(System::class.java) field "out" invoke "println"
            }
            clazz("ifExpr") {
                defineMethod("test") {
                    modifiers { static() }
                    body {
                        Define("x") assignTo (_if(lit(true)) then {
                            Lit(1)
                        } orElse {
                            Lit(2)
                        }).asExpr()

                        Define("y") assignTo (_try {
                            Lit(1)
                        } catch param("e", type(Exception::class.java)) then {
                            Lit(2)
                        }).asExpr()

                        Define("z") assignTo (_when {
                            case(lit(1), EQ, lit(2)) {
                                Lit(3)
                            }
                            case(lit(2), EQ, lit(3)) {
                                Lit(4)
                            }
                            otherwise {
                                Lit(5)
                            }
                        }).asExpr()
                    }
                }
            }
        }.buildTarget()
        module.print()
        Asaka.passer.pass(module)
        module.print()
    }
}