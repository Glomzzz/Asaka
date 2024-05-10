package com.skillw.asaka

import com.skillw.asaka.core.Span
import com.skillw.asaka.core.ir.type.A_INT
import kotlin.test.Test


object Generic {

    @Test
    fun test() {
        Asaka.boot()
        val span = Span.of(0..3, 0, "local", "local")
        val module = Asaka.module("TestGeneric", span) {
            importInvokeAs("println") { type(System::class.java) field "out" invoke "println" }
            clazz("TestAsakaClass") {
                generics {
                    name("A")
                }
                defineMethod("testLambda") {
                    generics {
                        name("T")
                    }
                    params {
                        name("l") type lambdaType(generic("T"), returnType = generic("A"))
                        name("t") type generic("T")
                    }
                    body {
                        Invoke("l") with arrayOf(variable("t"))
                    }
                }
            }
            clazz("TestAsakaClass2") {
                defineMethod("test") {
                    body {
                        Define("test") assignTo (type("TestAsakaClass").new() generics arrayOf(type(A_INT)))
                        Define("list") assignTo (type(ArrayList::class.java).new() generics arrayOf(type(A_INT)))
                        variable("test") Invoke "testLambda" with arrayOf(
                            lambda("x") then {
                                variable("x") Invoke "get" with arrayOf(lit(0))
                                Return(variable("x"))
                            },
                            variable("list"),
                        )
                    }
                }
            }
        }.buildTarget()
        module.print()
        Asaka.passer.pass(module)
        module.print()
    }
}